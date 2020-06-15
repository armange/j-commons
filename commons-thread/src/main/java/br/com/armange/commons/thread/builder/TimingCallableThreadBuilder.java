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
import java.util.function.Consumer;

/**
 * Class responsible for simplifying and speeding up the creation and configuration of threads.
 *
 * @param <S> the result type of method call.
 * @author Diego Armange Costa
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see br.com.armange.commons.thread.builder.ThreadBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
@SuppressWarnings("rawtypes")
public class TimingCallableThreadBuilder<S>
        extends AbstractTimingThreadBuilder<S, Callable, TimingCallableThreadBuilder<S>> {

    private TimingCallableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Creates a thread builder.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
     * @param <T>          the type of the object resulting from the execution of the thread.
     * @return a new thread builder
     */
    protected static <T> TimingCallableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new TimingCallableThreadBuilder<>(corePoolSize);
    }

    /**
     * Sets a consumer for the thread's execution result.
     *
     * @param threadResultConsumer the consumer for the thread's execution result.
     * @return the current thread builder.
     */
    @Override
    public TimingCallableThreadBuilder setThreadResultConsumer(final Consumer<S> threadResultConsumer) {
        return super.setThreadResultConsumer(threadResultConsumer);
    }

    /**
     * @return the type of the thread implementation.
     */
    @Override
    Class<Callable> getExecutionClass() {
        return Callable.class;
    }

    /**
     * Sets the thread task.
     *
     * @param execution a task that returns a result and may throw an exception.
     * @return the current thread builder.
     */
    protected TimingCallableThreadBuilder<S> setScheduling(final Callable<S> execution) {
        return super.setExecution(execution);
    }
}
