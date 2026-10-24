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
import java.util.concurrent.*;

/**
 * Base for scheduled platform-thread builders with delay, fixed-rate interval
 * and cancellation deadline. A timeout uses the task executor, with at least
 * two workers, to schedule cancellation. A deadline is measured from
 * submission, including any delay. The caller chooses delay, timeout and
 * interval according to the intended task behavior; a timeout can expire
 * before a delayed task starts. Timing settings belong to one submission and
 * return to their defaults after it is accepted.
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

    protected AbstractTimingThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Sets a deadline measured from submission, not from the start of the
     * task body. At that deadline the task executor attempts
     * {@link Future#cancel(boolean)} using the interruption flag captured for
     * this task.
     * Cancellation does not prove the body stopped. The caller is responsible
     * for choosing this deadline relative to any delay and expected task
     * duration. If the task completes before the deadline, its scheduled
     * timeout action is not cancelled automatically and can remain scheduled
     * until the deadline. Consequently, an executor that has received
     * {@code shutdown()} can remain active until that action runs.
     *
     * @param milliseconds the timeout value in milliseconds.
     * @return the current thread builder.
     * @see #setMayInterruptIfRunning(boolean)
     */
    public U setTimeout(final long milliseconds) {
        requireActive();
        taskConfiguration = taskConfiguration.withTimeout(Duration.ofMillis(milliseconds));

        return getSelf();
    }

    /**
     * Sets the delay value.<br>
     * Sets the initial delay before the next task or periodic series can start. The
     * caller is responsible for choosing this delay relative to any timeout,
     * which is measured from submission and can expire before the task starts.
     *
     * @param milliseconds the delay value in milliseconds.
     * @return the current thread builder.
     */
    public U setDelay(final long milliseconds) {
        requireActive();
        taskConfiguration = taskConfiguration.withDelay(Duration.ofMillis(milliseconds));

        return getSelf();
    }

    /**
     * Sets the initial delay and submits the configured task. Equivalent to
     * {@code setDelay(milliseconds).start()}. The delay is measured from
     * submission, and the usual shutdown rule of {@link #start()} applies.
     *
     * @param milliseconds the initial delay in milliseconds
     * @return the submitted task's result and executor
     */
    public ExecutorResult<S> startAfterDelay(final long milliseconds) {
        setDelay(milliseconds);

        return start();
    }

    /**
     * Sets a fixed-rate period for the next {@code Runnable}, measured between
     * planned starts rather than after each completion. Executions of one
     * series do not overlap. The caller chooses the interval according to the
     * intended task behavior. A {@code Callable} builder overrides this method
     * to throw {@code UnsupportedOperationException} immediately, before any
     * interval is stored or an executor is created.
     *
     * @param milliseconds the repeating interval value in milliseconds.
     * @return the current thread builder.
     */
    public U setInterval(final long milliseconds) {
        requireActive();
        taskConfiguration = taskConfiguration.withInterval(Duration.ofMillis(milliseconds));

        return getSelf();
    }

    /**
     * Selects the argument passed to {@code Future.cancel(boolean)} by the
     * timeout action for the next task. The default is {@code false}; requesting
     * interruption is only a best-effort attempt to stop a running task.
     *
     * @param flag true if the thread executing this task should be interrupted;
     *             otherwise, in-progress tasks are allowed to complete.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    public U setMayInterruptIfRunning(final boolean flag) {
        requireActive();
        taskConfiguration = taskConfiguration.withMayInterruptIfRunning(flag);

        return getSelf();
    }

    @Override
    protected void newExecutorServiceIfNull() {
        super.newExecutorServiceIfNull();

        if (taskConfiguration.timeout().isPresent() && executor.getCorePoolSize() < 2) {
            executor.setCorePoolSize(2);
        }
    }

    private ThreadTimeConfig readThreadTimeConfig(final TaskConfiguration<T> task) {
        if (noSchedule(task)) {
            return ThreadTimeConfig.NO_SCHEDULE;
        } else if (onlyDelay(task)) {
            return ThreadTimeConfig.DELAY;
        } else if (onlyTimeout(task)) {
            return ThreadTimeConfig.TIMEOUT;
        } else if (onlyInterval(task)) {
            return ThreadTimeConfig.INTERVAL;
        } else if (delayAndTimeout(task)) {
            return ThreadTimeConfig.DELAY_AND_TIMEOUT;
        } else if (delayAndInterval(task)) {
            return ThreadTimeConfig.DELAY_AND_INTERVAL;
        }

        return ThreadTimeConfig.ALL_CONFIGURATION;
    }

    private boolean noSchedule(final TaskConfiguration<T> task) {
        return task.delay().isEmpty() && task.timeout().isEmpty() && task.interval().isEmpty();
    }

    private boolean onlyDelay(final TaskConfiguration<T> task) {
        return task.delay().isPresent() && task.timeout().isEmpty() && task.interval().isEmpty();
    }

    private boolean onlyTimeout(final TaskConfiguration<T> task) {
        return task.delay().isEmpty() && task.timeout().isPresent() && task.interval().isEmpty();
    }

    private boolean onlyInterval(final TaskConfiguration<T> task) {
        return task.delay().isEmpty() && task.timeout().isEmpty() && task.interval().isPresent();
    }

    private boolean delayAndTimeout(final TaskConfiguration<T> task) {
        return task.delay().isPresent() && task.timeout().isPresent() && task.interval().isEmpty();
    }

    private boolean delayAndInterval(final TaskConfiguration<T> task) {
        return task.delay().isPresent() && task.timeout().isEmpty() && task.interval().isPresent();
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     */
    @Override
    void runThread(final TaskConfiguration<T> submittedTask) {
        switch (readThreadTimeConfig(submittedTask)) {
            case DELAY:
                runWithDelay(submittedTask);
                break;
            case DELAY_AND_INTERVAL:
            case INTERVAL:
                runWithDelayAndInterval(submittedTask);
                break;
            case DELAY_AND_TIMEOUT:
            case TIMEOUT:
                runWithDelayAndTimeout(submittedTask);
                break;
            case NO_SCHEDULE:
                runWithNoSchedule(submittedTask);
                break;
            case ALL_CONFIGURATION:
            default:
                runWithAllTimesControls(submittedTask);
                break;
        }
    }

    private void runWithNoSchedule(final TaskConfiguration<T> submittedTask) {
        submitToBatch(() -> submit(submittedTask));
    }

    private void runWithDelay(final TaskConfiguration<T> submittedTask) {
        submitToBatch(() -> schedule(submittedTask, handleDelay(submittedTask), TimeUnit.MILLISECONDS));
    }

    private void runWithDelayAndTimeout(final TaskConfiguration<T> submittedTask) {
        final ScheduledFuture<S> future = submitToBatch(
                () -> schedule(submittedTask, handleDelay(submittedTask), TimeUnit.MILLISECONDS));
        handleInterruption(future, submittedTask);
    }

    private void runWithDelayAndInterval(final TaskConfiguration<T> submittedTask) {
        submitToBatch(() -> scheduleAtFixedRate(submittedTask, handleDelay(submittedTask),
                submittedTask.interval().orElseThrow().toMillis(), TimeUnit.MILLISECONDS));
        markPeriodicTaskSubmitted();
    }

    private void runWithAllTimesControls(final TaskConfiguration<T> submittedTask) {
        final ScheduledFuture<S> future = submitToBatch(() -> scheduleAtFixedRate(submittedTask,
                handleDelay(submittedTask), submittedTask.interval().orElseThrow().toMillis(),
                TimeUnit.MILLISECONDS));
        markPeriodicTaskSubmitted();
        handleInterruption(future, submittedTask);
    }

    /**
     * It schedules the thread, configuring the delay parameter.
     *
     * @param submittedTask the immutable configuration of the submitted task
     * @param delay the delay before thread execution.
     * @param unit  the time unit of the delay.
     * @return the scheduled-future-object of the thread.
     * @see ScheduledThreadPoolExecutor#schedule(Callable, long, TimeUnit)
     * @see ScheduledFuture
     */
    @SuppressWarnings({"unchecked"})
    ScheduledFuture<S> schedule(final TaskConfiguration<T> submittedTask,
                                final long delay, final TimeUnit unit) {
        final ScheduledFuture<S> future;

        switch (executionType) {
            case CALLABLE:
                future = executor.schedule((Callable<S>) submittedTask.execution(), delay, unit);

                break;
            case RUNNABLE:
            default:
                future = (ScheduledFuture<S>) executor.schedule((Runnable) submittedTask.execution(), delay, unit);

                break;
        }

        return future;
    }

    /**
     * Schedules a {@code Runnable} at fixed rate. A {@code Callable} is not
     * supported and causes {@code UnsupportedOperationException}. A failure
     * in a cycle suppresses later cycles according to the JDK scheduler.
     *
     * @param submittedTask the immutable configuration of the submitted task
     * @param delay  the delay before thread execution.
     * @param period the interval between thread executions.
     * @param unit   the time unit of the delay and period.
     * @return the scheduled-future-object of the thread.
     * @see ScheduledThreadPoolExecutor#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     * @see ScheduledFuture
     */
    @SuppressWarnings({"unchecked"})
    ScheduledFuture<S> scheduleAtFixedRate(final TaskConfiguration<T> submittedTask,
                                           final long delay,
                                           final long period, final TimeUnit unit) {
        final ScheduledFuture<S> future;

        switch (executionType) {
            case CALLABLE:
                throw new UnsupportedOperationException(ExecutionType.CALLABLE.name());
            case RUNNABLE:
            default:
                future = (ScheduledFuture<S>) executor
                        .scheduleAtFixedRate((Runnable) submittedTask.execution(), delay, period, unit);

                break;
        }

        return future;
    }

    private long handleDelay(final TaskConfiguration<T> submittedTask) {
        return submittedTask.delay().orElse(Duration.ZERO).toMillis();
    }

    private void handleInterruption(final ScheduledFuture<S> scheduledFuture,
                                    final TaskConfiguration<T> submittedTask) {
        executor.scheduleTimeout(cancelFuture(scheduledFuture, submittedTask.mayInterruptIfRunning()),
                submittedTask.timeout().orElseThrow().toMillis(), TimeUnit.MILLISECONDS);
    }

    private Runnable cancelFuture(final ScheduledFuture<S> future,
                                  final boolean mayInterruptIfRunning) {
        return () -> {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(mayInterruptIfRunning);
            }
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
