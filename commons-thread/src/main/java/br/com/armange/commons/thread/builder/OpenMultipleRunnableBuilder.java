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

final class OpenMultipleRunnableBuilder implements OpenMultipleThreadBuilder<Void, Runnable> {
    private final TaskStageState<Void> state;
    private final CommonRunnableBuilder delegate;
    private final long batchId;

    OpenMultipleRunnableBuilder(final TaskStageState<Void> state,
                                      final CommonRunnableBuilder delegate,
                                      final long batchId) {
        this.state = state;
        this.delegate = delegate;
        this.batchId = batchId;
    }

    @Override
    public OpenMultipleRunnableBuilder setExecution(final Runnable execution) {
        state.configureOpenMultiple(batchId, () -> delegate.setExecution(execution));

        return this;
    }

    @Override
    public OpenMultipleRunnableBuilder setAfterExecuteConsumer(
            final BiConsumer<Runnable, Throwable> consumer) {
        state.configureOpenMultiple(batchId, () -> delegate.setAfterExecuteConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleRunnableBuilder setUncaughtExceptionConsumer(
            final Consumer<? super Throwable> consumer) {
        state.configureOpenMultiple(batchId, () -> delegate.setUncaughtExceptionConsumer(consumer));

        return this;
    }

    @Override
    public OpenMultipleRunnableBuilder setThreadNameSupplier(final Supplier<String> supplier) {
        state.configureOpenMultiple(batchId, () -> delegate.setThreadNameSupplier(supplier));

        return this;
    }

    @Override
    public OpenMultipleRunnableBuilder setThreadPrioritySupplier(final IntSupplier supplier) {
        state.configureOpenMultiple(batchId, () -> delegate.setThreadPrioritySupplier(supplier));

        return this;
    }

    @Override
    public OpenMultipleRunnableBuilder setSilentInterruption(final boolean flag) {
        state.configureOpenMultiple(batchId, () -> delegate.setSilentInterruption(flag));

        return this;
    }

    @Override
    public OpenMultipleRunnableBuilder startAndBuildOther() {
        state.submitOpenMultiple(batchId);

        return this;
    }

    @Override
    public MultipleThreadExecutorResult<Void> start() {
        return state.finishOpenMultiple(batchId);
    }
}
