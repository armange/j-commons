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
 * first start and reuses it on later open starts. A default nonperiodic final
 * start requests orderly shutdown after submitting the batch, without waiting
 * for task completion or executor termination. The caller owns shutdown after
 * receiving an {@link OpenExecutorResult}. Each successful submission consumes
 * an immutable task configuration; the next submission requires a new task.
 * Executor and thread-factory settings remain shared by the builder.
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
    protected Optional<BiConsumer<Runnable, Throwable>> afterExecuteConsumer = Optional.empty();
    protected Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = Optional.empty();
    protected Optional<ExceptionCatcherThreadFactory> threadFactory = Optional.empty();
    protected Optional<Supplier<String>> threadNameSupplier = Optional.empty();
    protected Optional<IntSupplier> threadPrioritySupplier = Optional.empty();
    protected ScheduledThreadBuilderExecutor executor;
    protected ExecutorResult<S> executorResult;
    protected TaskBatch<S> batch;

    TaskConfiguration<T> taskConfiguration = TaskConfiguration.empty();
    protected final ExecutionType executionType;
    protected final int corePoolSize;
    private boolean openMode;
    private boolean terminal;
    private boolean periodicTaskSubmitted;
    private boolean executorDelivered;

    /**
     * Constructs an abstract thread builder.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
     */
    protected AbstractThreadBuilder(final int corePoolSize) {
        this.corePoolSize = Math.max(1, corePoolSize);

        executionType = ExecutionType.valueOf(getExecutionClass());
    }

    /**
     * Adds a callback to the shared executor. Before the executor exists, the
     * most recent configuration is registered when it is created. After that,
     * each non-null call registers one global callback immediately. Passing
     * {@code null} does not remove callbacks already registered. The callback
     * can receive events from other tasks using that executor. For submitted
     * tasks, {@code throwable} can be {@code null} even when their
     * {@code Future} contains a failure.
     *
     * @param afterExecuteConsumer the consumer to be called after thread execution.
     * @return the current thread builder.
     * @see ScheduledThreadBuilderExecutor#afterExecute(Runnable, Throwable)
     */
    public U setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteConsumer) {
        requireActive();
        this.afterExecuteConsumer = Optional.ofNullable(afterExecuteConsumer);
        if (executor != null) {
            this.afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
        }

        return getSelf();
    }

    /**
     * Configures an {@code UncaughtExceptionHandler} for threads created by
     * this builder's factory. It must be configured before the executor is
     * created. Failures captured by submitted tasks remain in their
     * {@code Future}s and do not invoke this handler.
     *
     * @param uncaughtExceptionConsumer the consumer for genuinely uncaught
     *                                  thread failures
     * @return the current thread builder.
     */
    public U setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer) {
        requireActive();
        if (executor != null) {
            throw new IllegalStateException("Uncaught exception handler must be configured before executor creation");
        }
        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);

        return getSelf();
    }

    /**
     * Sets the thread name supplier. The last supplier configured before the
     * first {@link #start()} or {@link #startAndBuildOther()} is used when the
     * executor and its thread factory are created. A later call is accepted
     * but does not reconfigure that factory, rename existing threads, or
     * change the names it gives to new threads. The default thread factory
     * consults the selected supplier once for each new thread. When an
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
        requireActive();
        this.threadNameSupplier = Optional.ofNullable(threadNameSupplier);

        return getSelf();
    }

    /**
     * Sets the thread name supplier and submits the configured task. The
     * supplier is consulted when the thread factory creates a thread, not for
     * each task. Equivalent to {@code setThreadNameSupplier(supplier).start()}.
     * Before the executor is created, this supplier replaces an earlier name
     * supplier. On a reused builder whose executor already exists, the task
     * starts but the supplied name does not change its factory or worker names.
     * The usual shutdown rule of {@link #start()} applies.
     *
     * @param threadNameSupplier the supplier for names of newly created threads
     * @return the submitted task's result and executor
     */
    public ExecutorResult<S> startAs(final Supplier<String> threadNameSupplier) {
        setThreadNameSupplier(threadNameSupplier);

        return start();
    }

    /**
     * Sets a priority supplier for platform threads. The default factory
     * consults the last supplier configured before the first submission once
     * when the executor is created; a supplied
     * {@link ExceptionCatcherThreadFactory} receives that value through its
     * setter. A later call is accepted but does not change the existing
     * factory or the priority of existing or future threads from that factory.
     * Priority is not a configurable virtual-thread property.
     *
     * @param threadPrioritySupplier the thread priority supplier.
     * @return the current thread builder.
     */
    public U setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier) {
        requireActive();
        this.threadPrioritySupplier = Optional.ofNullable(threadPrioritySupplier);

        return getSelf();
    }

    /**
     * Sets the execution for the next task submission.
     *
     * @param execution a {@link Runnable} or {@link Callable} task
     * @return the current thread builder.
     */
    protected U setExecution(final T execution) {
        requireActive();
        taskConfiguration = taskConfiguration.withExecution(Objects.requireNonNull(execution));

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
     * Creates or reuses the executor and submits the last task in a batch.
     * The result exposes that batch's futures and the executor. A nonperiodic
     * default batch receives orderly shutdown before this method returns;
     * shutdown does not wait for the futures or the executor to terminate.
     * After a successful open start, a new task must be configured before
     * starting again. A missing task is rejected without closing the executor.
     *
     * @return the executor's result after starting thread.
     * @see ExecutorResult
     */
    public ExecutorResult<S> start() {
        requireActive();
        requireExecutionNonNull();

        try {
            createExecutorAndRunThread();

            final TaskBatch<S> completedBatch = batch;
            final ExecutorResult<S> result;

            if (openMode || periodicTaskSubmitted) {
                result = new OpenExecutorResult<>(completedBatch);
                executorDelivered = true;
            } else {
                executor.shutdown();
                result = new ClosedExecutorResult<>(completedBatch);
                terminal = true;
            }

            executorResult = result;
            batch = null;

            return result;
        } catch (final RuntimeException | Error failure) {
            failStart(failure);

            throw failure;
        }
    }

    /**
     * Creates or reuses the executor, submits the task and returns this same
     * builder for another submission. The next task and its timing options
     * start empty. The pool size does not itself create multiple tasks.
     *
     * @return the current thread builder to prepare another thread.
     */
    public U startAndBuildOther() {
        requireActive();
        requireExecutionNonNull();

        try {
            createExecutorAndRunThread();
        } catch (final RuntimeException | Error failure) {
            failStart(failure);

            throw failure;
        }

        return getSelf();
    }

    /**
     * Submits one task to the executor and adds its future to the current batch.
     */
    protected void createExecutorAndRunThread() {
        requireExecutionNonNull();
        final TaskConfiguration<T> submittedTask = taskConfiguration;

        newExecutorServiceIfNull();

        runThread(submittedTask);
        taskConfiguration = TaskConfiguration.empty();
    }

    /**
     * Creates the thread executor (if necessary).
     */
    protected void newExecutorServiceIfNull() {
        if (executor == null) {
            executor = new ScheduledThreadBuilderExecutor(corePoolSize, getThreadFactory());
            afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
        }
    }

    protected void requireActive() {
        if (terminal) {
            throw new IllegalStateException("Task builder is no longer active");
        }
    }

    protected final boolean isOpenMode() {
        return openMode;
    }

    protected final void markPeriodicTaskSubmitted() {
        periodicTaskSubmitted = true;
    }

    protected final void copyToOpen(final AbstractThreadBuilder<S, T, ?> target) {
        requireActive();
        target.afterExecuteConsumer = afterExecuteConsumer;
        target.uncaughtExceptionConsumer = uncaughtExceptionConsumer;
        target.threadFactory = threadFactory;
        target.threadNameSupplier = threadNameSupplier;
        target.threadPrioritySupplier = threadPrioritySupplier;
        target.executor = executor;
        target.executorResult = executorResult;
        target.batch = batch;
        target.taskConfiguration = taskConfiguration;
        target.openMode = true;
        target.periodicTaskSubmitted = periodicTaskSubmitted;
        target.executorDelivered = executorDelivered;
        terminal = true;
    }

    private void failStart(final Throwable failure) {
        terminal = true;

        if (!executorDelivered && executor != null) {
            try {
                executor.shutdown();
            } catch (final RuntimeException | Error shutdownFailure) {
                failure.addSuppressed(shutdownFailure);
            }
        }
    }

    /** Requires an execution for the next submission. */
    protected void requireExecutionNonNull() {
        if (taskConfiguration.execution() == null) {
            throw new IllegalStateException("Configure a task before starting it");
        }
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
    void runThread(final TaskConfiguration<T> submittedTask) {
        submitToBatch(() -> submit(submittedTask));
    }

    protected <F extends Future<S>> F submitToBatch(final Supplier<F> submission) {
        newExecutorResultIfNull();

        final F future = submission.get();
        batch.addFuture(future);

        return future;
    }

    /**
     * Submits the thread's implementation to the executor.
     *
     * @return the Future object.
     * @see ScheduledThreadPoolExecutor#submit(java.util.concurrent.Callable)
     */
    @SuppressWarnings({"unchecked"})
    Future<S> submit(final TaskConfiguration<T> submittedTask) {
        final Future<S> future;

        switch (executionType) {
            case CALLABLE:
                future = executor.submit((Callable<S>) submittedTask.execution());

                break;
            case RUNNABLE:
            default:
                future = (Future<S>) executor.submit((Runnable) submittedTask.execution());

                break;
        }

        return future;
    }

    /**
     * Creates mutable state for the current batch if necessary.
     */
    protected void newExecutorResultIfNull() {
        batch = batch == null ? new TaskBatch<>(executor) : batch;
    }

    /**
     * @return the type of the thread implementation.
     */
    abstract Class<?> getExecutionClass();

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
