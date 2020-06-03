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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import br.com.armange.commons.thread.core.ScheduledCaughtExecutorService;
import br.com.armange.commons.thread.message.ExceptionMessage;

public abstract class AbstractTimingThreadBuilder<S, T, U extends AbstractTimingThreadBuilder<S, T, U>>
        extends AbstractThreadBuilder<S, T, U> {

    protected AbstractTimingThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    protected static enum ThreadTimeConfig {
        NO_SCHEDULE, DELAY, TIMEOUT, INTERVAL, DELAY_AND_TIMEOUT, DELAY_AND_INTERVAL, ALL_CONFIGURATION;
    }

    /**
     * 1000 milliseconds as a minimal delay.
     */
    public static final long MINIMAL_REQUIRED_DELAY = 1000;
    protected Optional<Duration> timeout = Optional.empty();
    protected Optional<Duration> delay = Optional.empty();
    protected Optional<Duration> interval = Optional.empty();
    protected ThreadTimeConfig threadTimeConfig;

    /**
     * Sets the timeout value.
     * 
     * @param milliseconds the timeout value in milliseconds.
     * @return the current thread builder.
     */
    public U setTimeout(final long milliseconds) {
        timeout = Optional.of(Duration.ofMillis(milliseconds));

        threadTimeConfig = null;

        return getSelf();
    }

    /**
     * Sets the delay value.
     * 
     * @param milliseconds the delay value in milliseconds.
     * @return the current thread builder.
     */
    public U setDelay(final long milliseconds) {
        delay = Optional.of(Duration.ofMillis(milliseconds));

        threadTimeConfig = null;

        return getSelf();
    }

    /**
     * Sets the repeating interval value.
     * 
     * @param milliseconds the repeating interval value in milliseconds.
     * @return the current thread builder.
     */
    public U setInterval(final long milliseconds) {
        interval = Optional.of(Duration.ofMillis(milliseconds));

        threadTimeConfig = null;

        return getSelf();
    }

    @Override
    protected void createExecutorAndRunThread() {
        requireExecutionNonNull();

        readThreadTimeConfig();

        executor = new ScheduledCaughtExecutorService(corePoolSize, getThreadFactory());

        runThread();

        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
    }

    private void readThreadTimeConfig() {
        if (this.threadTimeConfig == null)
            if (noSchedule()) {
                this.threadTimeConfig = ThreadTimeConfig.NO_SCHEDULE;
            } else if (onlyDelay()) {
                this.threadTimeConfig = ThreadTimeConfig.DELAY;
            } else if (onlyTimeout()) {
                this.threadTimeConfig = ThreadTimeConfig.TIMEOUT;
            } else if (onlyInterval()) {
                this.threadTimeConfig = ThreadTimeConfig.INTERVAL;
            } else if (delayAndTimeout()) {
                this.threadTimeConfig = ThreadTimeConfig.DELAY_AND_TIMEOUT;
            } else if (delayAndInterval()) {
                this.threadTimeConfig = ThreadTimeConfig.DELAY_AND_INTERVAL;
            } else /* All */ {
                this.threadTimeConfig = ThreadTimeConfig.ALL_CONFIGURATION;
            }
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

    @Override
    protected void runThread() {
        switch (threadTimeConfig) {
        case DELAY:
            runWithDelay();
            break;
        case DELAY_AND_INTERVAL:
            runWithDelayAndInterval();
            break;
        case DELAY_AND_TIMEOUT:
            runWithDelayAndTimeout();
            break;
        case INTERVAL:
            runWithDelayAndInterval();
            break;
        case NO_SCHEDULE:
            runWithNoSchedule();
            break;
        case TIMEOUT:
            runWithDelayAndTimeout();
            break;
        case ALL_CONFIGURATION:
            runWithAllTimesControls();
            break;
        default:
            throw new IllegalStateException(
                    ExceptionMessage.ILLEGAL_STATE_THREAD_TIMER_CONFIG.format(threadTimeConfig));
        }
    }

    private void runWithNoSchedule() {
        final Future<S> future = schedule(handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithDelay() {
        final ScheduledFuture<S> future = schedule(handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithDelayAndTimeout() {
        final ScheduledFuture<S> future = schedule(handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult<S> timeoutExecutorResult = handleInterruption(future);

        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private void runWithDelayAndInterval() {
        final ScheduledFuture<S> future = scheduleAtFixedRate(handleDelay(),
                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void newExecutorResultIfNull() {
        executorResult = executorResult == null ? new ExecutorResult<>(executor) : executorResult;
    }

    private void runWithAllTimesControls() {
        final ScheduledFuture<S> future = scheduleAtFixedRate(handleDelay(),
                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult<S> timeoutExecutorResult = handleInterruption(future);

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

    private ExecutorResult<S> handleInterruption(final ScheduledFuture<S> future) {
        final ScheduledCaughtExecutorService localExecutor = new ScheduledCaughtExecutorService(1);

        localExecutor.addAfterExecuteConsumer(handleException(future));
        localExecutor.schedule(cancelFuture(future), timeout.orElse(Duration.ofMillis(0)).toMillis(),
                TimeUnit.MILLISECONDS);

        final ExecutorResult<S> timeoutExecutorResult = new ExecutorResult<>(localExecutor);

        timeoutExecutorResult.getFutures().add(future);

        return timeoutExecutorResult;
    }

    private Runnable cancelFuture(final ScheduledFuture<S> future) {
        return () -> {
            if (!future.isDone() && !future.isCancelled())
                future.cancel(mayInterruptIfRunning);
        };
    }
}
