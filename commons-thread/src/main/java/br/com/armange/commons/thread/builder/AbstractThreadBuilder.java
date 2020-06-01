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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import br.com.armange.commons.message.CommonMessages;
import br.com.armange.commons.thread.core.CaughtExecutorThreadFactory;
import br.com.armange.commons.thread.core.ScheduledCaughtExecutorService;
import br.com.armange.commons.thread.message.ExceptionMessage;

/**
 * @param <S> Result type
 * @param <T> Execution type
 * @param <U> Builder
 * @author Diego Armange Costa
 * @since 2020-05-31 V1.1.0 (JDK 1.8)
 */
public abstract class AbstractThreadBuilder<S, T, U extends AbstractThreadBuilder<S, T, U>> {

    protected static enum ExecutionType {
        CALLABLE, RUNNABLE, RECURSIVE_TASK, RECURSIVE_ACTION;

        static ExecutionType valueOf(final Class<?> sourceClass) {
            if (Callable.class.equals(sourceClass)) {
                return CALLABLE;
            } else if (Runnable.class.equals(sourceClass)) {
                return RUNNABLE;
            } else if (RecursiveTask.class.equals(sourceClass)) {
                return RECURSIVE_TASK;
            } else if (RecursiveAction.class.equals(sourceClass)) {
                return RECURSIVE_ACTION;
            } else {
                throw new IllegalArgumentException(
                        ExceptionMessage.ILLEGAL_ARGUMENT_THREAD_EXECUTOR_TYPE.format(Optional.ofNullable(sourceClass)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        ExceptionMessage.ILLEGAL_ARGUMENT_THREAD_EXECUTOR_TYPE.format("null")))
                                .getName()));
            }
        }
    }

    protected Optional<BiConsumer<Runnable, Throwable>> afterExecuteConsumer = Optional.empty();
    protected Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = Optional.empty();
    protected Optional<CaughtExecutorThreadFactory> threadFactory = Optional.empty();
    protected Optional<Supplier<String>> threadNameSupplier = Optional.empty();
    protected Optional<IntSupplier> threadPrioritySupplier = Optional.empty();
    protected Optional<Consumer<S>> threadResultConsumer = Optional.empty();

    protected ScheduledCaughtExecutorService executor;
    protected ExecutorResult<S> executorResult;

    protected T execution;

    protected boolean mayInterruptIfRunning;
    protected boolean silentInterruption;

    protected final ExecutionType executionType;
    protected final int corePoolSize;

    protected AbstractThreadBuilder() {
        corePoolSize = 1;
        executionType = ExecutionType.valueOf(getExceutionClass());
    }

    protected AbstractThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
        executionType = ExecutionType.valueOf(getExceutionClass());
    }

    /**
     * Sets the consumer to be called after thread execution.
     * 
     * @param afterExecuteConsumer the consumer to be called after thread execution.
     * @return the current thread builder.
     * @see br.com.armange.commons.thread.core.ScheduledCaughtExecutorService#afterExecute(Runnable,
     *      Throwable)
     */
    public U setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteConsumer) {
        this.afterExecuteConsumer = Optional.ofNullable(afterExecuteConsumer);

        return getSelf();
    }

    /**
     * Sets the consumer to be called after exception throwing. This consumer will
     * be called as a first after-executes consumer.
     * 
     * @param uncaughtExceptionConsumer the consumer to be called after exception
     *                                  throwing.
     * @return the current thread builder.
     */
    public U setUncaughtExceptionConsumer(final Consumer<? super Throwable> uncaughtExceptionConsumer) {
        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);

        return getSelf();
    }

    /**
     * Sets the thread name supplier.The thread factory will consume this supplier
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
     * Sets the thread priority supplier.The thread factory will consume this
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
    public U setExecution(final T execution) {
        this.execution = execution;

        requireExecutionNonNull();

        return getSelf();
    }

    /**
     * Sets the thread-interrupting-flag.
     * 
     * @param flag true if the thread executing this task should be interrupted;
     *             otherwise, in-progress tasks are allowed to complete.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    public U setMayInterruptIfRunning(final boolean flag) {
        mayInterruptIfRunning = flag;

        return getSelf();
    }

    /**
     * Sets the thread-silent-interrupting-flag.
     * 
     * @param flag true if the Interruption/Cancellation exceptions should be
     *             ignored.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     * @see java.util.concurrent.CancellationException
     * @see java.lang.InterruptedException
     */
    public U setSilentInterruption(final boolean flag) {
        silentInterruption = flag;

        return getSelf();
    }

    protected U setThreadResultConsumer(final Consumer<S> threadResultConsumer) {
        this.threadResultConsumer = Optional.ofNullable(threadResultConsumer);

        return getSelf();
    }

    @SuppressWarnings("unchecked")
    protected U getSelf() {
        return (U) this;
    }

    /**
     * Starts the thread.
     * 
     * @return the executor's result after starting thread.
     * @see ExecutorResult
     */
    public ExecutorResult<S> start() {
        createExecutorAndRunThread();

        return executorResult;
    }

    /**
     * Starts the thread.
     * 
     * @return the current thread builder to prepare another thread.
     */
    public U startAndBuildOther() {
        createExecutorAndRunThread();

        return getSelf();
    }

    protected void createExecutorAndRunThread() {
        requireExecutionNonNull();

        executor = new ScheduledCaughtExecutorService(corePoolSize, getThreadFactory());

        runThread();

        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
    }

    protected void requireExecutionNonNull() {
        Objects.requireNonNull(execution, CommonMessages.REQUIRED_PARAMETER.format("execution"));
    }

    protected ThreadFactory getThreadFactory() {
        final CaughtExecutorThreadFactory factory = threadFactory.orElse(new CaughtExecutorThreadFactory());

        uncaughtExceptionConsumer
                .ifPresent(uec -> factory.setUncaughtExceptionHandler((thread, throwable) -> uec.accept(throwable)));

        threadNameSupplier.ifPresent(tns -> factory.setThreadName(tns.get()));

        threadPrioritySupplier.ifPresent(tps -> factory.setThreadPriority(tps.getAsInt()));

        return factory;
    }

    protected void runThread() {
        final Future<S> future = submit();

        executor.addAfterExecuteConsumer(handleException(future));

        newExecutorResultIfNull();

        executorResult.getFutures().add(future);
    }

    @SuppressWarnings({ "unchecked" })
    protected Future<S> submit() {
        final Future<S> future;

        switch (executionType) {
        case CALLABLE:
            future = executor.submit((Callable<S>) execution);

            break;
        case RUNNABLE:
            future = (Future<S>) executor.submit((Runnable) execution);

            break;
        default:
            throw new IllegalStateException();

        }
        return future;
    }

    @SuppressWarnings({ "unchecked" })
    protected ScheduledFuture<S> schedule(final long delay, final TimeUnit unit) {
        final ScheduledFuture<S> future;

        switch (executionType) {
        case CALLABLE:
            future = executor.schedule((Callable<S>) execution, delay, unit);

            break;
        case RUNNABLE:
            future = (ScheduledFuture<S>) executor.schedule((Runnable) execution, delay, unit);

            break;
        default:
            throw new IllegalStateException();

        }
        return future;
    }

    @SuppressWarnings({ "unchecked" })
    protected ScheduledFuture<S> scheduleAtFixedRate(final long delay, final long period, final TimeUnit unit) {
        final ScheduledFuture<S> future;

        switch (executionType) {
        case CALLABLE:
            throw new UnsupportedOperationException(ExecutionType.CALLABLE.name());
        case RUNNABLE:
            future = (ScheduledFuture<S>) executor.scheduleAtFixedRate((Runnable) execution, delay, period, unit);

            break;
        default:
            throw new IllegalStateException();

        }
        return future;
    }

    protected BiConsumer<Runnable, Throwable> handleException(final Future<S> future) {
        return (runnable, throwable) -> {
            if (throwable == null) {
                try {
                    if (future.isDone()) {
                        consumesFuture(future);
                    }
                } catch (final Exception e) {
                    if (isNotSilentOrIsExecutionException(e)) {
                        uncaughtExceptionConsumer.orElseGet(() -> System.out::println).accept(e);
                    }
                }
            }
        };
    }

    protected void consumesFuture(final Future<S> future) throws InterruptedException, ExecutionException {
        switch (executionType) {
        case CALLABLE:
            executorResult.setThreadResult(future.get());

            threadResultConsumer.ifPresent(consumer -> consumer.accept(executorResult.getThreadResult()));

            break;
        case RECURSIVE_ACTION:
            throw new UnsupportedOperationException(ExecutionType.RECURSIVE_ACTION.name());
        case RECURSIVE_TASK:
            throw new UnsupportedOperationException(ExecutionType.RECURSIVE_TASK.name());
        case RUNNABLE:
            future.get();

            break;
        default:
            throw new UnsupportedOperationException(executionType.name());
        }
    }

    private boolean isNotSilentOrIsExecutionException(final Exception e) {
        return !silentInterruption || !(e instanceof CancellationException) && !(e instanceof InterruptedException);
    }

    private void newExecutorResultIfNull() {
        executorResult = executorResult == null ? new ExecutorResult<>(executor) : executorResult;
    }

    protected boolean isRunnableClass() {
        return getExceutionClass().equals(Runnable.class);
    }

    protected boolean isCallableClass() {
        return getExceutionClass().equals(Callable.class);
    }

    abstract Class<T> getExceutionClass();
}
