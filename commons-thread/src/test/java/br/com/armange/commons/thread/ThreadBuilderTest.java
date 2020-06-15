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

import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

/**
 * @author Diego Armange Costa
 * @since 2019-11-18 V1.0.0 (JDK 1.8)
 */
public class ThreadBuilderTest {
    private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException();

    private static class LocalRunnable implements Runnable {
        @Override
        public void run() {
        }
    }

    private static class LocalRunnableWithException implements Runnable {
        @Override
        public void run() {
            throw RUNTIME_EXCEPTION;
        }
    }

    private static class LazyRunnableWithException implements Runnable {
        private boolean shouldFail;

        @Override
        public void run() {
            if (!shouldFail) {
                shouldFail = true;
            } else {
                throw RUNTIME_EXCEPTION;
            }
        }
    }

    private static class ThrowableConsumer implements Consumer<Throwable> {
        @Override
        public void accept(final Throwable t) {
            System.out.println("Exception tested successful: " + t);
        }
    }

    private static class AfterExecuteConsumer implements BiConsumer<Runnable, Throwable> {
        @Override
        public void accept(final Runnable t, final Throwable u) {
            System.out.println("After-Execute consumer tested successful.");
        }
    }

    private volatile Thread lastThread;

    @Test
    public void noSchedule() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(2000);

        Mockito.verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void noScheduleCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setExecution(localRunnableWithException)
                .setSilentInterruption(true)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        ThreadUtil.sleepUnchecked(2000);

        Mockito.verify(localRunnableWithException, times(1)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useDelay() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(2000)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(0)).run();

        ThreadUtil.sleepUnchecked(3000);

        Mockito.verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void useDelayCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(2000)
                .setExecution(localRunnableWithException)
                .setSilentInterruption(true)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnableWithException, times(0)).run();

        ThreadUtil.sleepUnchecked(3000);

        Mockito.verify(localRunnableWithException, times(1)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void cancelByTimeout() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(1000)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(() -> {
                    ThreadUtil.sleepUnchecked(2000);
                    localRunnable.run();
                })
                .start();

        ThreadUtil.sleepUnchecked(2000);

        Mockito.verify(localRunnable, times(0)).run();

        ThreadUtil.sleepUnchecked(3000);

        Mockito.verify(localRunnable, times(0)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void runBeforeTimeout() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(3000)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(2000);

        Mockito.verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void runBeforeTimeoutCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(3000)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setExecution(localRunnableWithException)
                .start();

        ThreadUtil.sleepUnchecked(2000);

        Mockito.verify(localRunnableWithException, times(1)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void useInterval() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setInterval(1000)
                .setSilentInterruption(true)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(500);

        Mockito.verify(localRunnable, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void useIntervalCaughtException() {
        final LazyRunnableWithException lazyRunnableWithException = spy(new LazyRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setExecution(lazyRunnableWithException)
                .setSilentInterruption(true)
                .start();

        ThreadUtil.sleepUnchecked(1500);

        Mockito.verify(lazyRunnableWithException, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(lazyRunnableWithException, times(2)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(lazyRunnableWithException, times(2)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void useIntervalAndDelay() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(1000)
                .setInterval(1000)
                .setExecution(localRunnable)
                .setSilentInterruption(true)
                .start();

        ThreadUtil.sleepUnchecked(1500);

        Mockito.verify(localRunnable, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void useIntervalAndDelayAndCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(1000)
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setExecution(localRunnableWithException)
                .setSilentInterruption(true)
                .start();

        ThreadUtil.sleepUnchecked(2500);

        Mockito.verify(localRunnableWithException, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnableWithException, times(1)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void useTimeoutAndInterval() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(3000)
                .setInterval(1000)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(500);

        Mockito.verify(localRunnable, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        ThreadUtil.sleepUnchecked(1000);
        Mockito.verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void cancelByTimeoutUsingInterval() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(2000)
                .setInterval(100)
                .setMayInterruptIfRunning(true)
                .setExecution(() -> {
                    ThreadUtil.sleepUnchecked(4000);
                    localRunnable.run();
                })
                .setSilentInterruption(true)
                .start();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(0)).run();

        ThreadUtil.sleepUnchecked(3000);

        Mockito.verify(localRunnable, times(0)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void useTimeoutAndIntervalCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(3000)
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(localRunnableWithException)
                .start();

        ThreadUtil.sleepUnchecked(1500);

        Mockito.verify(localRunnableWithException, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnableWithException, times(1)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void useAllDelayAndTimeout() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());

        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(1000)
                .setTimeout(2000)
                .setMayInterruptIfRunning(true)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(500);

        Mockito.verify(localRunnable, times(0)).run();

        ThreadUtil.sleepUnchecked(600);

        Mockito.verify(localRunnable, times(1)).run();

        ThreadUtil.sleepUnchecked(1100);

        Mockito.verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void useAllTimeControlsAndCaughtException() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(1000)
                .setTimeout(4000)
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(localRunnableWithException)
                .start();

        ThreadUtil.sleepUnchecked(2500);

        Mockito.verify(localRunnableWithException, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnableWithException, times(1)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void useAllTimeControls() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(1000)
                .setTimeout(4000)
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(1500);

        Mockito.verify(localRunnable, times(1)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        ThreadUtil.sleepUnchecked(1000);
        Mockito.verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void silentCancellationException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder(2)
                .setDelay(5000)
                .setExecution(localRunnableWithException)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setSilentInterruption(true)
                .start();

        result.getExecutorService().shutdownNow();

        Mockito.verify(localRunnableWithException, times(0)).run();
        Mockito.verify(throwableConsumer, times(0)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void throwingInterruptedException() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ExecutorResult result = ThreadBuilder
                .newBuilder(2)
                .setExecution(() -> ThreadUtil.sleepUnchecked(5000))
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        ThreadUtil.sleepUnchecked(1100);

        result.getExecutorService().shutdownNow();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnableWithException, times(0)).run();
        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void throwingCancellationException() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setExecution(() -> ThreadUtil.sleepUnchecked(5000))
                .setTimeout(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        ThreadUtil.sleepUnchecked(1500);

        Mockito.verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void callingAfterExecutionConsumer() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final AfterExecuteConsumer afterExecuteConsumer = spy(new AfterExecuteConsumer());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setExecution(localRunnable)
                .setAfterExecuteConsumer(afterExecuteConsumer)
                .start();

        ThreadUtil.sleepUnchecked(1100);

        Mockito.verify(localRunnable, times(1)).run();
        Mockito.verify(afterExecuteConsumer, times(1)).accept(any(), any());
        assertThat(result, hasProperty("executorService", Matchers.allOf(
                notNullValue(),
                IsInstanceOf.instanceOf(ScheduledCaughtExecutorService.class))));

        assertThat(
                ScheduledCaughtExecutorService.class.cast(result.getExecutorService()).getAfterExecuteConsumers(),
                notNullValue());

        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void useIntervalForFiveTimes() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(500)
                .setInterval(100)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(410);

        Mockito.verify(localRunnable, Mockito.atLeast(4)).run();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, Mockito.atMost(7)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void buildTwoThreadsUsingTheSameBuilder() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setTimeout(1000)
                .setExecution(localRunnable)
                .startAndBuildOther()
                .setTimeout(1000)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(1000);

        Mockito.verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(2)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(2)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(2));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(),
                Matchers.hasItem(
                        hasProperty("futures", hasSize(1))));
    }


    @Test
    public void cancelDelayBeforeExecution() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult result = ThreadBuilder
                .newBuilder()
                .setDelay(3000)
                .setExecution(localRunnable)
                .start();

        ThreadUtil.sleepUnchecked(1000);

        result.getExecutorService().shutdownNow();

        ThreadUtil.sleepUnchecked(3000);

        Mockito.verify(localRunnable, times(0)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }


    @Test
    public void threadName() {
        final String threadName = "Thread name";

        ThreadBuilder
                .newBuilder()
                .setThreadNameSupplier(() -> threadName)
                .setExecution(() -> lastThread = Thread.currentThread())
                .start();

        ThreadUtil.sleepUnchecked(1000);

        Assert.assertEquals(threadName, lastThread.getName());
    }


    @Test
    public void threadPriority() {
        final int threadPriority = 4;

        ThreadBuilder
                .newBuilder()
                .setThreadPrioritySupplier(() -> threadPriority)
                .setExecution(() -> lastThread = Thread.currentThread())
                .start();

        ThreadUtil.sleepUnchecked(1000);

        Assert.assertEquals(threadPriority, lastThread.getPriority());
    }
}