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
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

final class MultipleCallableBuilderImpl<S> implements MultipleCallableBuilder<S> {
    private final TaskStageState<S> state;
    private final CommonCallableBuilder<S> delegate;
    private final long batchId;

    MultipleCallableBuilderImpl(final TaskStageState<S> state,
                                          final CommonCallableBuilder<S> delegate,
                                          final long batchId) {
        this.state = state;
        this.delegate = delegate;
        this.batchId = batchId;
    }

    @Override
    public MultipleCallableBuilder<S> setExecution(final Callable<S> execution) {
        state.configureMultiple(batchId, () -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public MultipleCallableBuilder<S> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureMultiple(batchId, () -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public MultipleCallableBuilder<S> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureMultiple(batchId, () -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public MultipleCallableBuilder<S> setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureMultiple(batchId, () -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public MultipleCallableBuilder<S> setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureMultiple(batchId, () -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public MultipleCallableBuilder<S> setSilentInterruption(final boolean flag) {
        state.configureMultiple(batchId, () -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public MultipleCallableBuilder<S> setThreadResultConsumer(final Consumer<S> consumer) {
        state.configureMultiple(batchId, () -> delegate.setThreadResultConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleCallableBuilder<S> keepExecutorOpen() {
        state.keepExecutorOpenMultiple(batchId);

        return new OpenMultipleCallableBuilderImpl<>(state, delegate, batchId);
    }

    @Override
    public MultipleCallableBuilder<S> startAndBuildOther() {
        state.submitMultiple(batchId);

        return this;
    }

    @Override
    public List<Future<S>> start() {
        return state.finishMultiple(batchId)
                .getFutures();
    }
}
