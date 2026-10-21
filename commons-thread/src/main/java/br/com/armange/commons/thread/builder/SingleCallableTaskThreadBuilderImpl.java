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
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

final class SingleCallableTaskThreadBuilderImpl<S> implements SingleCallableTaskThreadBuilder<S> {
    private final CommonCallableThreadBuilder<S> delegate;
    private final TaskStageState<S> state;

    SingleCallableTaskThreadBuilderImpl(final int corePoolSize, final Callable<S> execution) {
        delegate = CommonCallableThreadBuilder.newBuilder(corePoolSize);
        state = new TaskStageState<>(delegate);
        setExecution(execution);
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setExecution(final Callable<S> execution) {
        state.configureSingle(() -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureSingle(() -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureSingle(() -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureSingle(() -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureSingle(() -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setSilentInterruption(final boolean flag) {
        state.configureSingle(() -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public SingleCallableTaskThreadBuilder<S> setThreadResultConsumer(final Consumer<S> consumer) {
        state.configureSingle(() -> delegate.setThreadResultConsumer(consumer));

        return this;
    }

    @Override
    public OpenSingleCallableTaskThreadBuilder<S> keepExecutorOpen() {
        state.keepExecutorOpenSingle();

        return new OpenSingleCallableTaskThreadBuilderImpl<>(state, delegate);
    }

    @Override
    public MultipleCallableTaskThreadBuilder<S> startAndBuildOther() {
        final long batchId = state.beginMultiple();

        return new MultipleCallableTaskThreadBuilderImpl<>(state, delegate, batchId);
    }

    @Override
    public Future<S> start() {
        return state.startSingle()
                .getFuture();
    }
}
