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
 * Multiple-task stage whose result transfers executor ownership to the caller.
 * Each stage is valid only until its final {@code start()}; the retained open
 * single-task stage can then begin another batch. A failure before the first
 * result closes the executor; after ownership transfers, a later failure
 * leaves shutdown to the caller.
 */
public sealed interface OpenMultipleBuilder<S, T>
        permits OpenMultipleRunnableBuilder, OpenMultipleCallableBuilder {

    OpenMultipleBuilder<S, T> setExecution(final T execution);

    OpenMultipleBuilder<S, T> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    OpenMultipleBuilder<S, T> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    OpenMultipleBuilder<S, T> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    OpenMultipleBuilder<S, T> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    OpenMultipleBuilder<S, T> setSilentInterruption(final boolean flag);

    OpenMultipleBuilder<S, T> startAndBuildOther();

    /**
     * Submits the final task without waiting for completion and returns this
     * batch's immutable futures in submission order with the shared executor.
     *
     * @return this batch's futures and caller-owned executor
     */
    MultipleThreadExecutorResult<S> start();
}
