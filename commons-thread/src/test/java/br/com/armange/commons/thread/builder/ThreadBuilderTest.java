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
package br.com.armange.commons.thread.builder;

import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;
import br.com.armange.commons.thread.util.ThreadUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Diego Armange Costa
 * @since 2019-11-18 V1.0.0 (JDK 1.8)
 */
public class ThreadBuilderTest {

    private static final String ACTIVE_THREADS = "Active threads: %d";
    private static final String ACTIVE_THREADS_BEFORE_THREADS = "Active threads before test: %d";

    @Test
    public void threadBuilderWith15CorePoolSize() throws InterruptedException {
        final int activeCountBeforeTest = Thread.activeCount();
        final int corePoolSize = 15;
        final CountDownLatch started = new CountDownLatch(corePoolSize);
        final CountDownLatch release = new CountDownLatch(1);
        final Runnable execution = () -> {
            started.countDown();
            try {
                release.await();
            } catch (final InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        };

        out.println(String.format(ACTIVE_THREADS_BEFORE_THREADS, activeCountBeforeTest));

        final TimingRunnableBuilder builder = ThreadBuilder
                .newBuilder(corePoolSize)
                .setScheduling(execution);

        try {
            for (int index = 1; index < corePoolSize; index++) {
                builder.startAndBuildOther();
                builder.setExecution(execution);
            }

            final ExecutorResult<?> result = builder.start();

            assertThat(result.getExecutorService(), instanceOf(ScheduledThreadBuilderExecutor.class));

            final ScheduledThreadBuilderExecutor executor = (ScheduledThreadBuilderExecutor) result
                    .getExecutorService();

            assertTrue(started.await(10, TimeUnit.SECONDS));
            assertEquals(corePoolSize, executor.getActiveCount());

            ThreadUtil.sleepUnchecked(1500);

            final int activeCountAfterTest = Thread.activeCount();
            final int newThreadsCount = activeCountAfterTest - activeCountBeforeTest;

            out.println(String.format(ACTIVE_THREADS, newThreadsCount));

            assertThat(newThreadsCount, greaterThanOrEqualTo(5));
        } finally {
            release.countDown();
            final ScheduledThreadBuilderExecutor executor = builder.executor;
            if (executor != null) {
                executor.shutdownNow();
                try {
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (final InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    public void threadBuilderWithOneCorePoolSize() {
        final int activeCountBeforeTest = Thread.activeCount();
        final int corePoolSize = 1;

        out.println(String.format(ACTIVE_THREADS_BEFORE_THREADS, activeCountBeforeTest));

        final OpenExecutorResult<?> result = ThreadBuilder
                .newBuilder(corePoolSize)
                .setExecution(() -> ThreadUtil.sleepUnchecked(10000))
                .keepExecutorOpen()
                .start();

        final ScheduledThreadBuilderExecutor executor = (ScheduledThreadBuilderExecutor) result
                .getExecutorService();
        try {
            assertThat(executor, instanceOf(ScheduledThreadBuilderExecutor.class));
            assertEquals(corePoolSize, executor.getActiveCount());

            ThreadUtil.sleepUnchecked(1500);

            final int activeCountAfterTest = Thread.activeCount();
            final int newThreadsCount = activeCountAfterTest - activeCountBeforeTest;

            out.println(String.format(ACTIVE_THREADS, newThreadsCount));

            assertThat(newThreadsCount, lessThanOrEqualTo(1));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void singleCallableThread() throws Exception {
        final LocalCallableString localCallableString = new LocalCallableString();

        final ExecutorResult<String> result = ThreadBuilder
                .newBuilder()
                .setExecution(localCallableString)
                .start();

        assertEquals(LocalCallableString.THIS_IS_A_CALLABLE_THREAD,
                result.getFuture(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    public void newExecutionRunnableStartsLazilyAndRequestsShutdown() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final CommonRunnableBuilder builder = ThreadBuilder.newExecution(() -> ran.set(true));

        assertFalse(ran.get());

        final ExecutorResult<Void> result = builder.start();

        assertNull(result.getFutureResult(0));
        assertTrue(ran.get());
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void newExecutionCallableReturnsValueAndRequestsShutdown() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final CommonCallableBuilder<Integer> builder = ThreadBuilder.newExecution(() -> {
            ran.set(true);

            return 42;
        });

        assertFalse(ran.get());

        final ExecutorResult<Integer> result = builder.start();

        assertEquals(42, result.getFutureResult(0));
        assertTrue(ran.get());
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void startExecutionSubmitsRunnableAndCallableWithClosedResults() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final ExecutorResult<Void> runnable = ThreadBuilder.startExecution(() -> ran.set(true));
        final ExecutorResult<Integer> callable = ThreadBuilder.startExecution(() -> 42);

        assertNull(runnable.getFutureResult(0));
        assertTrue(ran.get());
        assertEquals(42, callable.getFutureResult(0));
        assertThat(runnable, instanceOf(ClosedExecutorResult.class));
        assertThat(callable, instanceOf(ClosedExecutorResult.class));
        assertTrue(runnable.getExecutorService().isShutdown());
        assertTrue(callable.getExecutorService().isShutdown());
    }

    @Test
    public void newSchedulingRunnableAcceptsDelayAndRequestsShutdown() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final TimingRunnableBuilder builder = ThreadBuilder
                .newScheduling(() -> ran.set(true))
                .setDelay(10);

        assertFalse(ran.get());

        final ExecutorResult<Void> result = builder.start();

        assertNull(result.getFutureResult(0));
        assertTrue(ran.get());
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void newSchedulingCallableAcceptsDelayAndReturnsValue() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final TimingCallableBuilder<Integer> builder = ThreadBuilder
                .newScheduling(() -> {
                    ran.set(true);

                    return 42;
                })
                .setDelay(10);

        assertFalse(ran.get());

        final ExecutorResult<Integer> result = builder.start();

        assertEquals(42, result.getFutureResult(0));
        assertTrue(ran.get());
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void startAsNamesTheRunnableThreadAndRequestsShutdown() throws Exception {
        final AtomicReference<String> threadName = new AtomicReference<>();
        final ExecutorResult<Void> result = ThreadBuilder
                .newExecution((Runnable) () -> threadName.set(Thread.currentThread().getName()))
                .startAs(() -> "pedido-worker");

        assertNull(result.getFutureResult(0));
        assertEquals("pedido-worker", threadName.get());
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void startAfterDelayReturnsTheCallableValueAndRequestsShutdown() throws Exception {
        final ExecutorResult<String> result = ThreadBuilder
                .newScheduling(() -> "relatorio")
                .startAfterDelay(10);

        assertEquals("relatorio", result.getFutureResult(0));
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void startAfterDelayRunsTheRunnableAndRequestsShutdown() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean();
        final ExecutorResult<Void> result = ThreadBuilder
                .newScheduling((Runnable) () -> ran.set(true))
                .startAfterDelay(10);

        assertNull(result.getFutureResult(0));
        assertTrue(ran.get());
        assertThat(result, instanceOf(ClosedExecutorResult.class));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void repeatAtFixedRateKeepsExecutorOpenUntilCallerClosesIt() throws Exception {
        final CountDownLatch cycles = new CountDownLatch(2);
        final ExecutorResult<Void> result = ThreadBuilder
                .newScheduling((Runnable) cycles::countDown)
                .repeatAtFixedRate(10);

        try {
            assertThat(result, instanceOf(OpenExecutorResult.class));
            assertFalse(result.getExecutorService().isShutdown());
            assertTrue(cycles.await(5, TimeUnit.SECONDS));
        } finally {
            result.getFuture(0).cancel(false);
            result.getExecutorService().close();
        }

        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void getFutureResultPropagatesTaskFailure() {
        final ExecutorResult<Integer> result = ThreadBuilder
                .newExecution((Callable<Integer>) () -> {
                    throw new IllegalStateException("falha");
                })
                .start();

        final ExecutionException failure = assertThrows(ExecutionException.class,
                () -> result.getFutureResult(0));

        assertThat(failure.getCause(), instanceOf(IllegalStateException.class));
        assertThrows(IndexOutOfBoundsException.class, () -> result.getFutureResult(1));
    }

    @Test
    public void getFutureResultPropagatesCancellation() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final ExecutorResult<Integer> result = ThreadBuilder
                .newExecution((Callable<Integer>) () -> {
                    started.countDown();
                    new CountDownLatch(1).await();

                    return 42;
                })
                .start();

        assertTrue(started.await(5, TimeUnit.SECONDS));
        assertTrue(result.getFuture(0).cancel(true));
        assertThrows(CancellationException.class, () -> result.getFutureResult(0));
    }

    @Test
    public void timingCallableThread() throws Exception {
        final LocalCallableString localCallableString = new LocalCallableString();

        final ExecutorResult<String> result = ThreadBuilder
                .newBuilder()
                .setScheduling(localCallableString)
                .setDelay(500)
                .start();

        ThreadUtil.sleepUnchecked(400);
        assertFalse(result.getFuture(0).isDone());

        assertEquals(LocalCallableString.THIS_IS_A_CALLABLE_THREAD,
                result.getFuture(0).get(5, TimeUnit.SECONDS));
    }

    private static class LocalCallableString implements Callable<String> {
        private static final String THIS_IS_A_CALLABLE_THREAD = "This is a callable thread.";

        @Override
        public String call() {
            ThreadUtil.sleepUnchecked(1000);

            return THIS_IS_A_CALLABLE_THREAD;
        }
    }

}
