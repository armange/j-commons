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

final class OpenMultipleCallableTaskThreadBuilderImpl<S> implements OpenMultipleCallableTaskThreadBuilder<S> {
    private final TaskStageState<S> state;
    private final CommonCallableThreadBuilder<S> delegate;
    private final long batchId;

    OpenMultipleCallableTaskThreadBuilderImpl(final TaskStageState<S> state,
                                          final CommonCallableThreadBuilder<S> delegate,
                                          final long batchId) {
        this.state = state;
        this.delegate = delegate;
        this.batchId = batchId;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setExecution(final Callable<S> execution) {
        state.configureOpenMultiple(batchId, () -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureOpenMultiple(batchId, () -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureOpenMultiple(batchId, () -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureOpenMultiple(batchId, () -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureOpenMultiple(batchId, () -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setSilentInterruption(final boolean flag) {
        state.configureOpenMultiple(batchId, () -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> setThreadResultConsumer(final Consumer<S> consumer) {
        state.configureOpenMultiple(batchId, () -> delegate.setThreadResultConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleCallableTaskThreadBuilder<S> startAndBuildOther() {
        state.submitOpenMultiple(batchId);

        return this;
    }

    @Override
    public MultipleTaskExecutorResult<S> start() {
        return state.finishOpenMultiple(batchId);
    }
}
