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

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Open multiple Callable stage retaining its result callback across submissions. */
public sealed interface OpenMultipleCallableTaskThreadBuilder<S>
        extends OpenMultipleTaskThreadBuilder<S, Callable<S>>
        permits OpenMultipleCallableTaskThreadBuilderImpl {

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> setExecution(final Callable<S> execution);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> setSilentInterruption(final boolean flag);

    OpenMultipleCallableTaskThreadBuilder<S> setThreadResultConsumer(final Consumer<S> threadResultConsumer);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> startAndBuildOther();
}
