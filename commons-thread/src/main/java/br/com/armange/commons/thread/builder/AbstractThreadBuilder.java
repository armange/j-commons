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
 * Base for builders that submit tasks to a
 * {@link ScheduledThreadBuilderExecutor}. The builder creates the executor on
 * first start and reuses it on later starts. The caller owns its shutdown via
 * {@link ExecutorResult#getExecutorService()}.
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
     * Registers a callback on the shared executor after task execution. It
     * can receive events from other tasks using that executor. For submitted
     * tasks, {@code throwable} can be {@code null} even when their
     * {@code Future} contains a failure.
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
     * Configures a consumer for two failure paths. An after-execution callback
     * reads submitted tasks' {@code Future}s and typically passes an
     * {@code ExecutionException} (or a cancellation exception), rather than
     * the task's direct cause. The consumer is also installed as an
     * {@code UncaughtExceptionHandler} on threads created by the configured
     * factory; there it receives the original genuinely uncaught throwable.
     * Failures captured by a submitted task's {@code Future} do not invoke
     * that thread handler. On a reused executor, callbacks registered by
     * earlier starts can also observe later tasks.
     *
     * @param uncaughtExceptionConsumer the consumer for observed task failures
     *                                  and genuinely uncaught thread failures
     * @return the current thread builder.
     */
    public U setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer) {
        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);

        return getSelf();
    }

    /**
     * Sets the thread name supplier. The default thread factory consults this
     * supplier once for each new thread. When an
     * {@link ExceptionCatcherThreadFactory} is supplied, this builder consults
     * the supplier once while configuring the factory and passes the result to
     * {@link ExceptionCatcherThreadFactory#setThreadName(String)}. The supplied
     * factory determines each thread's actual name. With the default factory
     * or the base supplied factory, a null result keeps the generated name;
     * a supplied subclass may handle null differently.
     *
     * @param threadNameSupplier the thread name supplier.
     * @return the current thread builder.
     */
    public U setThreadNameSupplier(final Supplier<String> threadNameSupplier) {
        this.threadNameSupplier = Optional.ofNullable(threadNameSupplier);

        return getSelf();
    }

    /**
     * Sets a priority supplier for platform threads. The default factory
     * consults it once when the executor is created; a supplied
     * {@link ExceptionCatcherThreadFactory} receives that value through its
     * setter. Priority is not a configurable virtual-thread property.
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
     * Controls handling of {@code CancellationException} observed through an
     * internal {@code Future}. When true, cancellation is logged instead of
     * sent to the configured exception consumer; other exceptions still reach
     * that consumer. This does not suppress interruption of the task or make
     * the executor silent.
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
     * After successful {@code Callable} execution, an executor callback reads
     * the {@code Future} and calls this consumer. A caller's
     * {@code Future.get()} can return before the consumer finishes. A failed
     * consumer is handled by the callback path, not stored in that future.
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
     * Creates or reuses the executor and submits the configured task. The
     * returned result exposes task futures and the executor; the caller must
     * shut down the executor when it is no longer needed.
     *
     * @return the executor's result after starting thread.
     * @see ExecutorResult
     */
    public ExecutorResult<S> start() {
        createExecutorAndRunThread();

        return executorResult;
    }

    /**
     * Creates or reuses the executor, submits the task and returns this same
     * builder for another submission. The pool size does not itself create
     * multiple tasks.
     *
     * @return the current thread builder to prepare another thread.
     */
    public U startAndBuildOther() {
        createExecutorAndRunThread();

        return getSelf();
    }

    /**
     * Registers after-execution callbacks, submits one task and adds its
     * future to the shared {@link ExecutorResult}. Repeated submissions on
     * this executor also retain previously registered callbacks.
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
        if (threadFactory.isPresent()) {
            return configureSuppliedThreadFactory(threadFactory.get());
        }

        return createPlatformThreadFactory();
    }

    private ThreadFactory configureSuppliedThreadFactory(final ExceptionCatcherThreadFactory factory) {
        uncaughtExceptionConsumer
                .ifPresent(consumer -> factory.setUncaughtExceptionHandler(
                        (thread, throwable) -> consumer.accept(throwable)));
        threadNameSupplier
                .ifPresent(supplier -> factory.setThreadName(supplier.get()));
        threadPrioritySupplier
                .ifPresent(supplier -> factory.setThreadPriority(supplier.getAsInt()));

        return factory;
    }

    private ThreadFactory createPlatformThreadFactory() {
        final ExceptionCatcherThreadFactory platformFactory = new ExceptionCatcherThreadFactory();

        uncaughtExceptionConsumer
                .ifPresent(consumer -> platformFactory.setUncaughtExceptionHandler(
                        (thread, throwable) -> consumer.accept(throwable)));
        threadPrioritySupplier
                .ifPresent(supplier -> {
                    final int priority = supplier.getAsInt();

                    if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
                        throw new IllegalArgumentException("Invalid thread priority: " + priority);
                    }

                    platformFactory.setThreadPriority(priority);
                });

        if (threadNameSupplier.isEmpty()) {
            return platformFactory;
        }

        return createNamedThreadFactory(platformFactory, threadNameSupplier.get());
    }

    private ThreadFactory createNamedThreadFactory(
            final ThreadFactory platformFactory, final Supplier<String> nameSupplier) {
        return runnable -> {
            final Thread thread = platformFactory.newThread(runnable);
            Optional.ofNullable(nameSupplier.get())
                    .ifPresent(thread::setName);

            return thread;
        };
    }

    /**
     * Creates the thread executor (if necessary) and starts the thread.
     */
    protected void runThread() {
        final Holder<Future<S>> futureHolder = Holder.empty();

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
     * Returns an after-execution callback that inspects the submitted
     * {@code Future} when the executor supplies a null throwable. It reads a
     * successful callable result or passes a caught failure to the configured
     * consumer. {@code setSilentInterruption(true)} only bypasses that consumer
     * for {@code CancellationException}; the cancellation is still logged.
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
     * For a {@link Callable}, stores the value in the shared
     * {@link ExecutorResult} and invokes its result consumer. For a
     * {@link Runnable}, calls {@code Future.get()} to observe failure.
     * Callback execution follows future completion and is not awaited by
     * another caller of that future.
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
