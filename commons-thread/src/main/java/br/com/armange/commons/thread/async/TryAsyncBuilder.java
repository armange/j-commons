/*
 * Copyright [2019] [Diego Armange Costa]
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
package br.com.armange.commons.thread.async;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class TryAsyncBuilder {

    private TryAsyncBuilder() {
    }

    public static RunnableTryAsyncBuilder tryAsync(final Runnable runnable) {
        return RunnableTryAsyncBuilder.tryAsync(runnable);
    }

    public static <T> CallableTryAsyncBuilder<T> tryAsync(final Callable<T> callable,
            final Consumer<T> resultConsumer) {
        return CallableTryAsyncBuilder.tryAsync(callable, resultConsumer);
    }

    public static ResourceTryAsyncBuilder tryAsync(final Closeable closeable,
            final Consumer<Closeable> attemptedExecution) {
        return ResourceTryAsyncBuilder.tryAsync(closeable, attemptedExecution);
    }

    public static ResourcesTryAsyncBuilder tryAsync(final Consumer<Closeable[]> attemptedExecution,
            final Closeable... closeables) {
        return ResourcesTryAsyncBuilder.tryAsync(attemptedExecution, closeables);
    }

    public static MappedResourcesTryAsyncBuilder tryAsync(final Map<Object, Closeable> closeableMap,
            final Consumer<Map<Object, Closeable>> attemptedExecution) {
        return MappedResourcesTryAsyncBuilder.tryAsync(closeableMap, attemptedExecution);
    }
}
