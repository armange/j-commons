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

final class SingleRunnableTaskThreadBuilder implements SingleTaskThreadBuilder<Void, Runnable> {
    private final CommonRunnableThreadBuilder delegate;
    private final TaskStageState<Void> state;

    SingleRunnableTaskThreadBuilder(final int corePoolSize, final Runnable execution) {
        delegate = CommonRunnableThreadBuilder.newBuilder(corePoolSize);
        state = new TaskStageState<>(delegate);
        setExecution(execution);
    }

    @Override
    public SingleRunnableTaskThreadBuilder setExecution(final Runnable execution) {
        state.configureSingle(() -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public SingleRunnableTaskThreadBuilder setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureSingle(() -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public SingleRunnableTaskThreadBuilder setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureSingle(() -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public SingleRunnableTaskThreadBuilder setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureSingle(() -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public SingleRunnableTaskThreadBuilder setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureSingle(() -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public SingleRunnableTaskThreadBuilder setSilentInterruption(final boolean flag) {
        state.configureSingle(() -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public SingleRunnableTaskThreadBuilder keepExecutorOpen() {
        state.keepExecutorOpenSingle();

        return this;
    }

    @Override
    public MultipleTaskThreadBuilder<Void, Runnable> startAndBuildOther() {
        final long batchId = state.beginMultiple();

        return new MultipleRunnableTaskThreadBuilder(state, delegate, batchId);
    }

    @Override
    public SingleTaskExecutorResult<Void> start() {
        return state.startSingle();
    }
}
