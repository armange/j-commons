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

/**
 * Builder for an asynchronous {@link RunnableWithException} with optional
 * exception catchers and finalizers. Its {@code execute()} method returns
 * {@code void} and does not expose the internally created executor.
 *
 * @author Diego Armange Costa
 * @see br.com.armange.commons.thread.async.TryAsyncBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class RunnableTryAsyncBuilder extends AbstractTryAsyncBuilder<RunnableTryAsyncBuilder> {
    private final RunnableWithException attemptedExecution;

    private RunnableTryAsyncBuilder(final RunnableWithException runnable) {
        attemptedExecution = runnable;
    }

    /**
     * Creates a thread builder.
     *
     * @param execution the thread implementation.
     * @return the current thread builder.
     */
    protected static RunnableTryAsyncBuilder tryAsync(final RunnableWithException execution) {
        return new RunnableTryAsyncBuilder(execution);
    }

    /**
     * Submits the task and returns without waiting for task or callbacks.
     *
     * @see AbstractTryAsyncBuilder#execute(RunnableWithException)
     */
    @Override
    public void execute() {
        execute(attemptedExecution);
    }
}
