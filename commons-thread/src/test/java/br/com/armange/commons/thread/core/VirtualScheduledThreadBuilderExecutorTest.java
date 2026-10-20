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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualScheduledThreadBuilderExecutorTest {
    @Test
    void executesSingleCallableAndReportsFailure() throws Exception {
        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<String> value = executor.schedule(
                    () -> Thread.currentThread()
                            .isVirtual() ? "virtual" : "platform",
                    0, TimeUnit.MILLISECONDS);
            final VirtualScheduledTask<Void> failure = executor.schedule(() -> {
                throw new IllegalStateException("task failed");
            }, 0, TimeUnit.MILLISECONDS);

            assertEquals("virtual", value.get(2, TimeUnit.SECONDS));
            value.quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertEquals("task failed", assertThrows(ExecutionException.class,
                    () -> failure.get(2, TimeUnit.SECONDS))
                    .getCause()
                    .getMessage());
            assertEquals("task failed", assertThrows(ExecutionException.class,
                    () -> failure.quiescence()
                            .toCompletableFuture()
                            .get(2, TimeUnit.SECONDS))
                    .getCause()
                    .getMessage());
        }
    }

    @Test
    void cancelDuringWaitPreventsExecution() throws Exception {
        final AtomicReference<Thread> waitingThread = new AtomicReference<>();
        final ThreadFactory factory = command -> {
            final Thread thread = Thread.ofVirtual()
                    .unstarted(command);
            waitingThread.set(thread);

            return thread;
        };

        try (VirtualScheduledThreadBuilderExecutor executor =
                     new VirtualScheduledThreadBuilderExecutor(factory)) {
            final AtomicInteger calls = new AtomicInteger();
            final VirtualScheduledTask<Void> pending = executor.schedule(
                    () -> {
                        calls.incrementAndGet();
                    }, 1, TimeUnit.HOURS);

            assertTrue(pending.cancel(false));
            assertThrows(CancellationException.class, pending::get);
            pending.quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertEquals(0, calls.get());
            assertTrue(waitingThread.get()
                    .isVirtual());
            assertTrue(waitingThread.get()
                    .isDaemon());
            assertFalse(waitingThread.get()
                    .isInterrupted());
        }
    }

    @Test
    void cancelBeforeDeadlineRaceDoesNotInterruptOrStartBody() throws Exception {
        final CountDownLatch enteredFactoryThread = new CountDownLatch(1);
        final CountDownLatch releaseFactoryThread = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        final AtomicInteger calls = new AtomicInteger();
        final ThreadFactory factory = command -> Thread.ofVirtual()
                .unstarted(() -> {
                    enteredFactoryThread.countDown();

                    try {
                        releaseFactoryThread.await();
                    } catch (final InterruptedException exception) {
                        interrupted.set(true);
                        Thread.currentThread()
                                .interrupt();
                    }

                    command.run();
                });

        try (VirtualScheduledThreadBuilderExecutor executor =
                     new VirtualScheduledThreadBuilderExecutor(factory)) {
            final VirtualScheduledTask<Void> pending = executor.schedule(() -> {
                calls.incrementAndGet();
            }, 0, TimeUnit.MILLISECONDS);

            try {
                assertTrue(enteredFactoryThread.await(2, TimeUnit.SECONDS));
                assertTrue(pending.cancel(false));
                assertFalse(pending.quiescence()
                        .toCompletableFuture()
                        .isDone());
                releaseFactoryThread.countDown();
                pending.quiescence()
                        .toCompletableFuture()
                        .get(2, TimeUnit.SECONDS);
                assertFalse(interrupted.get());
                assertEquals(0, calls.get());
            } finally {
                releaseFactoryThread.countDown();
            }
        }
    }

    @Test
    void cancelWithoutInterruptionWaitsForBodyAndReportsLateFailure() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> running = executor.schedule(() -> {
                started.countDown();
                release.await();
                throw new IllegalStateException("late failure");
            }, 0, TimeUnit.MILLISECONDS);

            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertTrue(running.cancel(false));
            assertThrows(CancellationException.class, running::get);
            assertFalse(running.quiescence()
                    .toCompletableFuture()
                    .isDone());

            release.countDown();
            assertEquals("late failure", assertThrows(ExecutionException.class,
                    () -> running.quiescence()
                            .toCompletableFuture()
                            .get(2, TimeUnit.SECONDS))
                    .getCause()
                    .getMessage());
        } finally {
            release.countDown();
        }
    }

    @Test
    void periodicCyclesAreVirtualDistinctAndNeverOverlap() throws Exception {
        final CountDownLatch firstStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirst = new CountDownLatch(1);
        final CountDownLatch secondFinished = new CountDownLatch(1);
        final AtomicReference<Thread> first = new AtomicReference<>();
        final AtomicReference<Thread> second = new AtomicReference<>();
        final AtomicInteger calls = new AtomicInteger();

        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> series = executor.scheduleAtFixedRate(() -> {
                final int cycle = calls.incrementAndGet();

                if (cycle == 1) {
                    first.set(Thread.currentThread());
                    firstStarted.countDown();
                    await(releaseFirst);
                } else if (cycle == 2) {
                    second.set(Thread.currentThread());
                    secondFinished.countDown();
                }
            }, 0, 1, TimeUnit.MILLISECONDS);

            try {
                assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
                assertFalse(secondFinished.await(50, TimeUnit.MILLISECONDS));
                releaseFirst.countDown();
                assertTrue(secondFinished.await(2, TimeUnit.SECONDS));
                assertTrue(series.cancel(false));
                series.quiescence()
                        .toCompletableFuture()
                        .get(2, TimeUnit.SECONDS);
                assertTrue(first.get()
                        .isVirtual());
                assertTrue(second.get()
                        .isVirtual());
                assertNotSame(first.get(), second.get());
            } finally {
                releaseFirst.countDown();
                series.cancel(true);
            }
        }
    }

    @Test
    void fixedDelayStopsAfterFailure() throws Exception {
        final AtomicInteger calls = new AtomicInteger();

        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> series = executor.scheduleWithFixedDelay(() -> {
                calls.incrementAndGet();
                throw new IllegalArgumentException("cycle failed");
            }, 0, 1, TimeUnit.MILLISECONDS);

            assertEquals("cycle failed", assertThrows(ExecutionException.class,
                    () -> series.get(2, TimeUnit.SECONDS))
                    .getCause()
                    .getMessage());
            series.quiescence()
                    .toCompletableFuture()
                    .handle((value, failure) -> null)
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertEquals(1, calls.get());
        }
    }

    @Test
    void closeWaitsForStartedOneShotAndRejectsNewWork() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch closeFinished = new CountDownLatch(1);
        final VirtualScheduledThreadBuilderExecutor executor =
                VirtualScheduledThreadBuilderExecutor.create();

        try {
            final VirtualScheduledTask<String> running = executor.schedule(() -> {
                started.countDown();
                release.await();

                return "finished";
            }, 0, TimeUnit.MILLISECONDS);

            assertTrue(started.await(2, TimeUnit.SECONDS));

            final Thread closing = Thread.ofPlatform()
                    .start(() -> {
                        executor.close();
                        closeFinished.countDown();
                    });

            assertFalse(closeFinished.await(50, TimeUnit.MILLISECONDS));
            release.countDown();
            assertTrue(closeFinished.await(2, TimeUnit.SECONDS));
            closing.join(2000);
            assertEquals("finished", running.get(2, TimeUnit.SECONDS));
            assertThrows(RejectedExecutionException.class,
                    () -> executor.schedule(() -> {
                    }, 0, TimeUnit.MILLISECONDS));
        } finally {
            release.countDown();
            executor.close();
        }
    }

    @Test
    void cancelTrueAttemptsToInterruptRunningBody() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> running = executor.schedule(() -> {
                started.countDown();

                try {
                    new CountDownLatch(1)
                            .await();
                } catch (final InterruptedException exception) {
                    interrupted.countDown();
                    Thread.currentThread()
                            .interrupt();
                }
            }, 0, TimeUnit.MILLISECONDS);

            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertTrue(running.cancel(true));
            assertTrue(interrupted.await(2, TimeUnit.SECONDS));
            assertThrows(CancellationException.class, running::get);
            running.quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
        }
    }

    @Test
    void quiescenceStageCannotCancelInternalSignal() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> running = executor.schedule(() -> {
                started.countDown();
                await(release);
            }, 0, TimeUnit.MILLISECONDS);

            try {
                assertTrue(started.await(2, TimeUnit.SECONDS));
                running.quiescence()
                        .toCompletableFuture()
                        .cancel(false);
                release.countDown();
                running.get(2, TimeUnit.SECONDS);
                running.quiescence()
                        .toCompletableFuture()
                        .get(2, TimeUnit.SECONDS);
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void suppliedFactoryNamesEveryCycle() throws Exception {
        final AtomicInteger created = new AtomicInteger();
        final CountDownLatch cycles = new CountDownLatch(2);
        final ThreadFactory factory = command -> {
            created.incrementAndGet();

            return Thread.ofVirtual()
                    .name("custom-" + created.get())
                    .unstarted(command);
        };

        try (VirtualScheduledThreadBuilderExecutor executor =
                     new VirtualScheduledThreadBuilderExecutor(factory)) {
            final VirtualScheduledTask<String> task = executor.schedule(
                    () -> Thread.currentThread()
                            .getName(), 0, TimeUnit.MILLISECONDS);
            final VirtualScheduledTask<Void> series = executor.scheduleWithFixedDelay(
                    cycles::countDown, 0, 1, TimeUnit.MILLISECONDS);

            try {
                assertEquals("custom-1", task.get(2, TimeUnit.SECONDS));
                assertTrue(cycles.await(2, TimeUnit.SECONDS));
            } finally {
                series.cancel(false);
            }

            series.quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertTrue(created.get() >= 3);
        }
    }

    @Test
    void rejectsInvalidFactoryWithoutPendingHandles() throws Exception {
        assertFactoryFails(Thread::new, IllegalArgumentException.class);
        assertFactoryFails(command -> null, IllegalArgumentException.class);
        assertFactoryFails(command -> {
            throw new IllegalStateException("factory failed");
        }, IllegalStateException.class);
    }

    @Test
    void factoryFailureAfterFirstCycleEndsSeries() throws Exception {
        final AtomicInteger created = new AtomicInteger();
        final CountDownLatch firstCycle = new CountDownLatch(1);
        final ThreadFactory factory = command -> {
            if (created.incrementAndGet() == 1) {
                return Thread.ofVirtual()
                        .unstarted(command);
            }

            return null;
        };

        try (VirtualScheduledThreadBuilderExecutor executor =
                     new VirtualScheduledThreadBuilderExecutor(factory)) {
            final VirtualScheduledTask<Void> series = executor.scheduleAtFixedRate(
                    firstCycle::countDown, 0, 1, TimeUnit.MILLISECONDS);

            assertTrue(firstCycle.await(2, TimeUnit.SECONDS));
            assertTrue(assertThrows(ExecutionException.class,
                    () -> series.get(2, TimeUnit.SECONDS))
                    .getCause()
                    instanceof IllegalArgumentException);
            assertTrue(assertThrows(ExecutionException.class,
                    () -> series.quiescence()
                            .toCompletableFuture()
                            .get(2, TimeUnit.SECONDS))
                    .getCause()
                    instanceof IllegalArgumentException);
            assertEquals(2, created.get());
        }
    }

    @Test
    void closeCancelsPendingWaitAndRejectsReentrantClose() throws Exception {
        final VirtualScheduledThreadBuilderExecutor executor =
                VirtualScheduledThreadBuilderExecutor.create();

        try {
            final VirtualScheduledTask<Void> pending = executor.schedule(() -> {
                throw new AssertionError("pending body ran");
            }, 1, TimeUnit.HOURS);
            final var observer = pending.quiescence()
                    .whenComplete((value, failure) ->
                            assertThrows(IllegalStateException.class, executor::close));

            executor.close();

            assertThrows(CancellationException.class, pending::get);
            observer.toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertThrows(RejectedExecutionException.class,
                    () -> executor.schedule(() -> {
                    }, 0, TimeUnit.MILLISECONDS));
        } finally {
            executor.close();
        }
    }

    @Test
    void closeFromOwnBodyFailsWithoutDeadlock() throws Exception {
        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> task = executor.schedule(
                    () -> {
                        assertThrows(IllegalStateException.class, executor::close);
                    },
                    0, TimeUnit.MILLISECONDS);

            task.get(2, TimeUnit.SECONDS);
            task.quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
        }
    }

    private static void assertFactoryFails(final ThreadFactory factory,
                                           final Class<? extends Throwable> expected)
            throws Exception {
        try (VirtualScheduledThreadBuilderExecutor executor =
                     new VirtualScheduledThreadBuilderExecutor(factory)) {
            final VirtualScheduledTask<Void> task = executor.schedule(() -> {
            }, 0, TimeUnit.MILLISECONDS);

            assertTrue(expected.isInstance(assertThrows(ExecutionException.class,
                    () -> task.get(2, TimeUnit.SECONDS))
                    .getCause()));
            assertTrue(expected.isInstance(assertThrows(ExecutionException.class,
                    () -> task.quiescence()
                            .toCompletableFuture()
                            .get(2, TimeUnit.SECONDS))
                    .getCause()));
        }
    }

    private static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException exception) {
            Thread.currentThread()
                    .interrupt();
        }
    }
}
