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

/**
 * Class responsible for simplifying and speeding up the creation and configuration of threads.
 *
 * @author Diego Armange Costa
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see br.com.armange.commons.thread.builder.ThreadBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class TimingRunnableThreadBuilder<S>
        extends AbstractTimingThreadBuilder<S, Runnable, TimingRunnableThreadBuilder<S>> {

    private TimingRunnableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Creates a thread builder.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
     * @return the current thread builder.
     */
    protected static <T> TimingRunnableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new TimingRunnableThreadBuilder<>(corePoolSize);
    }

    @Override
    Class<Runnable> getExecutionClass() {
        return Runnable.class;
    }

    /**
     * Sets the thread task.
     *
     * @param execution the thread implementation.
     * @return the current thread builder.
     */
    protected TimingRunnableThreadBuilder<S> setScheduling(final Runnable execution) {
        return super.setExecution(execution);
    }

}
