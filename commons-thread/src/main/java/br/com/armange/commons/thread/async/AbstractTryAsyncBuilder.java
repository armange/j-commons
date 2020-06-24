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
 * These operations are just simple try-catch statements (or try-with-resources) executed on a new thread.
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractTryAsyncBuilder<T extends AbstractTryAsyncBuilder<T>> {
    protected final Map<Class<Throwable>, Consumer<Throwable>> exceptionConsumers = new HashMap<>();
    protected final List<Runnable> finalizingExecutables = new LinkedList<>();

    /**
     * Adds a treatment for a particular exception, if it is thrown during the
     * implementation contained in the "try" block.
     * @param exceptionClass the exception class that can be thrown and will be handled.
     * @param exceptionConsumer the implementation of the exception handling launched in the "try" block;
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
     * Adds an implementation that will be executed inside the "finally" block.
     * @param runnable the implementation that will be executed inside the "finally" block.
     * @return the current builder.
     */
    public T finallyAsync(final Runnable runnable) {
        return addFinalizer(runnable);
    }

    /**
     * Adds an implementation that will be executed inside the "finally" block.
     * @param runnable the implementation that will be executed inside the "finally" block.
     * @return the current builder.
     */
    @SuppressWarnings("unchecked")
    protected T addFinalizer(final Runnable runnable) {
        finalizingExecutables.add(runnable);

        return (T) this;
    }

    /**
     *
     * @param attemptedExecution
     */
    protected void execute(final Runnable attemptedExecution) {
        ThreadBuilder
                .newBuilder()
                .setExecution(attemptedExecution)
                .setUncaughtExceptionConsumer(consumeExceptionOrThrowRuntimeException())
                .setAfterExecuteConsumer((runnable, throwable) -> finalizingExecutables.forEach(Runnable::run))
                .start();
    }

    /**
     *
     * @param attemptedExecution
     * @param resultConsumer
     * @param <S>
     */
    protected <S> void execute(final Callable<S> attemptedExecution, final Consumer<S> resultConsumer) {
        ThreadBuilder
                .newBuilder()
                .setExecution(attemptedExecution)
                .setUncaughtExceptionConsumer(consumeExceptionOrThrowRuntimeException())
                .setAfterExecuteConsumer((runnable, throwable) -> finalizingExecutables.forEach(Runnable::run))
                .setThreadResultConsumer(resultConsumer)
                .start();
    }

    /**
     *
     * @return
     */
    protected Consumer<Throwable> consumeExceptionOrThrowRuntimeException() {
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

    public abstract void execute();
}
