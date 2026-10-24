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

/** Scheduled Callable stage whose executor remains open for the caller. */
public final class OpenTimingCallableBuilder<S> extends TimingCallableBuilder<S> {
    OpenTimingCallableBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    public OpenTimingCallableBuilder<S> setExecution(final Callable<S> execution) {
        super.setExecution(execution);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setTimeout(final long milliseconds) {
        super.setTimeout(milliseconds);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setDelay(final long milliseconds) {
        super.setDelay(milliseconds);

        return this;
    }

    /** {@inheritDoc} */
    @Override
    public OpenTimingCallableBuilder<S> setInterval(final long milliseconds) {
        super.setInterval(milliseconds);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setMayInterruptIfRunning(final boolean flag) {
        super.setMayInterruptIfRunning(flag);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> consumer) {
        super.setAfterExecuteConsumer(consumer);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setUncaughtExceptionConsumer(final Consumer<? super Throwable> consumer) {
        super.setUncaughtExceptionConsumer(consumer);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setThreadNameSupplier(final Supplier<String> supplier) {
        super.setThreadNameSupplier(supplier);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> setThreadPrioritySupplier(final IntSupplier supplier) {
        super.setThreadPrioritySupplier(supplier);

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> keepExecutorOpen() {
        requireActive();

        return this;
    }

    @Override
    public OpenTimingCallableBuilder<S> startAndBuildOther() {
        super.startAndBuildOther();

        return this;
    }

    @Override
    public OpenExecutorResult<S> start() {
        return (OpenExecutorResult<S>) super.start();
    }
}
