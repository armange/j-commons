/*
 * Copyright [2020] [Diego Armange Costa]
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

import br.com.armange.commons.thread.exception.UncheckedException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Asynchronous builder for an already-open map of {@link Closeable}
 * resources. Closure runs in an after-execution callback in map-values
 * iteration order, stopping at the first {@code IOException}; unlike Java
 * try-with-resources, later closing failures are not suppressed on the first.
 * The inherited fluent type currently causes {@code ClassCastException} when
 * {@code addCatcher()} or {@code finallyAsync()} is chained.
 *
 * @author Diego Armange Costa
 * @see br.com.armange.commons.thread.async.TryAsyncBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class MappedResourcesTryAsyncBuilder
        extends AbstractTryAsyncBuilder<ResourceTryAsyncBuilder> {
    private final Map<Object, Closeable> closeableMap;
    private final ResourcesMapConsumer attemptedExecution;

    private MappedResourcesTryAsyncBuilder(final Map<Object, Closeable> closeableMap,
                                           final ResourcesMapConsumer attemptedExecution) {
        this.closeableMap = closeableMap;
        this.attemptedExecution = attemptedExecution;

        addFinalizer(() ->
                closeableMap.values().forEach(closeable -> {
                    try {
                        closeable.close();
                    } catch (final IOException e) {
                        throw new UncheckedException(e);
                    }
                })
        );
    }

    /**
     * Generates a new thread builder to configure its operations simulating a try-with-resources
     * implementation.
     *
     * @param closeableMap       The closeable resources map.
     * @param attemptedExecution The implementation that will consumes the resources.
     * @return A new thread builder.
     */
    protected static MappedResourcesTryAsyncBuilder tryAsync(
            final Map<Object, Closeable> closeableMap,
            final ResourcesMapConsumer attemptedExecution) {
        return new MappedResourcesTryAsyncBuilder(closeableMap, attemptedExecution);
    }

    /**
     * Submits the task with the supplied map. Resource closing happens in
     * an after-execution callback, not before this method returns. A closing
     * {@code IOException} is wrapped and logged by the executor callback;
     * no public {@code Future} exposes that failure.
     *
     * @see AbstractTryAsyncBuilder#execute(RunnableWithException)
     */
    @Override
    public void execute() {
        execute(() -> {
            try {
                attemptedExecution.accept(closeableMap);
            } catch (final Exception e) {
                throw new AsyncRuntimeException(e);
            }
        });
    }

}
