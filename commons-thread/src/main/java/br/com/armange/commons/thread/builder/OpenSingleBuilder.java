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
public sealed interface OpenSingleBuilder<S, T>
        permits OpenSingleRunnableBuilder, OpenSingleCallableBuilder {

    OpenSingleBuilder<S, T> setExecution(final T execution);

    OpenSingleBuilder<S, T> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    OpenSingleBuilder<S, T> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    OpenSingleBuilder<S, T> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    OpenSingleBuilder<S, T> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    OpenSingleBuilder<S, T> setSilentInterruption(final boolean flag);

    OpenMultipleBuilder<S, T> startAndBuildOther();

    /**
     * Submits one task without waiting for it to finish and returns the
     * caller-owned executor with its future.
     *
     * @return the task future and its shared executor
     */
    SingleThreadExecutorResult<S> start();
}
