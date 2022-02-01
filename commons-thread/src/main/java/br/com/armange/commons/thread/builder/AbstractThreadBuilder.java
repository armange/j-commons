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

import br.com.armange.commons.thread.core.ExceptionCatcherThreadFactory;
import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Abstraction for thread builders that simplify, and provide some additional features(below)
 * to the thread implementation.
 * <br>
 * <ul>
 *   <li>
 *     Uncaught exception consumer
 *   </li>
 *   <li>
 *     After execute consumer
 *   </li>
 * </ul>
 *
 * @param <S> the type of the object resulting from the execution of the thread.
 * @param <T> Execution type. {@link Runnable}, {@link Callable}.
 * @param <U> Builder. The Builder extending {@link AbstractThreadBuilder}
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractThreadBuilder<S, T, U extends AbstractThreadBuilder<S, T, U>> {

    private static final String ILLEGAL_ARGUMENT_THREAD_EXECUTOR_TYPE = "Illegal executor-type \"{0}\".";
    private static final Logger LOGGER = LogManager.getLogger();

    protected Optional<BiConsumer<Runnable, Throwable>> afterExecuteConsumer = Optional.empty();
    protected Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = Optional.empty();
    protected Optional<ExceptionCatcherThreadFactory> threadFactory = Optional.empty();
    protected Optional<Supplier<String>> threadNameSupplier = Optional.empty();
    protected Optional<IntSupplier> threadPrioritySupplier = Optional.empty();
    protected Optional<Consumer<S>> threadResultConsumer = Optional.empty();

    protected ScheduledThreadBuilderExecutor executor;
    protected ExecutorResult<S> executorResult;

    protected T execution;

    protected boolean mayInterruptIfRunning;
    protected boolean silentInterruption;

    protected final ExecutionType executionType;
    protected final int corePoolSize;

    /**
     * Constructs an abstract thread builder.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
     */
    protected AbstractThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;

        executionType = ExecutionType.valueOf(getExecutionClass());
    }

    /**
     * Sets the consumer to be called after thread execution.
     *
     * @param afterExecuteConsumer the consumer to be called after thread execution.
     * @return the current thread builder.
     * @see ScheduledThreadBuilderExecutor#afterExecute(Runnable, Throwable)
     */
    public U setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteConsumer) {
        this.afterExecuteConsumer = Optional.ofNullable(afterExecuteConsumer);

        return getSelf();
    }

    /**
     * Sets the consumer to be called after exception throwing. This consumer will
     * be called as a first after-executes consumer.
     *
     * @param uncaughtExceptionConsumer the consumer to be called after exception throwing.
     * @return the current thread builder.
     */
    public U setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer) {
        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);

        return getSelf();
    }

    /**
     * Sets the thread name supplier. The thread factory will consume this supplier
     * to generate a thread name before return a new thread.
     *
     * @param threadNameSupplier the thread name supplier.
     * @return the current thread builder.
     */
    public U setThreadNameSupplier(final Supplier<String> threadNameSupplier) {
        this.threadNameSupplier = Optional.ofNullable(threadNameSupplier);

        return getSelf();
    }

    /**
     * Sets the thread priority supplier. The thread factory will consume this
     * supplier to generate a thread priority before return a new thread.
     *
     * @param threadPrioritySupplier the thread priority supplier.
     * @return the current thread builder.
     */
    public U setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier) {
        this.threadPrioritySupplier = Optional.ofNullable(threadPrioritySupplier);

        return getSelf();
    }

    /**
     * Sets the thread execution.
     *
     * @param execution the thread execution({@link java.lang.Runnable})
     * @return the current thread builder.
     */
    protected U setExecution(final T execution) {
        this.execution = execution;

        requireExecutionNonNull();

        return getSelf();
    }

    /**
     * Sets the thread-silent-interrupting-flag.<br>
     * When this flag is true, exceptions caught on the thread will only be logged,
     * and will not be handled in any way.
     *
     * @param flag true if the Interruption/Cancellation exceptions should be ignored.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     * @see java.util.concurrent.CancellationException
     * @see java.lang.InterruptedException
     */
    public U setSilentInterruption(final boolean flag) {
        silentInterruption = flag;

        return getSelf();
    }

    /**
     * Sets the thread result consumer.<br>
     * After the thread execution, the result obtained will be consumed by this consumer.
     *
     * @param threadResultConsumer the thread result consumer.
     * @return the current thread builder.
     */
    protected U setThreadResultConsumer(final Consumer<S> threadResultConsumer) {
        this.threadResultConsumer = Optional.ofNullable(threadResultConsumer);

        return getSelf();
    }

    /**
     * @return the current thread builder.
     */
    @SuppressWarnings("unchecked")
    protected U getSelf() {
        return (U) this;
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     *
     * @return the executor's result after starting thread.
     * @see ExecutorResult
     */
    public ExecutorResult<S> start() {
        createExecutorAndRunThread();

        return executorResult;
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     *
     * @return the current thread builder to prepare another thread.
     */
    public U startAndBuildOther() {
        createExecutorAndRunThread();

        return getSelf();
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     */
    protected void createExecutorAndRunThread() {
        requireExecutionNonNull();

        newExecutorServiceIfNull();

        runThread();
    }

    /**
     * Creates the thread executor (if necessary).
     */
    protected void newExecutorServiceIfNull() {
        executor = executor == null
                ? new ScheduledThreadBuilderExecutor(corePoolSize, getThreadFactory()) : executor;
    }

    /**
     * Throws {@link NullPointerException} if it has no thread execution implementation.
     */
    protected void requireExecutionNonNull() {
        Objects.requireNonNull(execution);
    }

    protected ThreadFactory getThreadFactory() {
        final ExceptionCatcherThreadFactory factory = threadFactory.orElseGet(ExceptionCatcherThreadFactory::new);

        uncaughtExceptionConsumer
                .ifPresent(uec -> factory.setUncaughtExceptionHandler((thread, throwable) -> uec.accept(throwable)));

        threadNameSupplier.ifPresent(tns -> factory.setThreadName(tns.get()));

        threadPrioritySupplier.ifPresent(tps -> factory.setThreadPriority(tps.getAsInt()));

        return factory;
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     */
    protected void runThread() {
        final Holder<Future<S>> futureHolder = Holder.empty();

        executor.addAfterExecuteConsumer(handleException(futureHolder));

        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);

        futureHolder.set(submit());

        newExecutorResultIfNull();

        executorResult.getFutures().add(futureHolder.get());
    }

    /**
     * Submits the thread's implementation to the executor.
     *
     * @return the Future object.
     * @see ScheduledThreadPoolExecutor#submit(java.util.concurrent.Callable)
     */
    @SuppressWarnings({"unchecked"})
    protected Future<S> submit() {
        final Future<S> future;

        switch (executionType) {
            case CALLABLE:
                future = executor.submit((Callable<S>) execution);

                break;
            case RUNNABLE:
            default:
                future = (Future<S>) executor.submit((Runnable) execution);

                break;
        }

        return future;
    }

    /**
     * Returns a lambda that handles exceptions caught on the thread.<br>
     * If the thread has not been canceled and there are no exceptions, and if there is
     * a consumer for the thread's result, it will be executed.<br>
     * If the thread has thrown an exception, and if the thread has not been configured
     * to silence exceptions, it will be handled according to the exception's
     * consumer (if configured previously).
     *
     * @param futureHolder the thread's Future object, encapsulated by a Holder object.
     * @param <F>          the type of the object resulting from the execution of the thread.
     * @return a consumer for exceptions caught within threads.
     * @see Holder
     * @see Future
     * @see #setSilentInterruption(boolean)
     */
    protected <F extends Future<S>> BiConsumer<Runnable, Throwable> handleException(final Holder<F> futureHolder) {
        return (runnable, throwable) -> {
            final Future<S> future = futureHolder.get();

            if (throwable == null) {
                try {
                    if (future.isDone()) {
                        consumesFuture(future);
                    }
                } catch (final Exception e) {
                    if (isNotSilentOrIsExecutionException(e)) {
                        uncaughtExceptionConsumer.orElseGet(() -> LOGGER::error).accept(e);
                    } else {
                        LOGGER.error(e);
                    }
                }
            }
        };
    }

    /**
     * If the thread is of type {@link Callable}, it stores the thread result in an
     * {@link ExecutorResult} instance and applies this result to the respective consumer.
     * If it is a {@link Runnable} thread, it just calls the get method of the
     * {@link Future} object.
     *
     * @param future the thread Future object.
     * @throws InterruptedException if the thread was interrupted.
     * @throws ExecutionException   if the thread fails.
     */
    protected void consumesFuture(final Future<S> future) throws InterruptedException, ExecutionException {
        switch (executionType) {
            case CALLABLE:
                executorResult.setThreadResult(future.get());

                threadResultConsumer.ifPresent(consumer -> consumer.accept(executorResult.getThreadResult()));

                break;
            case RUNNABLE:
            default:
                future.get();

                break;
        }
    }

    private boolean isNotSilentOrIsExecutionException(final Exception e) {
        return !silentInterruption || !(e instanceof CancellationException);
    }

    /**
     * Creates a new {@link ExecutorResult} if necessary.
     */
    protected void newExecutorResultIfNull() {
        executorResult = executorResult == null ? new ExecutorResult<>(executor) : executorResult;
    }

    /**
     * @return the type of the thread implementation.
     */
    abstract Class<T> getExecutionClass();

    /**
     * Class that enumerates the types of thread implementations that are supported by this builder.
     */
    protected enum ExecutionType {
        CALLABLE, RUNNABLE; //RECURSIVE_TASK, RECURSIVE_ACTION

        static ExecutionType valueOf(final Class<?> sourceClass) {
            if (Callable.class.equals(sourceClass)) {
                return CALLABLE;
            } else if (Runnable.class.equals(sourceClass)) {
                return RUNNABLE;
            } else {
                throw new IllegalArgumentException(
                        String.format(ILLEGAL_ARGUMENT_THREAD_EXECUTOR_TYPE, sourceClass.getName()));
            }
        }
    }
}
