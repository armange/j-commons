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

final class OpenSingleRunnableBuilder implements OpenSingleThreadBuilder<Void, Runnable> {
    private final CommonRunnableBuilder delegate;
    private final TaskStageState<Void> state;

    OpenSingleRunnableBuilder(final TaskStageState<Void> state, final CommonRunnableBuilder delegate) {
        this.state = state;
        this.delegate = delegate;
    }

    @Override
    public OpenSingleRunnableBuilder setExecution(final Runnable execution) {
        state.configureOpenSingle(() -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public OpenSingleRunnableBuilder setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureOpenSingle(() -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public OpenSingleRunnableBuilder setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureOpenSingle(() -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public OpenSingleRunnableBuilder setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureOpenSingle(() -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public OpenSingleRunnableBuilder setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureOpenSingle(() -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public OpenSingleRunnableBuilder setSilentInterruption(final boolean flag) {
        state.configureOpenSingle(() -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public OpenMultipleThreadBuilder<Void, Runnable> startAndBuildOther() {
        final long batchId = state.beginOpenMultiple();

        return new OpenMultipleRunnableBuilder(state, delegate, batchId);
    }

    @Override
    public SingleThreadExecutorResult<Void> start() {
        return state.startOpenSingle();
    }
}
