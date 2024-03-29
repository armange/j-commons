/*
 * Copyright [2019] [Diego Armange Costa]
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
 * */
package br.com.armange.commons.thread;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author Diego Armange Costa
 * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService
 * @since 2019-11-26 V1.0.0 (JDK 1.8)
 * @deprecated Consider to use {@link br.com.armange.commons.thread.builder.ThreadBuilder}
 * Useful structure for thread creation in the following scenarios:
 * <ul>
 * <li><em><b>Timeout</b></em><br>
 * <p>The thread will be active only until the timeout be fired.</p></li>
 * <li><em><b>Delay</b></em><br>
 * <p>The thread will be active only after the time delay be completed.</p></li>
 * <li><em><b>Interval</b></em><br>
 * <p>The thread will be repeated after the time interval be completed.</p></li>
 * <li><em><b>Exception handling</b></em><br>
 * <p>Handles of uncaught exceptions can be thrown and handled within threads.</p></li>
 * </ul>
 * <b>Note:</b><br>
 * <em>The thread will wait a minimum delay
 * ({@link br.com.armange.commons.thread.ThreadBuilder#MINIMAL_REQUIRED_DELAY}) if and only if a
 * ({@link br.com.armange.commons.thread.ThreadBuilder#setAfterExecuteConsumer(BiConsumer)})
 * or a ({@link br.com.armange.commons.thread.ThreadBuilder#setUncaughtExceptionConsumer(Consumer)})
 * is present.</em>
 *
 * <pre>
 * <b>Example:</b>
 *
 * final ExecutorService thread = ThreadBuilder
 *          .newBuilder() //New object to build a new thread.
 *          .setDelay(1000) //The thread will wait one second before start.
 *          .setTimeout(4000) //The thread will be canceled after four seconds.
 *          .setInterval(1000) //The thread will be repeated every second.
 *          .setAfterExecuteConsumer(afterExecuteConsumer) //A consumer will be called after thread execution.
 *          .setUncaughtExceptionConsumer(throwableConsumer) //A consumer will be called after any exception thrown.
 *          .setMayInterruptIfRunning(true) //The thread interruption/cancellation will not wait execution.
 *          .setSilentInterruption(true) //Interruption and Cancellation exceptions will not be thrown.
 *          .setExecution(anyRunnable) //The thread execution.
 *          .setThreadNameSupplier(() -&gt; "Thread name")
 *          .setThreadPrioritySupplier(() -&gt; 4)
 *          .start();
 * </pre>
 */
@Slf4j
@Deprecated(since = "2.0.0", forRemoval = true)
public class ThreadBuilder {
    /**
     * 1000 milliseconds as a minimal delay.
     */
    public static final long MINIMAL_REQUIRED_DELAY = 1000;
    private Optional<Duration> timeout = Optional.empty();
    private Optional<Duration> delay = Optional.empty();
    private Optional<Duration> interval = Optional.empty();
    private Optional<BiConsumer<Runnable, Throwable>> afterExecuteConsumer = Optional.empty();
    private Optional<Consumer<Throwable>> uncaughtExceptionConsumer = Optional.empty();
    private final Optional<CaughtExecutorThreadFactory> threadFactory = Optional.empty();
    private Optional<Supplier<String>> threadNameSupplier = Optional.empty();
    private Optional<IntSupplier> threadPrioritySupplier = Optional.empty();
    private Runnable execution;
    private ScheduledCaughtExecutorService executor;
    private ExecutorResult executorResult;
    private boolean mayInterruptIfRunning;
    private boolean silentInterruption;
    private final int corePoolSize;

    private ThreadBuilder() {
        corePoolSize = 1;
    }

    private ThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * @return a new object to perform a thread creation.
     */
    public static ThreadBuilder newBuilder() {
        return new ThreadBuilder();
    }

    /**
     * @param corePoolSize the {@link ScheduledCaughtExecutorService} pool size.
     * @return a new object to perform a thread creation.
     * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService#ScheduledCaughtExecutorService(int)
     */
    public static ThreadBuilder newBuilder(final int corePoolSize) {
        return new ThreadBuilder(corePoolSize);
    }

    /**
     * Sets the timeout value.
     *
     * @param milliseconds the timeout value in milliseconds.
     * @return the current thread builder.
     */
    public ThreadBuilder setTimeout(final long milliseconds) {
        timeout = Optional.of(Duration.ofMillis(milliseconds));

        return this;
    }

    /**
     * Sets the delay value.
     *
     * @param milliseconds the delay value in milliseconds.
     * @return the current thread builder.
     */
    public ThreadBuilder setDelay(final long milliseconds) {
        delay = Optional.of(Duration.ofMillis(milliseconds));

        return this;
    }

    /**
     * Sets the repeating interval value.
     *
     * @param milliseconds the repeating interval value in milliseconds.
     * @return the current thread builder.
     */
    public ThreadBuilder setInterval(final long milliseconds) {
        interval = Optional.of(Duration.ofMillis(milliseconds));

        return this;
    }

    /**
     * Sets the consumer to be called after thread execution.
     *
     * @param afterExecuteConsumer the consumer to be called after thread execution.
     * @return the current thread builder.
     * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService#afterExecute(Runnable, Throwable)
     */
    public ThreadBuilder setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteConsumer) {
        this.afterExecuteConsumer = Optional.ofNullable(afterExecuteConsumer);

        return this;
    }

    /**
     * Sets the consumer to be called after exception throwing. This consumer will be called as a first after-executes
     * consumer.
     *
     * @param uncaughtExceptionConsumer the consumer to be called after exception throwing.
     * @return the current thread builder.
     */
    public ThreadBuilder setUncaughtExceptionConsumer(final Consumer<Throwable> uncaughtExceptionConsumer) {
        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);

        return this;
    }

    /**
     * Sets the thread name supplier.The thread factory will consume this supplier to generate a thread name
     * before return a new thread.
     *
     * @param threadNameSupplier the thread name supplier.
     * @return the current thread builder.
     */
    public ThreadBuilder setThreadNameSupplier(final Supplier<String> threadNameSupplier) {
        this.threadNameSupplier = Optional.ofNullable(threadNameSupplier);

        return this;
    }

    /**
     * Sets the thread priority supplier.The thread factory will consume this supplier to generate a thread priority
     * before return a new thread.
     *
     * @param threadPrioritySupplier the thread priority supplier.
     * @return the current thread builder.
     */
    public ThreadBuilder setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier) {
        this.threadPrioritySupplier = Optional.ofNullable(threadPrioritySupplier);

        return this;
    }

    /**
     * Sets the thread execution.
     *
     * @param execution the thread execution({@link java.lang.Runnable})
     * @return the current thread builder.
     */
    public ThreadBuilder setExecution(final Runnable execution) {
        this.execution = execution;

        requireExecutionNonNull();

        return this;
    }

    /**
     * Sets the thread-interrupting-flag.
     *
     * @param flag true if the thread executing this task should be interrupted;
     *             otherwise, in-progress tasks are allowed to complete.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    public ThreadBuilder setMayInterruptIfRunning(final boolean flag) {
        mayInterruptIfRunning = flag;

        return this;
    }

    /**
     * Sets the thread-silent-interrupting-flag.
     *
     * @param flag true if the Interruption/Cancellation exceptions should be ignored.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     * @see java.util.concurrent.CancellationException
     * @see java.lang.InterruptedException
     */
    public ThreadBuilder setSilentInterruption(final boolean flag) {
        silentInterruption = flag;

        return this;
    }

    /**
     * Starts the thread.
     *
     * @return the executor's result after starting thread.
     * @see ExecutorResult
     */
    public ExecutorResult start() {
        createExecutorAndRunThread();

        return executorResult;
    }

    /**
     * Starts the thread.
     *
     * @return the current thread builder to prepare another thread.
     */
    public ThreadBuilder startAndBuildOther() {
        createExecutorAndRunThread();

        return this;
    }

    private void createExecutorAndRunThread() {
        requireExecutionNonNull();

        executor = new ScheduledCaughtExecutorService(corePoolSize, getThreadFactory());

        runThread();

        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
    }

    private ThreadFactory getThreadFactory() {
        final CaughtExecutorThreadFactory factory = threadFactory.orElse(new CaughtExecutorThreadFactory());

        uncaughtExceptionConsumer
                .ifPresent(uec -> factory.setUncaughtExceptionHandler((thread, throwable) -> uec.accept(throwable)));

        threadNameSupplier.ifPresent(tns -> factory.setThreadName(tns.get()));

        threadPrioritySupplier.ifPresent(tps -> factory.setThreadPriority(tps.getAsInt()));

        return factory;
    }

    private void runThread() {
        if (noSchedule()) {
            runWithNoSchedule();
        } else if (onlyDelay()) {
            runWithDelay();
        } else if (onlyTimeout()) {
            runWithDelayAndTimeout();
        } else if (onlyInterval()) {
            runWithDelayAndInterval();
        } else if (delayAndTimeout()) {
            runWithDelayAndTimeout();
        } else if (delayAndInterval()) {
            runWithDelayAndInterval();
        } else /* All */ {
            runWithAllTimesControls();
        }
    }

    private void requireExecutionNonNull() {
        Objects.requireNonNull(execution, "The {execution} parameter is required");
    }

    private boolean noSchedule() {
        return !delay.isPresent() && !timeout.isPresent() && !interval.isPresent();
    }

    private boolean onlyDelay() {
        return delay.isPresent() && !timeout.isPresent() && !interval.isPresent();
    }

    private boolean onlyTimeout() {
        return !delay.isPresent() && timeout.isPresent() && !interval.isPresent();
    }

    private boolean onlyInterval() {
        return !delay.isPresent() && !timeout.isPresent() && interval.isPresent();
    }

    private boolean delayAndTimeout() {
        return delay.isPresent() && timeout.isPresent() && !interval.isPresent();
    }

    private boolean delayAndInterval() {
        return delay.isPresent() && !timeout.isPresent() && interval.isPresent();
    }

    private void runWithNoSchedule() {
        final Future<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithDelay() {
        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithDelayAndTimeout() {
        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult timeoutExecutorResult = handleInterruption(future);

        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private void runWithDelayAndInterval() {
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void newExecutorResultIfNull() {
        executorResult = executorResult == null ? new ExecutorResult(executor) : executorResult;
    }

    private void runWithAllTimesControls() {
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult timeoutExecutorResult = handleInterruption(future);

        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private long handleDelay() {
        final long localDelay = delay.orElse(Duration.ofMillis(0)).toMillis();

        if (uncaughtExceptionConsumer.isPresent() || afterExecuteConsumer.isPresent()) {
            return localDelay >= MINIMAL_REQUIRED_DELAY ? localDelay : localDelay + MINIMAL_REQUIRED_DELAY;
        } else {
            return localDelay;
        }
    }

    private BiConsumer<Runnable, Throwable> handleException(final Future<?> future) {
        return (a, b) -> {
            try {
                if (future.isDone())
                    future.get();
            } catch (final Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                if (isNotSilentOrIsExecutionException(e)) {
                    uncaughtExceptionConsumer.ifPresent(consumer -> consumer.accept(e));
                }
            }
        };
    }

    private boolean isNotSilentOrIsExecutionException(final Exception e) {
        return !silentInterruption || !(e instanceof CancellationException) && !(e instanceof InterruptedException);
    }

    private ExecutorResult handleInterruption(final ScheduledFuture<?> future) {
        final ScheduledCaughtExecutorService localExecutor = new ScheduledCaughtExecutorService(1);

        localExecutor.addAfterExecuteConsumer(handleException(future));
        localExecutor.schedule(cancelFuture(future), timeout.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);

        final ExecutorResult timeoutExecutorResult = new ExecutorResult(localExecutor);

        timeoutExecutorResult.getFutures().add(future);

        return timeoutExecutorResult;
    }

    private Runnable cancelFuture(final ScheduledFuture<?> future) {
        return () -> {
            if (!future.isDone() && !future.isCancelled()) future.cancel(mayInterruptIfRunning);
        };
    }
}