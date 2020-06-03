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
import java.io.IOException;
import java.util.function.Consumer;

public class ResourceTryAsyncBuilder extends AbstractTryAsyncBuilder<ResourceTryAsyncBuilder> {
    private final Closeable closeable;
    private final Consumer<Closeable> attemptedExecution;

    private ResourceTryAsyncBuilder(final Closeable closeable, final Consumer<Closeable> attemptedExecution) {
        this.closeable = closeable;
        this.attemptedExecution = attemptedExecution;

        addFinalizer(() -> {
            try {
                closeable.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected static ResourceTryAsyncBuilder tryAsync(final Closeable closeable,
            final Consumer<Closeable> attemptedExecution) {
        return new ResourceTryAsyncBuilder(closeable, attemptedExecution);
    }

    @Override
    public void execute() {
        execute(() -> attemptedExecution.accept(closeable));
    }

}
