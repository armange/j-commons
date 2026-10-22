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

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Configures another task after at least one task has been submitted to the
 * same executor. Starting this state returns an immutable snapshot of the
 * current batch's futures in submission order. This state cannot be reused
 * after {@code start()}. By default, that start requests orderly executor
 * shutdown and makes the single-task alias terminal. {@link #keepExecutorOpen()}
 * selects a distinct stage that transfers shutdown responsibility to the caller.
 *
 * @param <S> the task result type, or {@link Void} for a {@link Runnable}
 * @param <T> the task type, either {@link Runnable} or {@link java.util.concurrent.Callable}
 */
public sealed interface MultipleBuilder<S, T>
        permits MultipleRunnableBuilder, MultipleCallableBuilder {

    MultipleBuilder<S, T> setExecution(final T execution);

    MultipleBuilder<S, T> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    MultipleBuilder<S, T> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    MultipleBuilder<S, T> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    MultipleBuilder<S, T> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    MultipleBuilder<S, T> setSilentInterruption(final boolean flag);

    /**
     * Selects the open-executor stage before the final {@code start()}.
     * Default-stage aliases then become invalid.
     *
     * @return the open multiple-task stage
     */
    OpenMultipleBuilder<S, T> keepExecutorOpen();

    /**
     * Submits the configured task and remains in the multiple-task state.
     *
     * @return this multiple-task state
     */
    MultipleBuilder<S, T> startAndBuildOther();

    /**
     * Submits the configured task.
     *
     * The default policy requests orderly executor shutdown before returning,
     * without waiting for or interrupting accepted tasks.
     *
     * @return immutable futures for this batch, in submission order
     */
    List<Future<S>> start();
}
