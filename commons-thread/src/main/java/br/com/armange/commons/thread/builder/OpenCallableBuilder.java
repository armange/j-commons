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

/** Callable stage that leaves executor shutdown to its caller. */
public final class OpenCallableBuilder<S> extends CommonCallableBuilder<S> {
    OpenCallableBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    public OpenCallableBuilder<S> setExecution(final Callable<S> execution) {
        super.setExecution(execution);

        return this;
    }

    @Override
    public OpenCallableBuilder<S> setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> consumer) {
        super.setAfterExecuteConsumer(consumer);

        return this;
    }

    @Override
    public OpenCallableBuilder<S> setUncaughtExceptionConsumer(final Consumer<? super Throwable> consumer) {
        super.setUncaughtExceptionConsumer(consumer);

        return this;
    }

    @Override
    public OpenCallableBuilder<S> setThreadNameSupplier(final Supplier<String> supplier) {
        super.setThreadNameSupplier(supplier);

        return this;
    }

    @Override
    public OpenCallableBuilder<S> setThreadPrioritySupplier(final IntSupplier supplier) {
        super.setThreadPrioritySupplier(supplier);

        return this;
    }

    @Override
    public OpenCallableBuilder<S> keepExecutorOpen() {
        requireActive();

        return this;
    }

    @Override
    public OpenCallableBuilder<S> startAndBuildOther() {
        super.startAndBuildOther();

        return this;
    }

    @Override
    public OpenExecutorResult<S> start() {
        return (OpenExecutorResult<S>) super.start();
    }
}
