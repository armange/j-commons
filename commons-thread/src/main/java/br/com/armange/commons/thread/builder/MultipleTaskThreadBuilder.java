/*
 * Copyright [2026] [Diego Armange Costa]
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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Configures another task after at least one task has been submitted to the
 * same executor. Starting this state returns an immutable snapshot of the
 * current batch's futures in submission order. This state cannot be reused
 * after {@code start()}. By default, that start requests orderly executor
 * shutdown and makes the single-task alias terminal. With
 * {@link #keepExecutorOpen()}, the caller owns shutdown and can use the alias
 * to begin another batch on the same executor.
 *
 * @param <S> the task result type, or {@link Void} for a {@link Runnable}
 * @param <T> the task type, either {@link Runnable} or {@link java.util.concurrent.Callable}
 */
public sealed interface MultipleTaskThreadBuilder<S, T>
        permits MultipleRunnableTaskThreadBuilder, MultipleCallableTaskThreadBuilder {

    MultipleTaskThreadBuilder<S, T> setExecution(final T execution);

    MultipleTaskThreadBuilder<S, T> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    MultipleTaskThreadBuilder<S, T> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    MultipleTaskThreadBuilder<S, T> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    MultipleTaskThreadBuilder<S, T> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    MultipleTaskThreadBuilder<S, T> setSilentInterruption(final boolean flag);

    /**
     * Keeps the shared executor open after the final {@code start()} and
     * transfers responsibility for its shutdown to the caller. The policy
     * persists for later batches. Call this before the final start.
     *
     * @return this multiple-task stage
     */
    MultipleTaskThreadBuilder<S, T> keepExecutorOpen();

    /**
     * Submits the configured task and remains in the multiple-task state.
     *
     * @return this multiple-task state
     */
    MultipleTaskThreadBuilder<S, T> startAndBuildOther();

    /**
     * Submits the configured task.
     *
     * The default policy requests orderly executor shutdown before returning,
     * without waiting for or interrupting accepted tasks. With
     * {@link #keepExecutorOpen()}, the caller owns the shared executor.
     *
     * @return this batch's task futures and their shared executor
     */
    MultipleTaskExecutorResult<S> start();
}
