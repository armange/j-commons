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
public class TimingRunnableBuilder
        extends AbstractTimingThreadBuilder<Void, Runnable, TimingRunnableBuilder> {

    protected TimingRunnableBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Creates a thread builder.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
     * @return the current thread builder.
     */
    protected static TimingRunnableBuilder newBuilder(final int corePoolSize) {
        return new TimingRunnableBuilder(corePoolSize);
    }

    /** Selects a scheduled stage whose executor remains owned by the caller. */
    public OpenTimingRunnableBuilder keepExecutorOpen() {
        final OpenTimingRunnableBuilder open = new OpenTimingRunnableBuilder(corePoolSize);
        copyToOpen(open);

        return open;
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
    protected TimingRunnableBuilder setScheduling(final Runnable execution) {
        return super.setExecution(execution);
    }

    /**
     * Sets the task for the next submission.
     *
     * @param execution the task to run
     * @return the current builder
     */
    @Override
    public TimingRunnableBuilder setExecution(final Runnable execution) {
        return super.setExecution(execution);
    }

    /**
     * Submits the configured {@code Runnable} at a fixed rate. Equivalent to
     * {@code setInterval(milliseconds).start()}. The result owns an open
     * executor; the caller must cancel the series and close the executor.
     * Executions of one series do not overlap.
     *
     * @param milliseconds the period between planned starts, in milliseconds
     * @return the periodic task's future and its open executor
     */
    public ExecutorResult<Void> repeatAtFixedRate(final long milliseconds) {
        setInterval(milliseconds);

        return start();
    }

}
