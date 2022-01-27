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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;

/**
 * A {@link ScheduledThreadPoolExecutor} that can additionally perform actions
 * after that the thread has completed normally. Consider to see the full documentation
 * ({@link java.util.concurrent.ScheduledThreadPoolExecutor}).
 *
 * @author Diego Armange Costa
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(Runnable, Throwable)
 * @see #afterExecute(Runnable, Throwable)
 * @see #addAfterExecuteConsumer(BiConsumer)
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
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
        super(corePoolSize);
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
     * Method invoked upon completion of execution of the given Runnable. This
     * method is invoked by the thread that executed the task. If non-null, the
     * Throwable is the uncaught {@link java.lang.RuntimeException} or
     * {@link java.lang.Error} that caused execution to terminate abruptly. This
     * method will consume a list of {@code java.util.function.BiConsumer} with a
     * runnable and a throwable as arguments.
     *
     * @param runnable  the runnable that has completed
     * @param throwable the exception that caused termination, or null if execution
     *                  completed normally
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
                e.printStackTrace();
            }
        });
    }

    /**
     * After the execution of the thread, each of the consumers in the list will be executed,
     * receiving as a parameter the implementation of the newly finished thread and a
     * throwable object, which will be null if no exception has been thrown.
     *
     * @return the consumers list that will be performed after thread completed
     * normally.
     * @see ScheduledThreadBuilderExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    public List<BiConsumer<Runnable, Throwable>> getAfterExecuteConsumers() {
        return afterExecuteConsumers;
    }

    /**
     * After the execution of the thread, each of the consumers in the list will be executed,
     * receiving as a parameter the implementation of the newly finished thread and a
     * throwable object, which will be null if no exception has been thrown.
     *
     * @param afterExecuteBiConsumer the consumer that will be performed after
     *                               thread has completed normally.
     * @see ScheduledThreadBuilderExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    public void addAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteBiConsumer) {
        afterExecuteConsumers.add(afterExecuteBiConsumer);
    }
}
