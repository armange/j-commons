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
package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.builder.ThreadBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Abstraction to implement a try-async operations builder.
 * These operations are simply try-catch-finally statements executed on a new thread.
 *
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractTryAsyncBuilder<T extends AbstractTryAsyncBuilder<T>> {
    protected final Map<Class<Throwable>, Consumer<Throwable>> exceptionConsumers = new HashMap<>();
    protected final List<Runnable> finalizingExecutables = new LinkedList<>();

    /**
     * It adds a treatment for a particular exception, if it is thrown during the
     * implementation contained in the "try-block".
     *
     * @param exceptionClass    the exception class that can be thrown and will be handled.
     * @param exceptionConsumer the implementation of the exception handling launched
     *                          in the "try-block".
     * @return the current builder.
     */
    @SuppressWarnings("unchecked")
    public T addCatcher(
            final Class<? extends Throwable> exceptionClass,
            final Consumer<Throwable> exceptionConsumer) {
        exceptionConsumers.put((Class<Throwable>) exceptionClass, exceptionConsumer);

        return (T) this;
    }

    /**
     * It adds an implementation that will be executed inside the "finally-block".
     *
     * @param runnable the implementation that will be executed inside the "finally-block".
     * @return the current builder.
     */
    public T finallyAsync(final Runnable runnable) {
        return addFinalizer(runnable);
    }

    /**
     * Adds an implementation that will be executed inside the finally-block.
     *
     * @param runnable the implementation that will be executed inside the "finally-block".
     * @return the current builder.
     */
    @SuppressWarnings("unchecked")
    protected T addFinalizer(final Runnable runnable) {
        finalizingExecutables.add(runnable);

        return (T) this;
    }

    /**
     * Executes a thread according to the given implementation, which simulates a try block,
     * configured with an (or more) exception catcher, as well as a finalizer.
     * As with a try-catch block implementation, only the main implementation(try-block)
     * is mandatory.
     *
     * @param attemptedExecution the thread implementation, which simulates the try-block
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher
     * @see AbstractTryAsyncBuilder#finallyAsync(java.lang.Runnable)
     */
    protected void execute(final RunnableWithException attemptedExecution) {
        ThreadBuilder
                .newBuilder()
                .setExecution(() -> {
                    try {
                        attemptedExecution.run();
                    } catch (final Exception e) {
                        throw new AsyncRuntimeException(e);
                    }
                })
                .setUncaughtExceptionConsumer(consumeExceptionIfExists())
                .setAfterExecuteConsumer((runnable, throwable) ->
                        finalizingExecutables.forEach(Runnable::run))
                .start();
    }

    /**
     * A new thread will be configured and executed with the following structure:<br><br>
     * In addition to the thread implementation, existing exception handling implementations
     * will be added, however, only the implementation corresponding to the thrown exception
     * will be executed. After the thread runs, even if it throws an exception, the finalizing
     * implementation (if configured) will be executed. Finally, if the thread produces some
     * object as a result, and if there is a consumer for that result, it will be executed.
     *
     * @param task           the thread implementation, which simulates the try-block
     * @param resultConsumer the thread result consumer implementation.
     * @param <S>            the type of the object resulting from the execution of the thread.
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher
     * @see AbstractTryAsyncBuilder#finallyAsync(java.lang.Runnable)
     */
    protected <S> void execute(final Callable<S> task,
                               final Consumer<S> resultConsumer) {
        ThreadBuilder
                .newBuilder()
                .setExecution(task)
                .setUncaughtExceptionConsumer(consumeExceptionIfExists())
                .setAfterExecuteConsumer((runnable, throwable) ->
                        finalizingExecutables.forEach(Runnable::run))
                .setThreadResultConsumer(resultConsumer)
                .start();
    }

    /**
     * It consumes an exception according the given consumers added by
     * {@link br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher}.
     * If no consumers is present, no action will be performed.
     * Only consumers compatible with the exception will be triggered.
     * It will be triggered twice if the exception consumer is rethrowing any exception.
     * The second calling will be triggered in
     * "java.lang.Thread#dispatchUncaughtException(Throwable)".
     * see "AbstractThreadBuilder#getThreadFactory()"
     *
     * @return the exception consumer implementation.
     */
    protected Consumer<Throwable> consumeExceptionIfExists() {
        return throwable ->
                exceptionConsumers
                        .keySet()
                        .stream()
                        .filter(isMatchingException(throwable))
                        .findFirst()
                        .ifPresent(catchException(throwable));
    }

    private Predicate<? super Class<Throwable>> isMatchingException(final Throwable thrown) {
        return expectedException -> isExpectedException(thrown, expectedException)
                || isExpectedCause(thrown, expectedException);
    }

    private boolean isExpectedException(final Throwable origin,
                                        final Class<? extends Throwable> expectedException) {
        return isClassOf(origin, expectedException)
                || expectedException.isAssignableFrom(origin.getClass());
    }

    private boolean isExpectedCause(final Throwable origin,
                                    final Class<? extends Throwable> expectedCause) {
        return isCauseOf(origin, expectedCause)
                && (isClassOf(origin, ExecutionException.class)
                || isClassOf(origin, RuntimeException.class));
    }

    private boolean isClassOf(final Throwable origin,
                              final Class<? extends Throwable> expectedClass) {
        return origin.getClass().equals(expectedClass);
    }

    private boolean isCauseOf(final Throwable origin,
                              final Class<? extends Throwable> expectedCause) {
        return origin.getCause().getClass().equals(expectedCause) ||
                expectedCause.isAssignableFrom(origin.getCause().getClass());
    }

    private Consumer<? super Class<Throwable>> catchException(final Throwable throwable) {
        return exceptionClass ->
                exceptionConsumers
                        .get(exceptionClass)
                        .accept(throwable);
    }

    /**
     * As a try-block implementation, performs the thread configured with the
     * exception catcher(s) and a finalizer. If the thread generates a result value
     * and no exceptions is thrown, a result consumer will be triggered.
     *
     * @see AbstractTryAsyncBuilder#execute(RunnableWithException)
     * @see AbstractTryAsyncBuilder#execute(Callable, Consumer)
     */
    public abstract void execute();
}
