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
 * Thread factory for platform threads. It can also be supplied by a subclass
 * of {@link br.com.armange.commons.thread.builder.AbstractThreadBuilder}.
 * A handler, fixed name and priority can be assigned to each new thread.
 * The name and priority setters configure future threads, not an existing
 * thread. A handler is invoked for genuinely uncaught failures; tasks
 * submitted to an executor usually capture failures in their {@code Future}
 * instead.
 * For direct thread creation on JDK 25, prefer {@link Thread#ofPlatform()} or
 * {@link Thread#ofVirtual()}. Both builders can configure names and uncaught
 * exception handlers; priority is available for platform threads only.
 *
 * @author Diego Armange Costa
 * @see java.util.concurrent.ThreadFactory
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class ExceptionCatcherThreadFactory implements ThreadFactory {
    private final ThreadFactory delegate = ThreadFactories.select(
            ThreadFactories.Mode.PLATFORM, ThreadFactories.Purpose.TASK);
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
        final Thread thread = delegate.newThread(runnable);

        uncaughtExceptionHandler.ifPresent(thread::setUncaughtExceptionHandler);

        threadName.ifPresent(thread::setName);

        threadPriority.ifPresent(thread::setPriority);

        return thread;
    }

    /**
     * Sets a per-thread handler for future threads created by this factory;
     * it does not change {@link Thread#setDefaultUncaughtExceptionHandler}.
     * A {@code null} argument clears the configured handler.
     *
     * @param uncaughtExceptionHandler the per-thread handler, or {@code null}
     *                                 to clear it
     * @see Thread#setUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
     */
    public void setUncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = Optional.ofNullable(uncaughtExceptionHandler);
    }

    /**
     * Sets one fixed name for future threads from this factory. A
     * {@code null} value clears the override and leaves generated names in
     * place. This setter does not rename any already-created thread.
     *
     * @param threadName the thread name.
     * @see Thread#setName(java.lang.String)
     */
    public void setThreadName(final String threadName) {
        this.threadName = Optional.ofNullable(threadName);
    }

    /**
     * Sets the priority for future platform threads from this factory. A
     * {@code null} value clears the override; an out-of-range priority is
     * rejected when a future thread is created, not by this setter.
     *
     * @param threadPriority the thread priority
     * @see java.lang.Thread#setPriority
     */
    public void setThreadPriority(final Integer threadPriority) {
        this.threadPriority = Optional.ofNullable(threadPriority);
    }
}
