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

/** Scheduled Runnable stage whose executor remains open for the caller. */
public final class OpenTimingRunnableBuilder extends TimingRunnableBuilder {
    OpenTimingRunnableBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    public OpenTimingRunnableBuilder setExecution(final Runnable execution) {
        super.setExecution(execution);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setTimeout(final long milliseconds) {
        super.setTimeout(milliseconds);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setDelay(final long milliseconds) {
        super.setDelay(milliseconds);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setInterval(final long milliseconds) {
        super.setInterval(milliseconds);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setMayInterruptIfRunning(final boolean flag) {
        super.setMayInterruptIfRunning(flag);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> consumer) {
        super.setAfterExecuteConsumer(consumer);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setUncaughtExceptionConsumer(final Consumer<? super Throwable> consumer) {
        super.setUncaughtExceptionConsumer(consumer);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setThreadNameSupplier(final Supplier<String> supplier) {
        super.setThreadNameSupplier(supplier);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder setThreadPrioritySupplier(final IntSupplier supplier) {
        super.setThreadPrioritySupplier(supplier);

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder keepExecutorOpen() {
        requireActive();

        return this;
    }

    @Override
    public OpenTimingRunnableBuilder startAndBuildOther() {
        super.startAndBuildOther();

        return this;
    }

    @Override
    public OpenExecutorResult<Void> start() {
        return (OpenExecutorResult<Void>) super.start();
    }
}
