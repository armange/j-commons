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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Abstraction for building threads with the main timing configuration options: Delay, Interval, Timeout.
 *
 * @param <S> the type of the object resulting from the execution of the thread.
 * @param <T> Execution type. {@link Runnable}, {@link Callable}.
 * @param <U> Builder. The Builder extending {@link AbstractThreadBuilder}
 * @author Diego Armange Costa
 * @see AbstractThreadBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractTimingThreadBuilder<S, T, U extends AbstractTimingThreadBuilder<S, T, U>>
        extends AbstractThreadBuilder<S, T, U> {

    protected Optional<Duration> timeout = Optional.empty();
    protected Optional<Duration> delay = Optional.empty();
    protected Optional<Duration> interval = Optional.empty();
    protected ThreadTimeConfig threadTimeConfig;

    protected AbstractTimingThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Sets the timeout value.<br>
     * The thread will have a timeout to complete its execution. After that time the thread will
     * be canceled({@link Future#cancel(boolean)}).
     *
     * @param milliseconds the timeout value in milliseconds.
     * @return the current thread builder.
     * @see #setMayInterruptIfRunning(boolean)
     */
    public U setTimeout(final long milliseconds) {
        timeout = Optional.of(Duration.ofMillis(milliseconds));

        threadTimeConfig = null;

        return getSelf();
    }

    /**
     * Sets the delay value.<br>
     * The thread will wait a predefined time before starting its execution.
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
     * Sets the repeating interval value.<br>
     * The thread will repeat its execution, waiting a predefined time before each new execution.
     *
     * @param milliseconds the repeating interval value in milliseconds.
     * @return the current thread builder.
     */
    public U setInterval(final long milliseconds) {
        interval = Optional.of(Duration.ofMillis(milliseconds));

        threadTimeConfig = null;

        return getSelf();
    }

    /**
     * Sets the thread-interrupting-flag.<br>
     * It is used during execution of cancellation by timeout feature.
     * The timeout feature uses a dedicated and secondary thread to perform the cancellation
     * of the primary thread. Note: the primary thread is the one that has been
     * configured with the timeout feature.
     *
     * @param flag true if the thread executing this task should be interrupted;
     *             otherwise, in-progress tasks are allowed to complete.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    public U setMayInterruptIfRunning(final boolean flag) {
        mayInterruptIfRunning = flag;

        return getSelf();
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     */
    @Override
    protected void createExecutorAndRunThread() {
        requireExecutionNonNull();

        readThreadTimeConfig();

        newExecutorServiceIfNull();

        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);

        runThread();
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

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     */
    @Override
    protected void runThread() {
        newExecutorResultIfNull();

        switch (threadTimeConfig) {
            case DELAY:
                runWithDelay();
                break;
            case DELAY_AND_INTERVAL:
            case INTERVAL:
                runWithDelayAndInterval();
                break;
            case DELAY_AND_TIMEOUT:
            case TIMEOUT:
                runWithDelayAndTimeout();
                break;
            case NO_SCHEDULE:
                runWithNoSchedule();
                break;
            case ALL_CONFIGURATION:
            default:
                runWithAllTimesControls();
                break;
        }
    }

    private void runWithNoSchedule() {
        final Holder<Future<S>> futureHolder = Holder.empty();

        executor.addAfterExecuteConsumer(handleException(futureHolder));
        futureHolder.set(submit());
        newExecutorResultIfNull();
        executorResult.getFutures().add(futureHolder.get());
    }

    private void runWithDelay() {
        final Holder<Future<S>> futureHolder = Holder.empty();

        executor.addAfterExecuteConsumer(handleException(futureHolder));
        futureHolder.set(schedule(handleDelay(), TimeUnit.MILLISECONDS));
        newExecutorResultIfNull();
        executorResult.getFutures().add(futureHolder.get());
    }

    private void runWithDelayAndTimeout() {
        final Holder<ScheduledFuture<S>> futureHolder = Holder.empty();

        executor.addAfterExecuteConsumer(handleException(futureHolder));
        futureHolder.set(schedule(handleDelay(), TimeUnit.MILLISECONDS));

        final ExecutorResult<S> timeoutExecutorResult = handleInterruption(futureHolder);

        newExecutorResultIfNull();
        executorResult.getFutures().add(futureHolder.get());
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private void runWithDelayAndInterval() {
        final Holder<ScheduledFuture<S>> futureHolder = Holder.empty();

        executor.addAfterExecuteConsumer(handleException(futureHolder));
        futureHolder.set(scheduleAtFixedRate(handleDelay(),
                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS));
        newExecutorResultIfNull();
        executorResult.getFutures().add(futureHolder.get());
    }

    private void runWithAllTimesControls() {
        final Holder<ScheduledFuture<S>> futureHolder = Holder.empty();

        executor.addAfterExecuteConsumer(handleException(futureHolder));
        futureHolder.set(scheduleAtFixedRate(handleDelay(),
                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS));

        final ExecutorResult<S> timeoutExecutorResult = handleInterruption(futureHolder);

        newExecutorResultIfNull();
        executorResult.getFutures().add(futureHolder.get());
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    /**
     * It schedules the thread, configuring the delay parameter.
     *
     * @param delay the delay before thread execution.
     * @param unit  the time unit of the delay.
     * @return the scheduled-future-object of the thread.
     * @see ScheduledThreadPoolExecutor#schedule(Callable, long, TimeUnit)
     * @see ScheduledFuture
     */
    @SuppressWarnings({"unchecked"})
    protected ScheduledFuture<S> schedule(final long delay, final TimeUnit unit) {
        final ScheduledFuture<S> future;

        switch (executionType) {
            case CALLABLE:
                future = executor.schedule((Callable<S>) execution, delay, unit);

                break;
            case RUNNABLE:
            default:
                future = (ScheduledFuture<S>) executor.schedule((Runnable) execution, delay, unit);

                break;
        }

        return future;
    }

    /**
     * It schedules the thread, configuring the delay and period parameters.
     *
     * @param delay  the delay before thread execution.
     * @param period the interval between thread executions.
     * @param unit   the time unit of the delay and period.
     * @return the scheduled-future-object of the thread.
     * @see ScheduledThreadPoolExecutor#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     * @see ScheduledFuture
     */
    @SuppressWarnings({"unchecked"})
    protected ScheduledFuture<S> scheduleAtFixedRate(final long delay,
                                                     final long period, final TimeUnit unit) {
        final ScheduledFuture<S> future;

        switch (executionType) {
            case CALLABLE:
                throw new UnsupportedOperationException(ExecutionType.CALLABLE.name());
            case RUNNABLE:
            default:
                future = (ScheduledFuture<S>) executor
                        .scheduleAtFixedRate((Runnable) execution, delay, period, unit);

                break;
        }

        return future;
    }

    private long handleDelay() {
        return delay.orElseGet(() -> Duration.ofMillis(0)).toMillis();
    }

    private ExecutorResult<S> handleInterruption(final Holder<ScheduledFuture<S>> futureHolder) {
        final ScheduledFuture<S> scheduledFuture = futureHolder.get();
        final ScheduledThreadBuilderExecutor localExec = new ScheduledThreadBuilderExecutor(1);

        localExec.addAfterExecuteConsumer(handleException(futureHolder));
        localExec.schedule(cancelFuture(scheduledFuture), timeout
                        .orElse(Duration.ofMillis(0)).toMillis(),
                TimeUnit.MILLISECONDS);

        final ExecutorResult<S> timeoutExecutorResult = new ExecutorResult<>(localExec);

        timeoutExecutorResult.getFutures().add(scheduledFuture);

        return timeoutExecutorResult;
    }

    private Runnable cancelFuture(final ScheduledFuture<S> future) {
        return () -> {
            if (!future.isDone() && !future.isCancelled())
                future.cancel(mayInterruptIfRunning);
        };
    }

    /**
     * Class that enumerates the types of thread configuration that are supported by this builder.
     */
    protected enum ThreadTimeConfig {
        NO_SCHEDULE, DELAY, TIMEOUT, INTERVAL,
        DELAY_AND_TIMEOUT, DELAY_AND_INTERVAL, ALL_CONFIGURATION;
    }

}
