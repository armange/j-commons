package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.core.ExceptionCatcherThreadFactory;
import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TryAsyncLifecycleTest {

    @Test
    void executeReturnsBeforeTaskAndTerminatesAfterFinalizer() throws InterruptedException {
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch releaseTask = new CountDownLatch(1);
        final CountDownLatch finalizerStarted = new CountDownLatch(1);
        final CountDownLatch releaseFinalizer = new CountDownLatch(1);
        final TrackingBuilder builder = new TrackingBuilder(() -> {
            taskStarted.countDown();
            await(releaseTask);
        });

        builder.finallyAsync(() -> {
            finalizerStarted.countDown();
            await(releaseFinalizer);
        });

        try {
            builder.execute();

            assertTrue(taskStarted.await(5, TimeUnit.SECONDS));
            assertTrue(builder.executor.isShutdown());
            assertFalse(builder.executor.isTerminated());

            releaseTask.countDown();

            assertTrue(finalizerStarted.await(5, TimeUnit.SECONDS));
            assertFalse(builder.executor.isTerminated());
        } finally {
            releaseTask.countDown();
            releaseFinalizer.countDown();
        }

        assertTrue(builder.executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void callableResultPrecedesFinalizer() throws InterruptedException {
        final List<String> events = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch finalized = new CountDownLatch(1);

        TryAsyncBuilder.tryAsync(() -> {
                    events.add("task");

                    return 42;
                }, result -> events.add("result:" + result))
                .finallyAsync(() -> {
                    events.add("finalizer");
                    finalized.countDown();
                })
                .execute();

        assertTrue(finalized.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("task", "result:42", "finalizer"), events);
    }

    @Test
    void runnableCatcherPrecedesFinalizer() throws InterruptedException {
        final List<String> events = Collections.synchronizedList(new ArrayList<>());
        final AtomicReference<Throwable> caught = new AtomicReference<>();
        final CountDownLatch finalized = new CountDownLatch(1);

        TryAsyncBuilder.tryAsync(() -> {
                    events.add("task");
                    throw new IOException("task failed");
                })
                .addCatcher(AsyncRuntimeException.class, failure -> {
                    caught.set(failure);
                    events.add("catcher");
                })
                .finallyAsync(() -> {
                    events.add("finalizer");
                    finalized.countDown();
                })
                .execute();

        assertTrue(finalized.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("task", "catcher", "finalizer"), events);
        assertInstanceOf(AsyncRuntimeException.class, caught.get().getCause());
        assertInstanceOf(IOException.class, caught.get().getCause().getCause());
    }

    @Test
    void callableFailureStillRunsFinalizer() throws InterruptedException {
        final AtomicReference<Throwable> caught = new AtomicReference<>();
        final CountDownLatch finalized = new CountDownLatch(1);

        TryAsyncBuilder.tryAsync((Callable<Integer>) () -> {
                    throw new IOException("call failed");
                }, ignored -> {
                    throw new AssertionError("result consumer must not run");
                })
                .addCatcher(IOException.class, caught::set)
                .finallyAsync(finalized::countDown)
                .execute();

        assertTrue(finalized.await(5, TimeUnit.SECONDS));
        assertInstanceOf(IOException.class, caught.get().getCause());
    }

    @Test
    void closesResourcesFromEachOverload() throws InterruptedException {
        final CountDownLatch closed = new CountDownLatch(4);
        final AtomicInteger calls = new AtomicInteger();
        final Closeable single = () -> {
            calls.incrementAndGet();
            closed.countDown();
        };
        final Closeable first = () -> {
            calls.incrementAndGet();
            closed.countDown();
        };
        final Closeable second = () -> {
            calls.incrementAndGet();
            closed.countDown();
        };
        final Closeable mapped = () -> {
            calls.incrementAndGet();
            closed.countDown();
        };
        final Map<Object, Closeable> map = new LinkedHashMap<>();

        map.put("mapped", mapped);

        TryAsyncBuilder.tryAsync(single, resource -> {
        }).execute();
        TryAsyncBuilder.tryAsync(resources -> {
        }, first, second).execute();
        TryAsyncBuilder.tryAsync(map, resources -> {
        }).execute();

        assertTrue(closed.await(5, TimeUnit.SECONDS));
        assertEquals(4, calls.get());
    }

    @Test
    void failingResultConsumerStillRunsFinalizer() throws InterruptedException {
        final CountDownLatch finalized = new CountDownLatch(1);

        TryAsyncBuilder.tryAsync(() -> 7, result -> {
                    throw new IllegalStateException("consumer failed");
                })
                .finallyAsync(finalized::countDown)
                .execute();

        assertTrue(finalized.await(5, TimeUnit.SECONDS));
    }

    @Test
    void failingFinalizerStillTerminatesExecutor() throws InterruptedException {
        final AtomicBoolean laterFinalizerRan = new AtomicBoolean();
        final TrackingBuilder builder = new TrackingBuilder(() -> {
        });

        builder.finallyAsync(() -> {
            throw new IllegalStateException("finalizer failed");
        });
        builder.finallyAsync(() -> laterFinalizerRan.set(true));
        builder.execute();

        assertTrue(builder.executor.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(laterFinalizerRan.get());
    }

    @Test
    void rejectionShutsDownPrestartedExecutorWithoutRunningTaskOrFinalizer()
            throws InterruptedException {
        final AtomicBoolean taskRan = new AtomicBoolean();
        final AtomicBoolean finalizerRan = new AtomicBoolean();
        final RejectingBuilder builder = new RejectingBuilder(taskRan);

        builder.finallyAsync(() -> finalizerRan.set(true));

        assertThrows(RejectedExecutionException.class, builder::execute);
        assertTrue(builder.workerPrestarted);
        assertFalse(taskRan.get());
        assertFalse(finalizerRan.get());
        assertTrue(builder.executor.isShutdown());
        assertTrue(builder.executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    private static void await(final CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test gate");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static class TrackingBuilder extends AbstractTryAsyncBuilder<TrackingBuilder> {
        private final RunnableWithException task;
        private ScheduledThreadBuilderExecutor executor;

        private TrackingBuilder(final RunnableWithException task) {
            this.task = task;
        }

        @Override
        public void execute() {
            execute(task);
        }

        @Override
        ScheduledThreadBuilderExecutor createExecutor() {
            executor = super.createExecutor();

            return executor;
        }
    }

    private static final class RejectingBuilder extends AbstractTryAsyncBuilder<RejectingBuilder> {
        private final AtomicBoolean taskRan;
        private ScheduledThreadBuilderExecutor executor;
        private boolean workerPrestarted;

        private RejectingBuilder(final AtomicBoolean taskRan) {
            this.taskRan = taskRan;
        }

        @Override
        public void execute() {
            execute((RunnableWithException) () -> taskRan.set(true));
        }

        @Override
        ScheduledThreadBuilderExecutor createExecutor() {
            executor = new ScheduledThreadBuilderExecutor(1, new ExceptionCatcherThreadFactory()) {
                @Override
                public void execute(final Runnable runnable) {
                    throw new RejectedExecutionException("rejected by test executor");
                }
            };
            workerPrestarted = executor.prestartCoreThread();

            return executor;
        }
    }
}
