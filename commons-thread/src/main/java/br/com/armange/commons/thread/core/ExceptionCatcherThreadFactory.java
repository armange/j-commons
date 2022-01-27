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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

/**
 * Thread factory for use by the {@link br.com.armange.commons.thread.builder.ThreadBuilder}.
 * An implementation of {@link UncaughtExceptionHandler}, name and priority
 * can be assigned to each new thread created.
 *
 * @author Diego Armange Costa
 * @see java.util.concurrent.ThreadFactory
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class ExceptionCatcherThreadFactory implements ThreadFactory {
    private Optional<UncaughtExceptionHandler> uncaughtExceptionHandler = Optional.empty();
    private Optional<String> threadName = Optional.empty();
    private Optional<Integer> threadPriority = Optional.empty();

    /**
     * Creates a new thread according to the given parameter.
     * An implementation of exception handling, name and priority
     * can be assigned to each new thread created.
     *
     * @param runnable the thread implementation
     * @see ExceptionCatcherThreadFactory#setUncaughtExceptionHandler(UncaughtExceptionHandler)
     * @see #setThreadName(String)
     * @see #setThreadPriority(Integer)
     * @see java.util.concurrent.ThreadFactory#newThread(Runnable)
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable);

        uncaughtExceptionHandler.ifPresent(thread::setUncaughtExceptionHandler);

        threadName.ifPresent(thread::setName);

        threadPriority.ifPresent(thread::setPriority);

        return thread;
    }

    /**
     * Set the default handler invoked when a thread abruptly terminates
     * due to an uncaught exception, and no other handler has been defined
     * for that thread.
     *
     * @param uncaughtExceptionHandler the default handler.
     * @see Thread#setDefaultUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
     */
    public void setUncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = Optional.ofNullable(uncaughtExceptionHandler);
    }

    /**
     * Changes the name of this thread to be equal to the argument {@code threadName}.
     *
     * @param threadName the thread name.
     * @see Thread#setName(java.lang.String)
     */
    public void setThreadName(final String threadName) {
        this.threadName = Optional.ofNullable(threadName);
    }

    /**
     * Changes the priority of this thread.
     *
     * @param threadPriority the thread priority
     * @see java.lang.Thread#setPriority
     */
    public void setThreadPriority(final Integer threadPriority) {
        this.threadPriority = Optional.ofNullable(threadPriority);
    }
}
