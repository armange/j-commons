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

/**
 * It is a thread builder that assists in creating structured threads to simulate a set of
 * try-with-resources blocks.
 *
 * @author Diego Armange Costa
 * @see br.com.armange.commons.thread.async.TryAsyncBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class ResourceTryAsyncBuilder extends AbstractTryAsyncBuilder<ResourceTryAsyncBuilder> {
    private final Closeable closeable;
    private final ResourceConsumer attemptedExecution;

    private ResourceTryAsyncBuilder(final Closeable closeable,
                                    final ResourceConsumer attemptedExecution) {
        this.closeable = closeable;
        this.attemptedExecution = attemptedExecution;

        addFinalizer(() -> {
            try {
                closeable.close();
            } catch (final IOException e) {
                throw new UncheckedException(e);
            }
        });
    }

    /**
     * Generates a new thread builder to configure its operations simulating a try-with-resources
     * implementation.
     *
     * @param closeable          The closeable resource.
     * @param attemptedExecution The implementation that will consumes the resource.
     * @return A new thread builder.
     */
    protected static ResourceTryAsyncBuilder tryAsync(final Closeable closeable,
                                                      final ResourceConsumer attemptedExecution) {
        return new ResourceTryAsyncBuilder(closeable, attemptedExecution);
    }

    /**
     * From the configuration parameters previously provided in the thread constructor,
     * a new thread will be configured and executed so that it can simulate a try-catch-finally
     * code block or a try-with-resources code block.<br> The consumer of closable objects
     * will be executed taking the closable objects as a parameter.
     *
     * @see AbstractTryAsyncBuilder#execute(java.util.concurrent.Callable, java.util.function.Consumer)
     */
    @Override
    public void execute() {
        execute(() -> {
            try {
                attemptedExecution.accept(closeable);
            } catch (final Exception e) {
                throw new AsyncRuntimeException(e);
            }
        });
    }

}
