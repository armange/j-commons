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
 * Class responsible for simplifying and speeding up the creation and configuration of threads.<br>
 * The implementation of threads can be simplified using one of the four classes below.<br>
 * <ul>
 *     <li>
 *          <b>CommonRunnableThreadBuilder:</b> To implement threads that run tasks
 *          but do not need to return values after execution. See {@link #setExecution(Runnable)}.
 *     </li>
 *     <li>
 *          <b>TimingRunnableThreadBuilder:</b> To implement threads that run tasks
 *          but do not need to return values after execution. However, they can be configured
 *          with scheduling options such as delay, interval or timeout, for example.
 *          See {@link #setScheduling(Runnable)}
 *     </li>
 *     <li>
 *          <b>CommonCallableThreadBuilder:</b> To implement threads that execute tasks
 *          and return values after execution. See {@link #setExecution(Callable)}
 *     </li>
 *     <li>
 *          <b>TimingCallableThreadBuilder:</b> To implement threads that execute tasks
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
     * Sets the thread task that can be configured with some common resources.
     *
     * @param execution the Runnable interface should be implemented by any class whose
     *                  instances are intended to be executed by a thread. The class must
     *                  define a method of no arguments called run.
     * @return a thread builder with scheduling settings options.
     */
    public CommonRunnableThreadBuilder setExecution(final Runnable execution) {
        final CommonRunnableThreadBuilder builder = CommonRunnableThreadBuilder
                .newBuilder(corePoolSize);

        return builder.setExecution(execution);
    }

    /**
     * Sets the thread task that can be configured with some common resources.
     *
     * @param execution a task that returns a result and may throw an exception.
     * @param <S>       the type of the object resulting from the execution of the thread.
     * @return a thread builder with common settings options.
     */
    public <S> CommonCallableThreadBuilder<S> setExecution(final Callable<S> execution) {
        final CommonCallableThreadBuilder<S> builder = CommonCallableThreadBuilder
                .newBuilder(corePoolSize);

        return builder.setExecution(execution);
    }

    /**
     * Sets the thread task that can be configured with some scheduling features.
     *
     * @param execution the Runnable interface should be implemented by any class whose
     *                  instances are intended to be executed by a thread. The class must
     *                  define a method of no arguments called run.
     * @return a thread builder with scheduling settings options.
     */
    public TimingRunnableThreadBuilder setScheduling(final Runnable execution) {
        final TimingRunnableThreadBuilder builder = TimingRunnableThreadBuilder
                .newBuilder(corePoolSize);

        return builder.setScheduling(execution);
    }

    /**
     * Sets the thread task that can be configured with some scheduling features.
     *
     * @param execution a task that returns a result and may throw an exception.
     * @param <S>       the type of the object resulting from the execution of the thread.
     * @return a thread builder with scheduling settings options.
     */
    public <S> TimingCallableThreadBuilder<S> setScheduling(final Callable<S> execution) {
        final TimingCallableThreadBuilder<S> builder = TimingCallableThreadBuilder
                .newBuilder(corePoolSize);

        return builder.setScheduling(execution);
    }
}
