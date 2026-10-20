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
package br.com.armange.commons.thread.core;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;

/**
 * A {@link ScheduledThreadPoolExecutor} with callbacks invoked after each
 * execution, including exceptional completion. Callbacks are global to this
 * executor, not scoped to a submitted task. For tasks wrapped in a
 * {@code Future}, the {@code throwable} argument can be {@code null} even when
 * the future contains a failure; callers must inspect that future separately.
 * The mutable callback list is not synchronized.
 *
 * @author Diego Armange Costa
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(Runnable, Throwable)
 * @see #afterExecute(Runnable, Throwable)
 * @see #addAfterExecuteConsumer(BiConsumer)
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
@Slf4j
public class ScheduledThreadBuilderExecutor extends ScheduledThreadPoolExecutor {
    private final List<BiConsumer<Runnable, Throwable>> afterExecuteConsumers = new LinkedList<>();

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given core pool size.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they
     *                     are idle, unless {@code allowCoreThreadTimeOut} is set
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#ScheduledThreadPoolExecutor(int)
     */
    public ScheduledThreadBuilderExecutor(final int corePoolSize) {
        super(corePoolSize, ThreadFactories.select(
                ThreadFactories.Mode.PLATFORM, ThreadFactories.Purpose.TIMER));
    }

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given core pool size and thread factory.
     *
     * @param corePoolSize  the number of threads to keep in the pool, even if they
     *                      are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor creates a new
     *                      thread
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#ScheduledThreadPoolExecutor(int,
     * ThreadFactory)
     */
    public ScheduledThreadBuilderExecutor(final int corePoolSize, final ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    /**
     * Invokes all registered callbacks on the worker thread after an execution.
     * A submitted or scheduled task normally wraps its failure in a
     * {@code Future}, so {@code throwable} can be {@code null} despite a failed
     * task. Exceptions thrown by a callback are logged and later callbacks are
     * still attempted; an {@code Error} is not caught here.
     *
     * @param runnable  the completed runnable, possibly a future wrapper
     * @param throwable an uncaught failure from the runnable, or {@code null}
     *                  when the wrapper captured it or execution succeeded
     * @see ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     * @see ScheduledThreadBuilderExecutor#addAfterExecuteConsumer(java.util.function.BiConsumer)
     */
    @Override
    public void afterExecute(final Runnable runnable, final Throwable throwable) {
        super.afterExecute(runnable, throwable);
        afterExecuteConsumers.forEach(consumer -> {
            try {
                consumer.accept(runnable, throwable);
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Returns the live mutable callback list shared by all tasks on this
     * executor. Modifying it concurrently with execution is not synchronized.
     *
     * @return the live list of after-execution callbacks
     * @see ScheduledThreadBuilderExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    public List<BiConsumer<Runnable, Throwable>> getAfterExecuteConsumers() {
        return afterExecuteConsumers;
    }

    /**
     * Adds a callback for every execution on this executor, not just the task
     * that led to registration. The callback's throwable can be {@code null}
     * even when a submitted task failed inside its {@code Future}.
     *
     * @param afterExecuteBiConsumer the callback invoked after execution
     * @see ScheduledThreadBuilderExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    public void addAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteBiConsumer) {
        afterExecuteConsumers.add(afterExecuteBiConsumer);
    }
}
