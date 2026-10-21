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
 * Configures a task before its first submission. Starting this state returns
 * the result of one task; submitting it while building another task moves to
 * the multiple-task state. This alias cannot be used while that multiple-task
 * state is active. By default, the final {@code start()} requests orderly
 * shutdown and makes this alias terminal. With {@link #keepExecutorOpen()},
 * the caller owns shutdown and can reuse this alias for another batch.
 *
 * @param <S> the task result type, or {@link Void} for a {@link Runnable}
 * @param <T> the task type, either {@link Runnable} or {@link java.util.concurrent.Callable}
 */
public sealed interface SingleTaskThreadBuilder<S, T>
        permits SingleRunnableTaskThreadBuilder, SingleCallableTaskThreadBuilder {

    SingleTaskThreadBuilder<S, T> setExecution(final T execution);

    SingleTaskThreadBuilder<S, T> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    SingleTaskThreadBuilder<S, T> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    SingleTaskThreadBuilder<S, T> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    SingleTaskThreadBuilder<S, T> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    SingleTaskThreadBuilder<S, T> setSilentInterruption(final boolean flag);

    /**
     * Keeps the executor open after the final {@code start()} and transfers
     * responsibility for its shutdown to the caller. The policy persists for
     * later batches using this builder. Call this before the final start.
     *
     * @return this single-task stage
     */
    SingleTaskThreadBuilder<S, T> keepExecutorOpen();

    /**
     * Submits the configured task and returns the state for another submission.
     *
     * @return a builder whose {@code start()} returns multiple task results
     */
    MultipleTaskThreadBuilder<S, T> startAndBuildOther();

    /**
     * Submits the configured task.
     *
     * The default policy requests orderly executor shutdown before returning,
     * without waiting for or interrupting a running task. With
     * {@link #keepExecutorOpen()}, the caller owns the executor.
     *
     * @return the future of this submission and its executor
     */
    SingleTaskExecutorResult<S> start();
}
