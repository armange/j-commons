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
 * Builder for a one-shot {@link Callable} with optional delay and timeout.
 * Periodic callable execution is unsupported. Calling {@code setInterval()}
 * throws {@code UnsupportedOperationException} before the task configuration
 * changes or an executor is created.
 *
 * @param <S> the result type of method call.
 * @author Diego Armange Costa
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see br.com.armange.commons.thread.builder.ThreadBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class TimingCallableBuilder<S>
        extends AbstractTimingThreadBuilder<S, Callable<S>, TimingCallableBuilder<S>> {

    protected TimingCallableBuilder(final int corePoolSize) {
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
    protected static <T> TimingCallableBuilder<T> newBuilder(final int corePoolSize) {
        return new TimingCallableBuilder<>(corePoolSize);
    }

    /** Selects a scheduled stage whose executor remains owned by the caller. */
    public OpenTimingCallableBuilder<S> keepExecutorOpen() {
        final OpenTimingCallableBuilder<S> open = new OpenTimingCallableBuilder<>(corePoolSize);
        copyToOpen(open);

        return open;
    }

    /**
     * @return the type of the thread implementation.
     */
    @Override
    Class<?> getExecutionClass() {
        return Callable.class;
    }

    /**
     * Sets the thread task.
     *
     * @param execution a task that returns a result and may throw an exception.
     * @return the current thread builder.
     */
    protected TimingCallableBuilder<S> setScheduling(final Callable<S> execution) {
        return super.setExecution(execution);
    }

    /**
     * Sets the task for the next submission.
     *
     * @param execution the task to call
     * @return the current builder
     */
    @Override
    public TimingCallableBuilder<S> setExecution(final Callable<S> execution) {
        return super.setExecution(execution);
    }

    /**
     * Rejects a periodic {@code Callable} immediately. Any configured task
     * remains available for a nonperiodic submission after this exception.
     *
     * @param milliseconds the requested interval in milliseconds
     * @return never returns because periodic callables are unsupported
     * @throws UnsupportedOperationException for an active builder, before
     *                                       creating the executor
     */
    @Override
    public TimingCallableBuilder<S> setInterval(final long milliseconds) {
        requireActive();
        throw new UnsupportedOperationException("Periodic Callable scheduling is not supported");
    }
}
