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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadBuilderStageTest {

    @Test
    void singleRunnableStartReturnsOnlyItsTaskFuture() throws Exception {
        final SingleTaskExecutorResult<Void> result = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                })
                .start();

        try (var executor = result.getExecutorService()) {
            assertTrue(executor.isShutdown());
            assertNull(result.getFuture()
                    .get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void defaultSingleStartShutsDownWithoutWaitingOrInterruptingRunningTask() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        final SingleTaskThreadBuilder<Void, Runnable> single = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                    started.countDown();

                    try {
                        if (!release.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Task was not released");
                        }
                    } catch (final InterruptedException e) {
                        interrupted.set(true);
                        Thread.currentThread().interrupt();
                    }
                });
        final SingleTaskExecutorResult<Void> result = single.start();

        try {
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertTrue(result.getExecutorService()
                    .isShutdown());
            assertFalse(result.getExecutorService()
                    .isTerminated());
            assertFalse(result.getFuture()
                    .isDone());
            assertFalse(interrupted.get());
            assertThrows(IllegalStateException.class, single::start);
            assertThrows(IllegalStateException.class, single::keepExecutorOpen);
        } finally {
            release.countDown();

            if (!result.getExecutorService()
                    .isShutdown()) {
                result.getExecutorService()
                        .shutdownNow();
            }
        }

        assertNull(result.getFuture()
                .get(5, TimeUnit.SECONDS));
        assertTrue(result.getExecutorService()
                .awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(interrupted.get());
    }

    @Test
    void defaultMultipleStartDrainsQueuedTasksAndMakesAliasesTerminal() throws Exception {
        final CountDownLatch firstStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirst = new CountDownLatch(1);
        final AtomicInteger calls = new AtomicInteger();
        final SingleTaskThreadBuilder<Void, Runnable> single = ThreadBuilder
                .newBuilder(1)
                .setExecution((Runnable) () -> {
                    firstStarted.countDown();

                    try {
                        if (!releaseFirst.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("First task was not released");
                        }
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }

                    calls.incrementAndGet();
                });
        final MultipleTaskThreadBuilder<Void, Runnable> multiple = single.startAndBuildOther();

        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

        final MultipleTaskExecutorResult<Void> result = multiple
                .setExecution(calls::incrementAndGet)
                .startAndBuildOther()
                .setExecution(calls::incrementAndGet)
                .start();

        try {
            assertTrue(result.getExecutorService()
                    .isShutdown());
            assertEquals(3, result.getFutures()
                    .size());
            assertFalse(result.getFuture(1)
                    .isDone());
            assertFalse(result.getFuture(2)
                    .isDone());
            assertThrows(IllegalStateException.class, single::startAndBuildOther);
            assertThrows(IllegalStateException.class,
                    () -> single.setExecution(calls::incrementAndGet));
            assertThrows(IllegalStateException.class, multiple::start);
        } finally {
            releaseFirst.countDown();

            if (!result.getExecutorService()
                    .isShutdown()) {
                result.getExecutorService()
                        .shutdownNow();
            }
        }

        for (final Future<Void> future : result.getFutures()) {
            assertNull(future.get(5, TimeUnit.SECONDS));
        }

        assertEquals(3, calls.get());
        assertTrue(result.getExecutorService()
                .awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void multipleRunnableResultIsBatchSnapshotAndSingleAliasCanStartNextBatch() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        final SingleTaskThreadBuilder<Void, Runnable> single = ThreadBuilder
                .newBuilder(2)
                .setExecution((Runnable) calls::incrementAndGet)
                .keepExecutorOpen();
        final MultipleTaskThreadBuilder<Void, Runnable> multiple = single.startAndBuildOther();

        assertThrows(IllegalStateException.class, single::start);
        assertThrows(IllegalStateException.class,
                () -> single.setExecution((Runnable) calls::incrementAndGet));

        multiple
                .setExecution(calls::incrementAndGet)
                .startAndBuildOther()
                .setExecution(calls::incrementAndGet);

        final MultipleTaskExecutorResult<Void> firstBatch = multiple.start();
        final List<Future<Void>> firstFutures = firstBatch.getFutures();

        try (var executor = firstBatch.getExecutorService()) {
            assertFalse(executor.isShutdown());
            assertEquals(3, firstFutures.size());

            for (final Future<Void> future : firstFutures) {
                assertNull(future.get(5, TimeUnit.SECONDS));
            }

            assertThrows(UnsupportedOperationException.class,
                    () -> firstFutures.add(firstBatch.getFuture(0)));
            assertThrows(IllegalStateException.class, multiple::start);
            assertThrows(IllegalStateException.class, multiple::startAndBuildOther);

            final MultipleTaskThreadBuilder<Void, Runnable> nextBatch = single
                    .setExecution((Runnable) calls::incrementAndGet)
                    .startAndBuildOther();

            assertThrows(IllegalStateException.class, multiple::start);

            final MultipleTaskExecutorResult<Void> secondBatch = nextBatch
                    .setExecution(calls::incrementAndGet)
                    .start();

            final List<Future<Void>> secondFutures = secondBatch.getFutures();

            assertSame(firstBatch.getExecutorService(), secondBatch.getExecutorService());
            assertFalse(executor.isShutdown());
            assertEquals(3, firstFutures.size());
            assertEquals(2, secondFutures.size());

            for (final Future<Void> future : secondFutures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertEquals(5, calls.get());
        }
    }

    @Test
    void keepExecutorOpenAfterIntermediateSubmissionTransfersOwnershipAndPersists() throws Exception {
        final SingleTaskThreadBuilder<Void, Runnable> single = ThreadBuilder
                .newBuilder()
                .setExecution((Runnable) () -> {
                });
        final MultipleTaskExecutorResult<Void> batch = single
                .startAndBuildOther()
                .keepExecutorOpen()
                .setExecution(() -> {
                })
                .start();

        try (var executor = batch.getExecutorService()) {
            assertFalse(executor.isShutdown());

            final SingleTaskExecutorResult<Void> next = single
                    .setExecution(() -> {
                    })
                    .start();

            assertSame(executor, next.getExecutorService());
            assertFalse(executor.isShutdown());
            next.getFuture()
                    .get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void callableStagesKeepResultConsumerAndTypedFutures() throws Exception {
        final SingleCallableTaskThreadBuilder<Integer> single = ThreadBuilder
                .newBuilder()
                .setExecution((Callable<Integer>) () -> 11)
                .setThreadResultConsumer(ignored -> {
                });
        final MultipleCallableTaskThreadBuilder<Integer> multiple = single
                .startAndBuildOther()
                .setExecution(() -> 22)
                .setThreadResultConsumer(ignored -> {
                });
        final MultipleTaskExecutorResult<Integer> result = multiple.start();

        try (var executor = result.getExecutorService()) {
            assertEquals(List.of(11, 22), List.of(
                    result.getFuture(0)
                            .get(5, TimeUnit.SECONDS),
                    result.getFuture(1)
                            .get(5, TimeUnit.SECONDS)));
            final List<Future<Integer>> futures = result.getFutures();
            assertTrue(futures.stream()
                    .allMatch(Future::isDone));
        }
    }

    @Test
    void callableMultipleStageKeepsResultConsumerAfterAnotherSubmission() throws Exception {
        final CountDownLatch thirdObserved = new CountDownLatch(1);
        final CountDownLatch releaseThird = new CountDownLatch(1);
        final MultipleTaskExecutorResult<Integer> result = ThreadBuilder
                .newBuilder()
                .setExecution((Callable<Integer>) () -> 1)
                .startAndBuildOther()
                .setExecution(() -> 2)
                .startAndBuildOther()
                .keepExecutorOpen()
                .setThreadResultConsumer(value -> {
                    if (value == 3) {
                        thirdObserved.countDown();
                    }
                })
                .setExecution(() -> {
                    if (!releaseThird.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Third task was not released");
                    }

                    return 3;
                })
                .start();

        try (var executor = result.getExecutorService()) {
            assertFalse(executor.isShutdown());
            releaseThird.countDown();
            assertEquals(List.of(1, 2, 3), List.of(
                    result.getFuture(0)
                            .get(5, TimeUnit.SECONDS),
                    result.getFuture(1)
                            .get(5, TimeUnit.SECONDS),
                    result.getFuture(2)
                            .get(5, TimeUnit.SECONDS)));
            assertTrue(thirdObserved.await(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void singleCallableStartExposesItsValue() throws Exception {
        final SingleTaskExecutorResult<Integer> result = ThreadBuilder
                .newBuilder()
                .setExecution((Callable<Integer>) () -> 42)
                .setThreadResultConsumer(ignored -> {
                })
                .start();

        try (var executor = result.getExecutorService()) {
            assertEquals(42, result.getFuture()
                    .get(5, TimeUnit.SECONDS));
        }
    }
}
