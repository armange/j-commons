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

final class OpenSingleCallableBuilderImpl<S> implements OpenSingleCallableBuilder<S> {
    private final CommonCallableBuilder<S> delegate;
    private final TaskStageState<S> state;

    OpenSingleCallableBuilderImpl(final TaskStageState<S> state, final CommonCallableBuilder<S> delegate) {
        this.state = state;
        this.delegate = delegate;
    }

    @Override
    public OpenSingleCallableBuilder<S> setExecution(final Callable<S> execution) {
        state.configureOpenSingle(() -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public OpenSingleCallableBuilder<S> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureOpenSingle(() -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public OpenSingleCallableBuilder<S> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureOpenSingle(() -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public OpenSingleCallableBuilder<S> setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureOpenSingle(() -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public OpenSingleCallableBuilder<S> setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureOpenSingle(() -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public OpenSingleCallableBuilder<S> setSilentInterruption(final boolean flag) {
        state.configureOpenSingle(() -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public OpenSingleCallableBuilder<S> setThreadResultConsumer(final Consumer<S> consumer) {
        state.configureOpenSingle(() -> delegate.setThreadResultConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleCallableBuilder<S> startAndBuildOther() {
        final long batchId = state.beginOpenMultiple();

        return new OpenMultipleCallableBuilderImpl<>(state, delegate, batchId);
    }

    @Override
    public SingleThreadExecutorResult<S> start() {
        return state.startOpenSingle();
    }
}
