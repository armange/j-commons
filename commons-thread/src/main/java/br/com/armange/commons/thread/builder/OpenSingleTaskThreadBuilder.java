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
 * Single-task stage whose result transfers executor ownership to the caller.
 * This stage can start another batch after a successful final {@code start()}.
 * While a multiple-task batch is active, it cannot submit or be configured.
 * A failure before the first result closes the executor; after ownership has
 * transferred, a later failure leaves shutdown to the caller.
 */
public sealed interface OpenSingleTaskThreadBuilder<S, T>
        permits OpenSingleRunnableTaskThreadBuilder, OpenSingleCallableTaskThreadBuilder {

    OpenSingleTaskThreadBuilder<S, T> setExecution(final T execution);

    OpenSingleTaskThreadBuilder<S, T> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    OpenSingleTaskThreadBuilder<S, T> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    OpenSingleTaskThreadBuilder<S, T> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    OpenSingleTaskThreadBuilder<S, T> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    OpenSingleTaskThreadBuilder<S, T> setSilentInterruption(final boolean flag);

    OpenMultipleTaskThreadBuilder<S, T> startAndBuildOther();

    /**
     * Submits one task without waiting for it to finish and returns the
     * caller-owned executor with its future.
     *
     * @return the task future and its shared executor
     */
    SingleTaskExecutorResult<S> start();
}
