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

/** Open single Callable stage retaining its result callback across configuration. */
public sealed interface OpenSingleCallableTaskThreadBuilder<S>
        extends OpenSingleTaskThreadBuilder<S, Callable<S>>
        permits OpenSingleCallableTaskThreadBuilderImpl {

    @Override
    OpenSingleCallableTaskThreadBuilder<S> setExecution(final Callable<S> execution);

    @Override
    OpenSingleCallableTaskThreadBuilder<S> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> afterExecuteConsumer);

    @Override
    OpenSingleCallableTaskThreadBuilder<S> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> uncaughtExceptionConsumer);

    @Override
    OpenSingleCallableTaskThreadBuilder<S> setThreadNameSupplier(final Supplier<String> threadNameSupplier);

    @Override
    OpenSingleCallableTaskThreadBuilder<S> setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier);

    @Override
    OpenSingleCallableTaskThreadBuilder<S> setSilentInterruption(final boolean flag);

    OpenSingleCallableTaskThreadBuilder<S> setThreadResultConsumer(final Consumer<S> threadResultConsumer);

    @Override
    OpenMultipleCallableTaskThreadBuilder<S> startAndBuildOther();
}
