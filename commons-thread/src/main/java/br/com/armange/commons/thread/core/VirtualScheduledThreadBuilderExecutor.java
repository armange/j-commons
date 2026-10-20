/*
 * Copyright [2026] [Diego Armange Costa]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.armange.commons.thread.core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * Schedules each execution on one fresh virtual thread. The thread waits for
 * its deadline and then runs the body itself. A periodic series never overlaps
 * its own bodies and uses another virtual thread for each cycle.
 *
 * <p>Virtual threads are daemon threads. Pending tasks do not keep the process
 * alive; callers must wait through a handle or {@link #close()} while a
 * non-daemon thread is still active. The class owns no timer or executor.
 * Closing cancels pending work and periodic series and waits for bodies
 * already running. It does not own resources used by task bodies.
 */
public final class VirtualScheduledThreadBuilderExecutor implements AutoCloseable {
    private final Object lifecycleLock = new Object();
    private final Set<Task<?>> tasks = ConcurrentHashMap.newKeySet();
    private final ThreadLocal<Boolean> internalExecution = new ThreadLocal<>();
    private final ThreadLocal<Boolean> publishingQuiet = new ThreadLocal<>();
    private final CompletableFuture<Void> closeDone = new CompletableFuture<>();
    private final ThreadFactory virtualFactory;
    private volatile boolean closed;

    /**
     * Creates an executor using the JDK virtual-thread factory.
     *
     * @return an executor that the caller must close
     */
    public static VirtualScheduledThreadBuilderExecutor create() {
        return new VirtualScheduledThreadBuilderExecutor(Thread.ofVirtual()
                .factory());
    }

    /**
     * Creates an executor with a caller-supplied virtual-thread factory. The
     * factory is consulted for every execution or cycle and must create a
     * fresh, unstarted virtual thread for the supplied command. A factory
     * failure, null result or platform thread fails the affected handle.
     *
     * @param virtualFactory factory for one virtual thread per execution
     */
    public VirtualScheduledThreadBuilderExecutor(final ThreadFactory virtualFactory) {
        this.virtualFactory = Objects.requireNonNull(virtualFactory, "virtualFactory");
    }

    /**
     * Schedules one Runnable. Its handle returns {@code null} on success.
     *
     * @param command task to run in a fresh virtual thread
     * @param delay delay before execution; zero and negative values request immediate execution
     * @param unit unit of the delay
     * @return a handle for the entire execution
     */
    public VirtualScheduledTask<Void> schedule(final Runnable command, final long delay,
                                               final TimeUnit unit) {
        Objects.requireNonNull(command, "command");

        return schedule(Executors.callable(command, null), delay, unit);
    }

    /**
     * Schedules one Callable and preserves its result or failure in the handle.
     *
     * @param <V> result type
     * @param command task to run in a fresh virtual thread
     * @param delay delay before execution; zero and negative values request immediate execution
     * @param unit unit of the delay
     * @return a handle for the entire execution
     */
    public <V> VirtualScheduledTask<V> schedule(final Callable<V> command, final long delay,
                                                final TimeUnit unit) {
        return submit(command, ScheduleMode.ONCE, delay, 0, unit);
    }

    /**
     * Schedules a series at planned intervals measured from its initial start.
     * A late cycle may begin immediately after the previous body ends.
     *
     * @param command task to run in a fresh virtual thread each cycle
     * @param initialDelay delay before the first execution
     * @param period positive period between planned starts
     * @param unit unit of both delays
     * @return a handle for the whole series
     */
    public VirtualScheduledTask<Void> scheduleAtFixedRate(final Runnable command,
                                                          final long initialDelay,
                                                          final long period,
                                                          final TimeUnit unit) {
        Objects.requireNonNull(command, "command");

        return submit(Executors.callable(command, null), ScheduleMode.FIXED_RATE,
                initialDelay, period, unit);
    }

    /**
     * Schedules a series whose delay starts after each body finishes.
     *
     * @param command task to run in a fresh virtual thread each cycle
     * @param initialDelay delay before the first execution
     * @param delay positive delay after a cycle completes
     * @param unit unit of both delays
     * @return a handle for the whole series
     */
    public VirtualScheduledTask<Void> scheduleWithFixedDelay(final Runnable command,
                                                             final long initialDelay,
                                                             final long delay,
                                                             final TimeUnit unit) {
        Objects.requireNonNull(command, "command");

        return submit(Executors.callable(command, null), ScheduleMode.FIXED_DELAY,
                initialDelay, delay, unit);
    }

    private <V> VirtualScheduledTask<V> submit(final Callable<V> command,
                                               final ScheduleMode mode,
                                               final long initialDelay,
                                               final long interval,
                                               final TimeUnit unit) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(unit, "unit");

        if (mode != ScheduleMode.ONCE && interval <= 0) {
            throw new IllegalArgumentException("period or delay must be positive");
        }

        final Task<V> task = new Task<>(command, mode, initialDelay, interval, unit);

        synchronized (lifecycleLock) {
            if (closed) {
                throw new RejectedExecutionException("Executor is closed");
            }

            tasks.add(task);
        }

        task.startCycle(task.nextDueNanos);

        return task;
    }

    /**
     * Rejects new work, cancels pending waits and periodic series, then waits
     * for bodies already started. An active one-shot body may finish normally.
     * A body that ignores interruption can delay closing. Do not call this
     * method from a task body, a supplied factory, or a synchronous
     * {@link VirtualScheduledTask#quiescence()} callback.
     */
    @Override
    public void close() {
        if (Boolean.TRUE.equals(internalExecution.get())) {
            throw new IllegalStateException("Cannot close from a scheduled task or its factory");
        }

        if (Boolean.TRUE.equals(publishingQuiet.get())) {
            throw new IllegalStateException("Cannot close from a quiescence callback");
        }

        final ArrayList<Task<?>> snapshot;

        synchronized (lifecycleLock) {
            if (closed) {
                snapshot = null;
            } else {
                closed = true;
                snapshot = new ArrayList<>(tasks);
            }
        }

        if (snapshot == null) {
            closeDone.join();

            return;
        }

        try {
            for (final Task<?> task : snapshot) {
                task.stopForClose();
            }

            for (final Task<?> task : snapshot) {
                task.awaitQuiescence();
            }

            closeDone.complete(null);
        } catch (final RuntimeException | Error failure) {
            closeDone.completeExceptionally(failure);

            throw failure;
        }
    }

    private static Thread requireVirtualThread(final ThreadFactory factory,
                                               final Runnable command) {
        final Thread thread = factory.newThread(command);

        if (thread == null || !thread.isVirtual() || thread.getState() != Thread.State.NEW) {
            throw new IllegalArgumentException("Factory must create a new, unstarted virtual thread");
        }

        return thread;
    }

    private void restoreInternalExecution(final Boolean previousExecution) {
        if (previousExecution == null) {
            internalExecution.remove();
        } else {
            internalExecution.set(previousExecution);
        }
    }

    private enum ScheduleMode {
        ONCE, FIXED_RATE, FIXED_DELAY
    }

    private final class Task<V> implements VirtualScheduledTask<V> {
        private final Object lock = new Object();
        private final Callable<V> command;
        private final ScheduleMode mode;
        private final long intervalNanos;
        private final CompletableFuture<V> result = new CompletableFuture<>();
        private final CompletableFuture<Void> quiet = new CompletableFuture<>();
        private final CompletionStage<Void> readOnlyQuiet = quiet.minimalCompletionStage();
        private long nextDueNanos;
        private Thread worker;
        private boolean creating;
        private boolean bodyStarted;
        private boolean settled;
        private boolean published;
        private Throwable settlementFailure;

        private Task(final Callable<V> command, final ScheduleMode mode,
                     final long initialDelay, final long interval, final TimeUnit unit) {
            this.command = command;
            this.mode = mode;
            this.intervalNanos = unit.toNanos(interval);
            this.nextDueNanos = System.nanoTime() + unit.toNanos(Math.max(0, initialDelay));
        }

        private void startCycle(final long dueNanos) {
            final Boolean previousExecution = internalExecution.get();
            internalExecution.set(true);

            try {
                if (!prepareCycle()) {
                    return;
                }

                final Thread thread;

                try {
                    thread = requireVirtualThread(virtualFactory, () -> runCycle(dueNanos));
                } catch (final RuntimeException | Error failure) {
                    failCreation(failure);

                    return;
                }

                startCreatedThread(thread);
            } finally {
                try {
                    publishSettlement();
                } finally {
                    restoreInternalExecution(previousExecution);
                }
            }
        }

        private boolean prepareCycle() {
            synchronized (lock) {
                if (closed || result.isDone()) {
                    result.cancel(false);
                    settle(null);

                    return false;
                }

                creating = true;

                return true;
            }
        }

        private void failCreation(final Throwable failure) {
            synchronized (lock) {
                creating = false;
                fail(failure);
            }
        }

        private void startCreatedThread(final Thread thread) {
            synchronized (lock) {
                creating = false;

                if (closed || result.isDone()) {
                    result.cancel(false);
                    settle(null);

                    return;
                }

                worker = thread;

                try {
                    thread.start();
                } catch (final RuntimeException | Error failure) {
                    worker = null;
                    fail(failure);
                }
            }
        }

        private void runCycle(final long dueNanos) {
            final Boolean previousExecution = internalExecution.get();
            internalExecution.set(true);

            try {
                if (!awaitDeadline(dueNanos) || !beginBody()) {
                    return;
                }

                V value = null;
                Throwable failure = null;

                try {
                    value = command.call();
                } catch (final Throwable thrown) {
                    failure = thrown;
                }

                finishBody(value, failure);
            } catch (final Throwable unexpected) {
                failUnexpected(unexpected);
            } finally {
                try {
                    finishAbortedWait();
                } finally {
                    try {
                        publishSettlement();
                    } finally {
                        restoreInternalExecution(previousExecution);
                    }
                }
            }
        }

        private boolean awaitDeadline(final long dueNanos) {
            while (true) {
                synchronized (lock) {
                    if (closed || result.isDone()) {
                        return false;
                    }
                }

                final long remainingNanos = dueNanos - System.nanoTime();

                if (remainingNanos <= 0) {
                    return true;
                }

                LockSupport.parkNanos(this, remainingNanos);
                Thread.interrupted();
            }
        }

        private boolean beginBody() {
            synchronized (lock) {
                if (closed || result.isDone()) {
                    result.cancel(false);

                    return false;
                }

                bodyStarted = true;

                return true;
            }
        }

        private void finishBody(final V value, final Throwable failure) {
            final long dueNanos;

            synchronized (lock) {
                worker = null;
                bodyStarted = false;

                if (failure != null) {
                    fail(failure);

                    return;
                }

                if (mode == ScheduleMode.ONCE) {
                    result.complete(value);
                    settle(null);

                    return;
                }

                if (closed || result.isCancelled()) {
                    result.cancel(false);
                    settle(null);

                    return;
                }

                nextDueNanos = nextDeadline();
                dueNanos = nextDueNanos;
            }

            startCycle(dueNanos);
        }

        private long nextDeadline() {
            if (mode == ScheduleMode.FIXED_RATE && intervalNanos != Long.MAX_VALUE) {
                return nextDueNanos + intervalNanos;
            }

            return System.nanoTime() + intervalNanos;
        }

        private void failUnexpected(final Throwable failure) {
            synchronized (lock) {
                worker = null;
                bodyStarted = false;
                creating = false;
                fail(failure);
            }
        }

        private void finishAbortedWait() {
            synchronized (lock) {
                if (worker == Thread.currentThread() && !bodyStarted) {
                    worker = null;

                    if (closed) {
                        result.cancel(false);
                    }

                    if (result.isDone()) {
                        settle(null);
                    }
                }
            }
        }

        private void fail(final Throwable failure) {
            result.completeExceptionally(failure);
            settle(failure);
        }

        private void settle(final Throwable failure) {
            if (!settled) {
                settled = true;
                settlementFailure = failure;
            }
        }

        private void publishSettlement() {
            final Throwable failure;

            synchronized (lock) {
                if (!settled || published) {
                    return;
                }

                published = true;
                failure = settlementFailure;
            }

            final Boolean previousPublishing = publishingQuiet.get();
            publishingQuiet.set(true);

            try {
                if (failure == null) {
                    quiet.complete(null);
                } else {
                    quiet.completeExceptionally(failure);
                }
            } finally {
                if (previousPublishing == null) {
                    publishingQuiet.remove();
                } else {
                    publishingQuiet.set(previousPublishing);
                }

                tasks.remove(this);
            }
        }

        private void stopForClose() {
            final boolean shouldCancel;

            synchronized (lock) {
                shouldCancel = mode != ScheduleMode.ONCE || !bodyStarted;
            }

            if (shouldCancel) {
                cancel(false);
            }
        }

        private void awaitQuiescence() {
            try {
                quiet.join();
            } catch (final CompletionException | CancellationException ignored) {
                // A task failure is observable through get() and quiescence().
            }
        }

        @Override
        public CompletionStage<Void> quiescence() {
            return readOnlyQuiet;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            final boolean cancelled;

            synchronized (lock) {
                cancelled = result.cancel(mayInterruptIfRunning);

                if (cancelled) {
                    if (worker != null) {
                        if (mayInterruptIfRunning) {
                            worker.interrupt();
                        } else if (!bodyStarted) {
                            LockSupport.unpark(worker);
                        }
                    }

                    if (worker == null && !creating) {
                        settle(null);
                    }
                }
            }

            publishSettlement();

            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return result.isCancelled();
        }

        @Override
        public boolean isDone() {
            return result.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return result.get();
        }

        @Override
        public V get(final long timeout, final TimeUnit timeoutUnit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return result.get(timeout, timeoutUnit);
        }
    }
}
