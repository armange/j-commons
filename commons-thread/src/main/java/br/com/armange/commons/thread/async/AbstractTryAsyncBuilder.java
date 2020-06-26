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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import br.com.armange.commons.thread.builder.ThreadBuilder;

/**
 * Minimal abstraction for implementing a try-async operations builder.
 * These operations are just simple try-catch-finally statements executed on a new thread.
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractTryAsyncBuilder<T extends AbstractTryAsyncBuilder<T>> {
    protected final Map<Class<Throwable>, Consumer<Throwable>> exceptionConsumers = new HashMap<>();
    protected final List<Runnable> finalizingExecutables = new LinkedList<>();

    /**
     * Adds a treatment for a particular exception, if it is thrown during the
     * implementation contained in the "try-block".
     * @param exceptionClass the exception class that can be thrown and will be handled.
     * @param exceptionConsumer the implementation of the exception handling launched in the "try-block";
     * @return the current builder.
     */
    @SuppressWarnings("unchecked")
    public T addCatcher(
            final Class<? extends Throwable> exceptionClass,
            final Consumer<Throwable> exceptionConsumer) {
        exceptionConsumers.put((Class<Throwable>)exceptionClass, exceptionConsumer);

        return (T) this;
    }

    /**
     * Adds an implementation that will be executed inside the "finally-block".
     * @param runnable the implementation that will be executed inside the "finally-block".
     * @return the current builder.
     */
    public T finallyAsync(final Runnable runnable) {
        return addFinalizer(runnable);
    }

    /**
     * Adds an implementation that will be executed inside the finally-block.
     * @param runnable the implementation that will be executed inside the "finally-block".
     * @return the current builder.
     */
    @SuppressWarnings("unchecked")
    protected T addFinalizer(final Runnable runnable) {
        finalizingExecutables.add(runnable);

        return (T) this;
    }

    /**
     * Performs a thread according to the given implementation, which represents the try-block,
     * configured with an exception catcher and a finalizer. As with a try-catch block implementation,
     * only the main implementation is mandatory.
     * @param attemptedExecution the thread implementation, which represents the try-block
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher
     * @see AbstractTryAsyncBuilder#finallyAsync(java.lang.Runnable)
     */
    protected void execute(final Runnable attemptedExecution) {
        ThreadBuilder
                .newBuilder()
                .setExecution(attemptedExecution)
                .setUncaughtExceptionConsumer(consumeExceptionIfExists())
                .setAfterExecuteConsumer((runnable, throwable) -> finalizingExecutables.forEach(Runnable::run))
                .start();
    }

    /**
     * Performs a thread according to the given implementation, which represents the try-block,
     * configured with an exception catcher and a finalizer. As with a try-catch block implementation,
     * only the main implementation is mandatory. After the execution, if no exceptions is thrown,
     * another implementation according the given consumer will be performed to handle the result
     * value obtained by the first implementation.
     * @param attemptedExecution the thread implementation, which represents the try-block
     * @param resultConsumer the thread result consumer implementation.
     * @param <S> the thread result type
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher
     * @see AbstractTryAsyncBuilder#finallyAsync(java.lang.Runnable)
     */
    protected <S> void execute(final Callable<S> attemptedExecution, final Consumer<S> resultConsumer) {
        ThreadBuilder
                .newBuilder()
                .setExecution(attemptedExecution)
                .setUncaughtExceptionConsumer(consumeExceptionIfExists())
                .setAfterExecuteConsumer((runnable, throwable) -> finalizingExecutables.forEach(Runnable::run))
                .setThreadResultConsumer(resultConsumer)
                .start();
    }

    /**
     * Consumes an exception according the given consumers added by
     * {@link br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher}.
     * If no consumers is present, no action will be performed.
     * Only consumers compatible with the exception will be triggered.
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

    private Predicate<? super Class<Throwable>> isMatchingException(final Throwable throwable) {
        return expectedException ->
            throwable.getClass().equals(expectedException)
                    || throwable.getCause().getClass().equals(expectedException)
                    && throwable.getClass().equals(ExecutionException.class);
    }

    private Consumer<? super Class<Throwable>> catchException(final Throwable throwable) {
        return exceptionClass ->
            exceptionConsumers
                    .get(exceptionClass)
                    .accept(throwable);
    }

    /**
     * As a try-block implementation, performs the thread configured with the exception catcher and finalizer.
     * If the thread generate a result value and no exceptions is thrown, a result consumer will be triggered.
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#execute(java.lang.Runnable)
     * @see AbstractTryAsyncBuilder#execute(java.util.concurrent.Callable, java.util.function.Consumer)
     */
    public abstract void execute();
}
