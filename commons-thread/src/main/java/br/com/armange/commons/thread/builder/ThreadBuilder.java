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

import java.util.concurrent.Callable;

/**
 * Entry point for platform-thread task builders. Each started builder uses a
 * {@code ScheduledThreadBuilderExecutor}; a pool size configures worker
 * capacity, not the number of tasks submitted. A final {@code start()} returns
 * an {@link ExecutorResult} containing the futures of its batch and the
 * executor. Nonperiodic tasks request orderly shutdown by default, without
 * waiting for task completion or executor termination. An open stage selected
 * by {@code keepExecutorOpen()}, or any periodic task, leaves shutdown to the
 * caller. The caller chooses timing values according to the intended task
 * behavior. A timeout uses the same executor and requires at least two workers;
 * its scheduled action is not cancelled automatically when the task finishes
 * earlier and can remain pending until the deadline. Each submission needs a
 * task; timing settings return to their defaults after a successful submission.
 * The four task forms are:
 * <ul>
 *     <li>
 *          <b>Runnable task stages:</b> For tasks without a return value.
 *          See {@link #setExecution(Runnable)}.
 *     </li>
 *     <li>
 *          <b>TimingRunnableBuilder:</b> To implement threads that run tasks
 *          but do not need to return values after execution. However, they can be configured
 *          with scheduling options such as delay, interval or timeout, for example.
 *          See {@link #setScheduling(Runnable)}
 *     </li>
 *     <li>
 *          <b>Callable task stages:</b> For tasks that return values.
 *          See {@link #setExecution(Callable)}.
 *     </li>
 *     <li>
 *          <b>TimingCallableBuilder:</b> To implement threads that execute tasks
 *          and return values after execution. However, they can be configured with
 *          scheduling options such as delay, interval or timeout, for example.
 *          See {@link #setScheduling(Callable)}
 *     </li>
 * </ul>
 *
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public final class ThreadBuilder {

    private final int corePoolSize;

    private ThreadBuilder() {
        corePoolSize = 1;
    }

    private ThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * Creates a thread builder.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
     * @return a thread builder
     */
    public static ThreadBuilder newBuilder(final int corePoolSize) {
        return new ThreadBuilder(corePoolSize);
    }

    /**
     * Creates a thread builder.
     *
     * @return a thread builder.
     */
    public static ThreadBuilder newBuilder() {
        return new ThreadBuilder();
    }

    /**
     * Selects a runnable task using the default pool configuration. Equivalent
     * to {@code newBuilder().setExecution(execution)}. The task is submitted
     * only when the returned builder is started; its usual result and executor
     * shutdown rules apply.
     *
     * @param execution the runnable task
     * @return a builder for one or more runnable tasks
     */
    public static CommonRunnableBuilder newExecution(final Runnable execution) {
        return newBuilder()
                .setExecution(execution);
    }

    /**
     * Selects a callable task using the default pool configuration. Equivalent
     * to {@code newBuilder().setExecution(execution)}. The task is submitted
     * only when the returned builder is started; its usual result and executor
     * shutdown rules apply.
     *
     * @param execution the callable task
     * @param <S> the task result type
     * @return a builder for one or more callable tasks
     */
    public static <S> CommonCallableBuilder<S> newExecution(final Callable<S> execution) {
        return newBuilder()
                .setExecution(execution);
    }

    /**
     * Submits one runnable task using the default pool configuration.
     * Equivalent to {@code newExecution(execution).start()}. Orderly executor
     * shutdown is requested after submission, without waiting for completion.
     *
     * @param execution the runnable task
     * @return the task future and its executor
     */
    public static ExecutorResult<Void> startExecution(final Runnable execution) {
        return newExecution(execution)
                .start();
    }

    /**
     * Submits one callable task using the default pool configuration.
     * Equivalent to {@code newExecution(execution).start()}. Orderly executor
     * shutdown is requested after submission, without waiting for completion.
     *
     * @param execution the callable task
     * @param <S> the task result type
     * @return the task future and its executor
     */
    public static <S> ExecutorResult<S> startExecution(final Callable<S> execution) {
        return newExecution(execution)
                .start();
    }

    /**
     * Selects a runnable task with optional timing settings using the default
     * pool configuration. Equivalent to
     * {@code newBuilder().setScheduling(execution)}. The task is submitted
     * only when the returned builder is started; its usual result and executor
     * shutdown rules apply.
     *
     * @param execution the runnable task
     * @return a builder with delay, interval and timeout settings
     */
    public static TimingRunnableBuilder newScheduling(final Runnable execution) {
        return newBuilder()
                .setScheduling(execution);
    }

    /**
     * Selects a callable task with optional timing settings using the default
     * pool configuration. Equivalent to
     * {@code newBuilder().setScheduling(execution)}. The task is submitted
     * only when the returned builder is started. Periodic callables are
     * unsupported: {@code setInterval(...)} throws immediately without
     * creating an executor.
     *
     * @param execution the callable task
     * @param <S> the task result type
     * @return a builder with delay and timeout settings
     */
    public static <S> TimingCallableBuilder<S> newScheduling(final Callable<S> execution) {
        return newBuilder()
                .setScheduling(execution);
    }

    /**
     * Selects a runnable task without scheduling.
     *
     * @param execution the Runnable interface should be implemented by any class whose
     *                  instances are intended to be executed by a thread. The class must
     *                  define a method of no arguments called run.
     * @return a builder for one or more runnable tasks
     */
    public CommonRunnableBuilder setExecution(final Runnable execution) {
        return CommonRunnableBuilder.newBuilder(corePoolSize)
                .setExecution(execution);
    }

    /**
     * Selects a callable task without scheduling.
     *
     * @param execution a task that returns a result and may throw an exception.
     * @param <S>       the type of the object resulting from the execution of the thread.
     * @return a builder for one or more callable tasks
     */
    public <S> CommonCallableBuilder<S> setExecution(final Callable<S> execution) {
        return CommonCallableBuilder.<S>newBuilder(corePoolSize)
                .setExecution(execution);
    }

    /**
     * Selects a runnable task with optional delay, fixed-rate interval and
     * timeout.
     *
     * @param execution the Runnable interface should be implemented by any class whose
     *                  instances are intended to be executed by a thread. The class must
     *                  define a method of no arguments called run.
     * @return a thread builder with scheduling settings options.
     */
    public TimingRunnableBuilder setScheduling(final Runnable execution) {
        final TimingRunnableBuilder builder = TimingRunnableBuilder
                .newBuilder(corePoolSize);

        return builder.setScheduling(execution);
    }

    /**
     * Selects a callable task with delay or timeout. Periodic callables are
     * unsupported: {@code setInterval(...)} on the returned builder throws
     * immediately without creating an executor.
     *
     * @param execution a task that returns a result and may throw an exception.
     * @param <S>       the type of the object resulting from the execution of the thread.
     * @return a thread builder with scheduling settings options.
     */
    public <S> TimingCallableBuilder<S> setScheduling(final Callable<S> execution) {
        final TimingCallableBuilder<S> builder = TimingCallableBuilder
                .newBuilder(corePoolSize);

        return builder.setScheduling(execution);
    }
}
