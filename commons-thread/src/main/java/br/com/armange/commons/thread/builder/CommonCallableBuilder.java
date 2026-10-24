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
 * Class responsible for simplifying and speeding up the creation and configuration of threads.
 *
 * @param <S> the result type of method call.
 * @author Diego Armange Costa
 * @see br.com.armange.commons.thread.builder.ThreadBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class CommonCallableBuilder<S> extends
        AbstractThreadBuilder<S, Callable<S>, CommonCallableBuilder<S>> {

    protected CommonCallableBuilder(final int corePoolSize) {
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
    protected static <T> CommonCallableBuilder<T> newBuilder(final int corePoolSize) {
        return new CommonCallableBuilder<>(corePoolSize);
    }

    /** Selects a stage whose executor remains owned by the caller. */
    public OpenCallableBuilder<S> keepExecutorOpen() {
        final OpenCallableBuilder<S> open = new OpenCallableBuilder<>(corePoolSize);
        copyToOpen(open);

        return open;
    }

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
    @Override
    public CommonCallableBuilder<S> setExecution(final Callable<S> execution) {
        return super.setExecution(execution);
    }
}
