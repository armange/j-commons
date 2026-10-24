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
 * */
package br.com.armange.commons.thread.builder;

import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadBuilderLifecycleTest {

    @Test
    void finalStartShutsDownAfterSubmittingEveryTaskWithoutAwaitingCompletion() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CommonRunnableBuilder builder = ThreadBuilder
                .newBuilder(1)
                .setExecution(() -> {
                    started.countDown();
                    await(release);
                });
        builder.startAndBuildOther();

        assertFalse(builder.executor.isShutdown());

        final ClosedExecutorResult<Void> result = assertInstanceOf(ClosedExecutorResult.class,
                builder.setExecution(() -> {
                }).start());

        try {
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertTrue(result.getExecutorService().isShutdown());
            assertEquals(2, result.getFutures().size());
            assertSame(result.getFutures().get(0), result.getFuture(0));
            assertFalse(result.getFuture(0).isDone());
            assertThrows(UnsupportedOperationException.class,
                    () -> result.getFutures().add(result.getFuture(0)));
            assertThrows(IndexOutOfBoundsException.class, () -> result.getFuture(2));
            assertThrows(IllegalStateException.class, builder::start);
        } finally {
            release.countDown();
        }

        for (final Future<Void> future : result.getFutures()) {
            assertNull(future.get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void openStageKeepsExecutorAndReturnsIndependentBatchSnapshots() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        final CommonRunnableBuilder defaultStage = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) calls::incrementAndGet);
        defaultStage.startAndBuildOther();

        final OpenRunnableBuilder openStage = defaultStage.keepExecutorOpen();

        assertThrows(IllegalStateException.class, defaultStage::start);

        final OpenExecutorResult<Void> first = openStage
                .setExecution(calls::incrementAndGet)
                .start();

        try (var executor = first.getExecutorService()) {
            assertFalse(executor.isShutdown());
            assertEquals(2, first.getFutures().size());

            final OpenExecutorResult<Void> second = openStage
                    .setExecution(calls::incrementAndGet)
                    .start();

            assertSame(executor, second.getExecutorService());
            assertEquals(2, first.getFutures().size());
            assertEquals(1, second.getFutures().size());

            for (final Future<Void> future : first.getFutures()) {
                future.get(5, TimeUnit.SECONDS);
            }

            second.getFuture(0)
                    .get(5, TimeUnit.SECONDS);
            assertEquals(3, calls.get());
        }
    }

    @Test
    void callableResultPreservesTypedFuture() throws Exception {
        final ClosedExecutorResult<Integer> result = assertInstanceOf(ClosedExecutorResult.class,
                ThreadBuilder.newBuilder()
                        .setExecution((Callable<Integer>) () -> 42)
                        .start());

        assertEquals(42, result.getFuture(0)
                .get(5, TimeUnit.SECONDS));
    }

    @Test
    void poolSizeHonorsMinimumAndConfiguredLargerValue() throws Exception {
        final ExecutorResult<Void> minimum = ThreadBuilder
                .newBuilder(0)
                .setExecution((Runnable) () -> {
                })
                .start();

        assertEquals(1, ((ScheduledThreadBuilderExecutor) minimum.getExecutorService()).getCorePoolSize());
        minimum.getFuture(0)
                .get(5, TimeUnit.SECONDS);

        final ExecutorResult<Void> larger = ThreadBuilder
                .newBuilder(3)
                .setScheduling((Runnable) () -> {
                })
                .setTimeout(100)
                .start();

        assertEquals(3, ((ScheduledThreadBuilderExecutor) larger.getExecutorService()).getCorePoolSize());
        larger.getFuture(0)
                .get(5, TimeUnit.SECONDS);
    }

    @Test
    void timeoutUpgradesExistingExecutorAndStillRunsAfterOrderlyShutdown() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final TimingRunnableBuilder builder = ThreadBuilder
                .newBuilder(1)
                .setScheduling((Runnable) started::countDown);
        builder.startAndBuildOther();
        assertTrue(started.await(5, TimeUnit.SECONDS));
        assertEquals(1, builder.executor.getCorePoolSize());

        final ExecutorResult<Void> result = builder
                .setExecution(() -> {
                    try {
                        Thread.sleep(5_000);
                    } catch (final InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    }
                })
                .setTimeout(100)
                .setMayInterruptIfRunning(true)
                .start();

        assertInstanceOf(ClosedExecutorResult.class, result);
        assertEquals(2, ((ScheduledThreadBuilderExecutor) result.getExecutorService()).getCorePoolSize());
        assertTrue(result.getExecutorService().isShutdown());
        assertThrows(CancellationException.class, () -> result.getFuture(1)
                .get(5, TimeUnit.SECONDS));
    }

    @Test
    void scheduledOpenStageLeavesNonperiodicExecutorToCaller() throws Exception {
        final OpenTimingRunnableBuilder open = ThreadBuilder
                .newBuilder()
                .setScheduling((Runnable) () -> {
                })
                .setDelay(10)
                .keepExecutorOpen();
        final OpenExecutorResult<Void> result = open.start();

        try (var executor = result.getExecutorService()) {
            assertFalse(executor.isShutdown());
            result.getFuture(0)
                    .get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void scheduledOpenCallablePreservesTypedResultAndTimeoutPool() throws Exception {
        final OpenTimingCallableBuilder<Integer> open = ThreadBuilder
                .newBuilder(1)
                .setScheduling((Callable<Integer>) () -> 42)
                .setTimeout(1_000)
                .keepExecutorOpen();
        final OpenExecutorResult<Integer> result = open.start();

        try (var executor = result.getExecutorService()) {
            assertEquals(2, ((ScheduledThreadBuilderExecutor) executor).getCorePoolSize());
            assertFalse(executor.isShutdown());
            assertEquals(42, result.getFuture(0)
                    .get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void periodicTaskReturnsOpenResultEvenAfterTimeoutCancelsIt() throws Exception {
        final ExecutorResult<Void> result = ThreadBuilder
                .newBuilder()
                .setScheduling((Runnable) () -> {
                })
                .setInterval(20)
                .setTimeout(100)
                .start();

        try (var executor = result.getExecutorService()) {
            assertInstanceOf(OpenExecutorResult.class, result);
            assertEquals(2, ((ScheduledThreadBuilderExecutor) executor).getCorePoolSize());
            assertThrows(CancellationException.class, () -> result.getFuture(0)
                    .get(5, TimeUnit.SECONDS));
            assertFalse(executor.isShutdown());
        }
    }

    @Test
    void internalTimeoutActionDoesNotReachUserAfterExecuteConsumer() throws Exception {
        final AtomicInteger observedTasks = new AtomicInteger();
        final ExecutorResult<Void> result = ThreadBuilder
                .newBuilder()
                .setScheduling((Runnable) () -> {
                })
                .setTimeout(50)
                .setAfterExecuteConsumer((task, failure) -> observedTasks.incrementAndGet())
                .start();

        result.getFuture(0)
                .get(5, TimeUnit.SECONDS);
        assertTrue(result.getExecutorService()
                .awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1, observedTasks.get());
    }

    @Test
    void manySubmissionsKeepOneExecutorCallback() throws Exception {
        final int taskCount = 200;
        final CountDownLatch callbacks = new CountDownLatch(taskCount);
        final CommonRunnableBuilder builder = ThreadBuilder
                .newBuilder(1)
                .setExecution((Runnable) () -> {
                })
                .setAfterExecuteConsumer((task, failure) -> callbacks.countDown());

        for (int index = 1; index < taskCount; index++) {
            builder.startAndBuildOther();
            builder.setExecution(() -> {
            });
        }

        final ExecutorResult<Void> result = builder.start();
        assertTrue(callbacks.await(5, TimeUnit.SECONDS));
        assertEquals(taskCount, result.getFutures().size());
        assertEquals(1, ((ScheduledThreadBuilderExecutor) result.getExecutorService())
                .getAfterExecuteConsumers().size());
    }

    @Test
    void explicitCallbackConfigurationAddsOneGlobalCallbackPerCall() throws Exception {
        final CountDownLatch firstCallback = new CountDownLatch(1);
        final CountDownLatch secondCallback = new CountDownLatch(1);
        final AtomicInteger firstCallbackCalls = new AtomicInteger();
        final OpenRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                })
                .setAfterExecuteConsumer((task, failure) -> {
                    firstCallbackCalls.incrementAndGet();
                    firstCallback.countDown();
                })
                .keepExecutorOpen();
        final OpenExecutorResult<Void> first = builder.start();

        try (var executor = first.getExecutorService()) {
            first.getFuture(0).get(5, TimeUnit.SECONDS);
            assertTrue(firstCallback.await(5, TimeUnit.SECONDS));
            builder.setAfterExecuteConsumer((task, failure) -> secondCallback.countDown());
            final OpenExecutorResult<Void> second = builder
                    .setExecution(() -> {
                    })
                    .start();
            second.getFuture(0).get(5, TimeUnit.SECONDS);

            assertTrue(secondCallback.await(5, TimeUnit.SECONDS));
            assertEquals(2, firstCallbackCalls.get());
            assertEquals(2, ((ScheduledThreadBuilderExecutor) executor)
                    .getAfterExecuteConsumers().size());
        }
    }

    @Test
    void uncaughtHandlerCannotBeReconfiguredAfterExecutorCreation() {
        final CommonRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                });
        builder.startAndBuildOther();

        assertThrows(IllegalStateException.class,
                () -> builder.setUncaughtExceptionConsumer(failure -> {
                }));

        builder.setExecution(() -> {
        }).start();
    }

    @Test
    void submissionFailureClosesExecutorBeforeFirstResult() {
        final TimingRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setScheduling((Runnable) () -> {
                })
                .setInterval(0);

        assertThrows(IllegalArgumentException.class, builder::start);
        assertTrue(builder.executor.isShutdown());
    }

    @Test
    void failureAfterOpenResultLeavesExecutorToCaller() throws Exception {
        final OpenTimingRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setScheduling((Runnable) () -> {
                })
                .keepExecutorOpen();
        final OpenExecutorResult<Void> result = builder.start();

        try (var executor = result.getExecutorService()) {
            result.getFuture(0)
                    .get(5, TimeUnit.SECONDS);
            builder.setExecution(() -> {
            }).setInterval(0);

            assertThrows(IllegalArgumentException.class, builder::start);
            assertFalse(executor.isShutdown());
        }
    }

    @Test
    void nextSubmissionRequiresAnotherExecutionWithoutClosingTheExecutor() throws Exception {
        final CommonRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                });
        builder.startAndBuildOther();

        assertThrows(IllegalStateException.class, builder::start);
        assertFalse(builder.executor.isShutdown());

        final ExecutorResult<Void> result = builder
                .setExecution(() -> {
                })
                .start();

        assertEquals(2, result.getFutures().size());
        assertTrue(result.getExecutorService().isShutdown());
        for (final Future<Void> future : result.getFutures()) {
            assertNull(future.get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void openStageRequiresAnotherExecutionAfterStart() throws Exception {
        final OpenRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                })
                .keepExecutorOpen();
        final OpenExecutorResult<Void> first = builder.start();

        try (var executor = first.getExecutorService()) {
            assertThrows(IllegalStateException.class, builder::start);
            assertFalse(executor.isShutdown());

            final OpenExecutorResult<Void> second = builder
                    .setExecution(() -> {
                    })
                    .start();

            assertNull(first.getFuture(0).get(5, TimeUnit.SECONDS));
            assertNull(second.getFuture(0).get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void scheduledCallableStageAcceptsAnotherTaskAfterStart() throws Exception {
        final OpenTimingCallableBuilder<Integer> builder = ThreadBuilder
                .newBuilder()
                .setScheduling((Callable<Integer>) () -> 1)
                .setDelay(10)
                .keepExecutorOpen();
        final OpenExecutorResult<Integer> first = builder.start();

        try (var executor = first.getExecutorService()) {
            assertThrows(IllegalStateException.class, builder::start);
            final OpenExecutorResult<Integer> second = builder
                    .setExecution(() -> 2)
                    .start();

            assertEquals(1, first.getFuture(0).get(5, TimeUnit.SECONDS));
            assertEquals(2, second.getFuture(0).get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void timingConfigurationResetsAfterSubmission() {
        final OpenTimingRunnableBuilder builder = ThreadBuilder
                .newBuilder()
                .setScheduling((Runnable) () -> {
                })
                .setDelay(10)
                .setInterval(20)
                .setTimeout(100)
                .setMayInterruptIfRunning(true)
                .keepExecutorOpen();
        final TaskConfiguration<Runnable> submittedTask = builder.taskConfiguration;
        builder.startAndBuildOther();

        try {
            assertNotSame(submittedTask, builder.taskConfiguration);
            assertNull(builder.taskConfiguration.execution());
            assertTrue(builder.taskConfiguration.delay().isEmpty());
            assertTrue(builder.taskConfiguration.interval().isEmpty());
            assertTrue(builder.taskConfiguration.timeout().isEmpty());
            assertFalse(builder.taskConfiguration.mayInterruptIfRunning());
        } finally {
            builder.executor.shutdownNow();
        }
    }

    @Test
    void firstTaskKeepsInterruptingTimeoutAfterNextTaskDisablesIt() throws Exception {
        assertTimeoutUsesSubmittedInterruptPolicy(true, false);
    }

    @Test
    void firstTaskKeepsNonInterruptingTimeoutAfterNextTaskEnablesIt() throws Exception {
        assertTimeoutUsesSubmittedInterruptPolicy(false, true);
    }

    private static void assertTimeoutUsesSubmittedInterruptPolicy(
            final boolean firstPolicy, final boolean secondPolicy) throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);
        final CountDownLatch completed = new CountDownLatch(1);
        final OpenTimingRunnableBuilder builder = ThreadBuilder
                .newBuilder(2)
                .setScheduling((Runnable) () -> {
                    started.countDown();
                    try {
                        release.await();
                    } catch (final InterruptedException failure) {
                        interrupted.countDown();
                        Thread.currentThread().interrupt();
                    } finally {
                        completed.countDown();
                    }
                })
                .setTimeout(2_000)
                .setMayInterruptIfRunning(firstPolicy)
                .keepExecutorOpen();
        final OpenExecutorResult<Void> first = builder.start();

        try (var executor = first.getExecutorService()) {
            try {
                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertFalse(first.getFuture(0).isDone());
                final OpenExecutorResult<Void> second = builder
                        .setExecution(() -> {
                        })
                        .setMayInterruptIfRunning(secondPolicy)
                        .start();
                assertNull(second.getFuture(0).get(5, TimeUnit.SECONDS));
                assertThrows(CancellationException.class,
                        () -> first.getFuture(0).get(5, TimeUnit.SECONDS));

                if (firstPolicy) {
                    assertTrue(interrupted.await(5, TimeUnit.SECONDS));
                    assertTrue(completed.await(5, TimeUnit.SECONDS));
                } else {
                    assertEquals(1, interrupted.getCount());
                    assertEquals(1, completed.getCount());
                    release.countDown();
                    assertTrue(completed.await(5, TimeUnit.SECONDS));
                }
            } finally {
                release.countDown();
            }
        }
    }

    private static void await(final CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for the test latch");
            }
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }
}
