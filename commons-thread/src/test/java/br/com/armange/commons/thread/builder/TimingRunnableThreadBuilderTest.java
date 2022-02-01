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
import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TimingRunnableThreadBuilderTest {
    private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException();

    @Test
    public void noSchedule() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnable)
                .start();

        sleepUnchecked(500);

        verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void noScheduleCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnableWithException)
                .setSilentInterruption(true)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        sleepUnchecked(500);

        verify(localRunnableWithException, times(1)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useDelay() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setScheduling(localRunnable)
                .setDelay(1000)
                .start();

        sleepUnchecked(500);

        verify(localRunnable, times(0)).run();

        sleepUnchecked(1500);

        verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useDelayCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnableWithException)
                .setDelay(1000)
                .setSilentInterruption(true)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        sleepUnchecked(500);

        verify(localRunnableWithException, times(0)).run();

        sleepUnchecked(1500);

        verify(localRunnableWithException, times(1)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void cancelByTimeout() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1).setExecution(() -> {
                    sleepUnchecked(1000);
                    localRunnable.run();
                })
                .setTimeout(500)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(1000);

        verify(localRunnable, times(0)).run();

        sleepUnchecked(1500);

        verify(localRunnable, times(0)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void runBeforeTimeout() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnable)
                .setTimeout(1500)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(1000);

        verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void runBeforeTimeoutCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnableWithException)
                .setTimeout(1500)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .start();

        sleepUnchecked(1000);

        verify(localRunnableWithException, times(1)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void useInterval() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnable)
                .setInterval(1000)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(500);

        verify(localRunnable, times(1)).run();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useIntervalCaughtException() {
        final LazyRunnableWithException lazyRunnableWithException = spy(new LazyRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(lazyRunnableWithException)
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(500);

        verify(lazyRunnableWithException, times(1)).run();

        sleepUnchecked(1000);

        verify(lazyRunnableWithException, times(2)).run();
        verify(throwableConsumer, times(1)).accept(any());

        sleepUnchecked(1000);

        verify(lazyRunnableWithException, times(2)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useIntervalAndDelay() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnable)
                .setDelay(500)
                .setInterval(1000)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(1000);

        verify(localRunnable, times(1)).run();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useIntervalAndDelayAndCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnableWithException)
                .setDelay(500)
                .setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(1000);

        verify(localRunnableWithException, times(1)).run();

        sleepUnchecked(1000);

        verify(localRunnableWithException, times(1)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useTimeoutAndInterval() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnable)
                .setTimeout(3000)
                .setInterval(1000)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(500);

        verify(localRunnable, times(1)).run();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        sleepUnchecked(1000);
        verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void cancelByTimeoutUsingInterval() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(() -> {
                    sleepUnchecked(3000);
                    localRunnable.run();
                })
                .setTimeout(1500)
                .setInterval(100)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(500);

        verify(localRunnable, times(0)).run();

        sleepUnchecked(1500);

        verify(localRunnable, times(0)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void useTimeoutAndIntervalCaughtException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnableWithException)
                .setTimeout(1500)
                .setInterval(500)
                .setUncaughtExceptionConsumer(throwableConsumer)
                .setMayInterruptIfRunning(true)
                .setSilentInterruption(true)
                .start();

        sleepUnchecked(600);

        verify(localRunnableWithException, times(1)).run();

        sleepUnchecked(600);

        verify(localRunnableWithException, times(1)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void useAllDelayAndTimeout() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder
                .newBuilder(1)
                .setExecution(localRunnable)
                .setDelay(500)
                .setTimeout(2000)
                .setMayInterruptIfRunning(true)
                .start();

        sleepUnchecked(100);

        verify(localRunnable, times(0)).run();

        sleepUnchecked(600);

        verify(localRunnable, times(1)).run();

        sleepUnchecked(600);

        verify(localRunnable, times(1)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void useAllTimeControlsAndCaughtException() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1)
                .setExecution(localRunnableWithException).setDelay(1000).setTimeout(4000).setInterval(1000)
                .setUncaughtExceptionConsumer(throwableConsumer).setMayInterruptIfRunning(true)
                .setSilentInterruption(true).start();

        sleepUnchecked(2500);

        verify(localRunnableWithException, times(1)).run();

        sleepUnchecked(1000);

        verify(localRunnableWithException, times(1)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void useAllTimeControls() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1).setExecution(localRunnable)
                .setDelay(1000).setTimeout(4000).setInterval(1000).setUncaughtExceptionConsumer(throwableConsumer)
                .setMayInterruptIfRunning(true).setSilentInterruption(true).start();

        sleepUnchecked(1500);

        verify(localRunnable, times(1)).run();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();

        result.getExecutorService().shutdownNow();

        sleepUnchecked(1000);
        verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void silentCancellationException() {
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(2).setExecution(localRunnableWithException)
                .setDelay(5000).setUncaughtExceptionConsumer(throwableConsumer).setSilentInterruption(true).start();

        result.getExecutorService().shutdownNow();

        verify(localRunnableWithException, times(0)).run();
        verify(throwableConsumer, times(0)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void throwingInterruptedException() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final LocalRunnableWithException localRunnableWithException = spy(new LocalRunnableWithException());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(2).setExecution(() -> sleepUnchecked(5000))
                .setUncaughtExceptionConsumer(throwableConsumer).start();

        sleepUnchecked(1100);

        result.getExecutorService().shutdownNow();

        sleepUnchecked(1000);

        verify(localRunnableWithException, times(0)).run();
        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void throwingCancellationException() {
        final ThrowableConsumer throwableConsumer = spy(new ThrowableConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1)
                .setExecution(() -> sleepUnchecked(5000)).setTimeout(1000)
                .setUncaughtExceptionConsumer(throwableConsumer).start();

        sleepUnchecked(1500);

        verify(throwableConsumer, times(1)).accept(any());
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void callingAfterExecutionConsumer() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final AfterExecuteConsumer afterExecuteConsumer = spy(new AfterExecuteConsumer());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1).setExecution(localRunnable)
                .setAfterExecuteConsumer(afterExecuteConsumer).start();

        sleepUnchecked(1100);

        verify(localRunnable, times(1)).run();
        verify(afterExecuteConsumer, times(1)).accept(any(), any());
        assertThat(result, hasProperty("executorService", Matchers.allOf(notNullValue(),
                IsInstanceOf.instanceOf(ScheduledThreadBuilderExecutor.class))));

        assertThat(ScheduledThreadBuilderExecutor.class.cast(result.getExecutorService()).getAfterExecuteConsumers(),
                notNullValue());

        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void useIntervalForFiveTimes() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1).setExecution(localRunnable)
                .setTimeout(500).setInterval(100).setMayInterruptIfRunning(true).setSilentInterruption(true).start();

        sleepUnchecked(410);

        verify(localRunnable, atLeast(4)).run();

        sleepUnchecked(1000);

        verify(localRunnable, atMost(7)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(1)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(1));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void buildTwoThreadsUsingTheSameBuilder() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1).setExecution(localRunnable)
                .setTimeout(1000).startAndBuildOther().setTimeout(1000).setExecution(localRunnable).start();

        sleepUnchecked(1000);

        verify(localRunnable, times(2)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(2)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(2)));
        assertThat(result.getTimeoutExecutorResults(), hasSize(2));
        assertThat(result.getTimeoutExecutorResults(),
                hasItem(hasProperty("executorService", notNullValue())));
        assertThat(result.getTimeoutExecutorResults(), hasItem(hasProperty("futures", hasSize(1))));
    }

    @Test
    public void cancelDelayBeforeExecution() {
        final LocalRunnable localRunnable = spy(new LocalRunnable());
        final ExecutorResult<?> result = TimingRunnableThreadBuilder.newBuilder(1).setExecution(localRunnable)
                .setDelay(3000).start();

        sleepUnchecked(1000);

        result.getExecutorService().shutdownNow();

        sleepUnchecked(3000);

        verify(localRunnable, times(0)).run();
        assertThat(result, hasProperty("executorService", notNullValue()));
        assertThat(result, hasProperty("futures", hasSize(1)));
        assertThat(result, hasProperty("timeoutExecutorResults", hasSize(0)));
    }

    @Test
    public void scheduleRunnableMethod() {
        final TimingRunnableThreadBuilderTestArtifact<?> builder = spy(TimingRunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        final Future<?> schedule = builder.schedule(0, TimeUnit.MILLISECONDS);

        assertNotNull(schedule);
    }

    @Test
    public void scheduleAtFixedRateRunnableMethod() {
        final TimingRunnableThreadBuilderTestArtifact<?> builder = spy(TimingRunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        final Future<?> scheduleAtFixedRate = builder.scheduleAtFixedRate(0, 1, TimeUnit.MILLISECONDS);

        assertNotNull(scheduleAtFixedRate);
    }

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
            System.out.println("Exception tested successfull: " + t);
        }
    }

    private static class AfterExecuteConsumer implements BiConsumer<Runnable, Throwable> {
        @Override
        public void accept(final Runnable t, final Throwable u) {
            System.out.println("After-Execute consumer tested successfull.");
        }
    }
}
