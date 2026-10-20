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

import br.com.armange.commons.thread.core.ExceptionCatcherThreadFactory;
import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base for asynchronous task builders with exception consumers and finalizers.
 * Calling {@link #execute()} submits a task through an internally created
 * executor and returns {@code void}; the executor and {@code Future} are not
 * exposed. Shutdown is requested immediately after submission; the executor
 * terminates after task callbacks, without blocking {@code execute()}.
 * Finalizers are after-execution callbacks, rather than a Java
 * {@code finally} block inside the task.
 *
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractTryAsyncBuilder<T extends AbstractTryAsyncBuilder<T>> {
    protected final Map<Class<Throwable>, Consumer<Throwable>> exceptionConsumers = new HashMap<>();
    protected final List<Runnable> finalizingExecutables = new LinkedList<>();

    /**
     * Registers a consumer for a matching task exception. If several types
     * match, this implementation does not guarantee that the most specific
     * catcher wins. The runnable and resource paths wrap a caught
     * {@code Exception}, but do not catch {@code Error} in the task body.
     * Callable failures are captured by their internal {@code Future}.
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
     * Adds an after-execution callback. It runs after the task's internal
     * {@code Future} completes, not inside the task's {@code finally} block.
     * For a {@code Callable}, its result consumer runs first. Its failure is
     * logged and finalizers are still attempted. If an earlier finalizer
     * throws, later finalizers may not run; these failures are logged rather
     * than delivered through a public result.
     *
     * @param runnable the after-execution callback
     * @return the current builder.
     */
    public T finallyAsync(final Runnable runnable) {
        return addFinalizer(runnable);
    }

    /**
     * Adds an after-execution finalizer. This method is protected; callers use
     * {@link #finallyAsync(Runnable)}.
     *
     * @param runnable the after-execution finalizer
     * @return the current builder.
     */
    @SuppressWarnings("unchecked")
    protected T addFinalizer(final Runnable runnable) {
        finalizingExecutables.add(runnable);

        return (T) this;
    }

    /**
     * Submits a {@link RunnableWithException} through an internal executor.
     * Its checked {@code Exception} is wrapped in
     * {@link AsyncRuntimeException}; catchers and finalizers run in executor
     * callbacks after the task's internal {@code Future} completes. The
     * executor is shut down after submission and terminates after its callbacks
     * without blocking this method.
     * Neither the {@code Future} nor the executor is exposed to the caller.
     *
     * @param attemptedExecution the thread implementation, which simulates the try-block
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher
     * @see AbstractTryAsyncBuilder#finallyAsync(java.lang.Runnable)
     */
    protected void execute(final RunnableWithException attemptedExecution) {
        final FutureTask<Void> futureTask = new FutureTask<>(() -> {
            try {
                attemptedExecution.run();
            } catch (final Exception e) {
                throw new AsyncRuntimeException(e);
            }
        }, null);

        submitAsyncTask(futureTask, null);
    }

    /**
     * Submits a {@link Callable} through an internal executor. On success,
     * the result consumer runs in an after-execution callback before the
     * finalizers. A failed result consumer is logged, then finalizers are
     * still attempted; a failed finalizer can prevent later finalizers.
     * Neither the consumer's failure nor the task's {@code Future} is returned
     * to the caller. Shutdown is requested after submission; the internal
     * executor terminates after callbacks.
     *
     * @param task           the thread implementation, which simulates the try-block
     * @param resultConsumer the thread result consumer implementation.
     * @param <S>            the type of the object resulting from the execution of the thread.
     * @see br.com.armange.commons.thread.async.AbstractTryAsyncBuilder#addCatcher
     * @see AbstractTryAsyncBuilder#finallyAsync(java.lang.Runnable)
     */
    protected <S> void execute(final Callable<S> task,
                               final Consumer<S> resultConsumer) {
        submitAsyncTask(new FutureTask<>(task), resultConsumer);
    }

    private <S> void submitAsyncTask(final FutureTask<S> futureTask,
                                     final Consumer<S> resultConsumer) {
        final ScheduledThreadBuilderExecutor executor = createExecutor();

        try {
            executor.addAfterExecuteConsumer((runnable, throwable) ->
                    observeResult(futureTask, resultConsumer));
            executor.addAfterExecuteConsumer((runnable, throwable) ->
                    finalizingExecutables.forEach(Runnable::run));
            executor.execute(futureTask);
        } finally {
            executor.shutdown();
        }
    }

    private <S> void observeResult(final FutureTask<S> futureTask,
                                   final Consumer<S> resultConsumer) {
        try {
            final S result = futureTask.get();

            if (resultConsumer != null) {
                resultConsumer.accept(result);
            }
        } catch (final Exception e) {
            consumeExceptionIfExists().accept(e);
        }
    }

    ScheduledThreadBuilderExecutor createExecutor() {
        final ExceptionCatcherThreadFactory factory = new ExceptionCatcherThreadFactory();

        factory.setUncaughtExceptionHandler((thread, throwable) ->
                consumeExceptionIfExists().accept(throwable));

        return new ScheduledThreadBuilderExecutor(1, factory);
    }

    /**
     * Selects one registered consumer compatible with the task exception or
     * its supported cause. Selection iterates a {@code HashMap}, so there is
     * no most-specific-first guarantee. The cause matching path assumes a
     * non-null cause; a matching runtime exception without one can cause
     * {@code NullPointerException}. An {@code Exception} thrown by a catcher is
     * logged by the executor callback; later callbacks are still attempted.
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
     * Starts the configured asynchronous task and returns without waiting for
     * its result, exception consumers, resource closure or finalizers. This
     * API does not provide a {@code Future} or a close method for its internal
     * executor. Shutdown is requested after submission and termination follows
     * the task callbacks; rejection propagates to the caller and also requests
     * shutdown.
     *
     * @see AbstractTryAsyncBuilder#execute(RunnableWithException)
     * @see AbstractTryAsyncBuilder#execute(Callable, Consumer)
     */
    public abstract void execute();
}
