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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualScheduledThreadBuilderExecutorIntegrationTest {
    @Test
    void fixedRateRecoversLateCycleWhileFixedDelayWaitsAfterCompletion() throws Exception {
        final CountDownLatch firstRateStarted = new CountDownLatch(1);
        final CountDownLatch firstDelayStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirstCycles = new CountDownLatch(1);
        final CountDownLatch secondRateStarted = new CountDownLatch(1);
        final CountDownLatch secondDelayStarted = new CountDownLatch(1);
        final AtomicInteger rateCycles = new AtomicInteger();
        final AtomicInteger delayCycles = new AtomicInteger();
        final AtomicLong firstRateFinishedAt = new AtomicLong();
        final AtomicLong firstDelayFinishedAt = new AtomicLong();
        final AtomicLong secondRateStartedAt = new AtomicLong();
        final AtomicLong secondDelayStartedAt = new AtomicLong();

        try (VirtualScheduledThreadBuilderExecutor executor =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Void> fixedRate = executor.scheduleAtFixedRate(() -> {
                final int cycle = rateCycles.incrementAndGet();

                if (cycle == 1) {
                    firstRateStarted.countDown();
                    await(releaseFirstCycles);
                    firstRateFinishedAt.set(System.nanoTime());
                } else if (cycle == 2) {
                    secondRateStartedAt.set(System.nanoTime());
                    secondRateStarted.countDown();
                }
            }, 0, 700, TimeUnit.MILLISECONDS);
            final VirtualScheduledTask<Void> fixedDelay = executor.scheduleWithFixedDelay(() -> {
                final int cycle = delayCycles.incrementAndGet();

                if (cycle == 1) {
                    firstDelayStarted.countDown();
                    await(releaseFirstCycles);
                    firstDelayFinishedAt.set(System.nanoTime());
                } else if (cycle == 2) {
                    secondDelayStartedAt.set(System.nanoTime());
                    secondDelayStarted.countDown();
                }
            }, 0, 700, TimeUnit.MILLISECONDS);

            try {
                assertTrue(firstRateStarted.await(2, TimeUnit.SECONDS));
                assertTrue(firstDelayStarted.await(2, TimeUnit.SECONDS));
                assertFalse(secondRateStarted.await(900, TimeUnit.MILLISECONDS));
                assertFalse(secondDelayStarted.await(0, TimeUnit.MILLISECONDS));

                releaseFirstCycles.countDown();
                assertTrue(secondRateStarted.await(3, TimeUnit.SECONDS));
                assertTrue(secondDelayStarted.await(3, TimeUnit.SECONDS));
            } finally {
                releaseFirstCycles.countDown();
                fixedRate.cancel(false);
                fixedDelay.cancel(false);
            }

            fixedRate
                    .quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            fixedDelay
                    .quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);

            final long rateGap = secondRateStartedAt.get() - firstRateFinishedAt.get();
            final long delayGap = secondDelayStartedAt.get() - firstDelayFinishedAt.get();

            assertTrue(rateGap < TimeUnit.MILLISECONDS.toNanos(550));
            assertTrue(delayGap >= TimeUnit.MILLISECONDS.toNanos(600));
            assertTrue(rateGap < delayGap);
        }
    }

    @Test
    void independentPeriodicSeriesUseConfiguredFactoryAndFreshVirtualThreads() throws Exception {
        final CountDownLatch firstRateStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirstRate = new CountDownLatch(1);
        final CountDownLatch secondRateStarted = new CountDownLatch(1);
        final CountDownLatch twoDelayCycles = new CountDownLatch(2);
        final AtomicInteger rateCycles = new AtomicInteger();
        final AtomicInteger delayCycles = new AtomicInteger();
        final AtomicInteger activeRateBodies = new AtomicInteger();
        final AtomicBoolean rateOverlapped = new AtomicBoolean();
        final AtomicReference<Thread> firstRateThread = new AtomicReference<>();
        final AtomicReference<Thread> secondRateThread = new AtomicReference<>();
        final AtomicReference<Thread> firstDelayThread = new AtomicReference<>();
        final AtomicReference<Thread> secondDelayThread = new AtomicReference<>();
        final AtomicInteger names = new AtomicInteger();
        final ThreadFactory virtualFactory = command -> Thread.ofVirtual()
                .name("consumer-" + names.incrementAndGet())
                .unstarted(command);

        try (VirtualScheduledThreadBuilderExecutor executor =
                     new VirtualScheduledThreadBuilderExecutor(virtualFactory)) {
            final VirtualScheduledTask<Void> fixedRate = executor.scheduleAtFixedRate(() -> {
                if (activeRateBodies.incrementAndGet() != 1) {
                    rateOverlapped.set(true);
                }

                try {
                    final int cycle = rateCycles.incrementAndGet();

                    if (cycle == 1) {
                        firstRateThread.set(Thread.currentThread());
                        firstRateStarted.countDown();
                        await(releaseFirstRate);
                    } else if (cycle == 2) {
                        secondRateThread.set(Thread.currentThread());
                        secondRateStarted.countDown();
                    }
                } finally {
                    activeRateBodies.decrementAndGet();
                }
            }, 0, 5, TimeUnit.MILLISECONDS);

            try {
                assertTrue(firstRateStarted.await(2, TimeUnit.SECONDS));

                final VirtualScheduledTask<Void> fixedDelay =
                        executor.scheduleWithFixedDelay(() -> {
                            final int cycle = delayCycles.incrementAndGet();

                            if (cycle == 1) {
                                firstDelayThread.set(Thread.currentThread());
                            } else if (cycle == 2) {
                                secondDelayThread.set(Thread.currentThread());
                            }

                            twoDelayCycles.countDown();
                        }, 0, 5, TimeUnit.MILLISECONDS);

                try {
                    assertTrue(twoDelayCycles.await(2, TimeUnit.SECONDS));
                    assertEquals(1, rateCycles.get());
                    releaseFirstRate.countDown();
                    assertTrue(secondRateStarted.await(2, TimeUnit.SECONDS));
                } finally {
                    releaseFirstRate.countDown();
                    fixedDelay.cancel(false);
                }

                fixedDelay
                        .quiescence()
                        .toCompletableFuture()
                        .get(2, TimeUnit.SECONDS);
                assertTrue(firstDelayThread
                        .get()
                        .isVirtual());
                assertTrue(secondDelayThread
                        .get()
                        .isVirtual());
                assertNotSame(firstDelayThread.get(), secondDelayThread.get());
                assertTrue(firstDelayThread
                        .get()
                        .getName()
                        .startsWith("consumer-"));
            } finally {
                releaseFirstRate.countDown();
                fixedRate.cancel(false);
            }

            fixedRate
                    .quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertFalse(rateOverlapped.get());
            assertTrue(firstRateThread
                    .get()
                    .isVirtual());
            assertTrue(secondRateThread
                    .get()
                    .isVirtual());
            assertNotSame(firstRateThread.get(), secondRateThread.get());
        }
    }

    @Test
    void closeWaitsForPeriodicBodyAndCancelsTheSeries() throws Exception {
        final CountDownLatch bodyStarted = new CountDownLatch(1);
        final CountDownLatch releaseBody = new CountDownLatch(1);
        final CountDownLatch closeFinished = new CountDownLatch(1);
        final AtomicReference<Throwable> closeFailure = new AtomicReference<>();
        final AtomicInteger cycles = new AtomicInteger();

        final ThreadFactory virtualFactory = Thread.ofVirtual()
                .factory();
        final VirtualScheduledThreadBuilderExecutor executor =
                new VirtualScheduledThreadBuilderExecutor(virtualFactory);

        try {
            final VirtualScheduledTask<Void> series = executor.scheduleAtFixedRate(() -> {
                cycles.incrementAndGet();
                bodyStarted.countDown();
                await(releaseBody);
            }, 0, 1, TimeUnit.MILLISECONDS);

            assertTrue(bodyStarted.await(2, TimeUnit.SECONDS));

            final Thread closing = Thread.ofPlatform()
                    .daemon()
                    .start(() -> {
                        try {
                            executor.close();
                        } catch (final Throwable failure) {
                            closeFailure.set(failure);
                        } finally {
                            closeFinished.countDown();
                        }
                    });

            assertFalse(closeFinished.await(50, TimeUnit.MILLISECONDS));
            releaseBody.countDown();
            assertTrue(closeFinished.await(2, TimeUnit.SECONDS));
            closing.join(2000);
            assertNull(closeFailure.get());
            assertThrows(CancellationException.class, series::get);
            series
                    .quiescence()
                    .toCompletableFuture()
                    .get(2, TimeUnit.SECONDS);
            assertEquals(1, cycles.get());
        } finally {
            releaseBody.countDown();
            executor.close();
        }
    }

    @Test
    void legacyPlatformExecutorStillInvokesAfterExecuteCallback() throws Exception {
        final CountDownLatch callbackCalled = new CountDownLatch(1);
        final AtomicBoolean callbackWasVirtual = new AtomicBoolean(true);

        try (ScheduledThreadBuilderExecutor legacy = new ScheduledThreadBuilderExecutor(1)) {
            legacy.addAfterExecuteConsumer((command, failure) -> {
                callbackWasVirtual.set(Thread.currentThread()
                        .isVirtual());
                callbackCalled.countDown();
            });

            final ScheduledFuture<Integer> result = legacy.schedule(
                    () -> 42, 0, TimeUnit.MILLISECONDS);

            assertEquals(42, result.get(2, TimeUnit.SECONDS));
            assertTrue(callbackCalled.await(2, TimeUnit.SECONDS));
            assertFalse(callbackWasVirtual.get());
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
