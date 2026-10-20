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
 * Asynchronous builder for one already-open {@link Closeable}. A callback
 * closes the resource after task execution, separately from the task's
 * internal {@code Future}; this is not Java try-with-resources.
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
     * Submits the task with the already-open resource. An after-execution
     * finalizer closes it; an {@code IOException} from closing is wrapped in
     * {@code UncheckedException} and logged by the executor callback, not
     * returned to the caller. This method does not wait for task or closure.
     *
     * @see AbstractTryAsyncBuilder#execute(RunnableWithException)
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
