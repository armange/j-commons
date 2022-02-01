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
 * It is a thread builder that assists in creating structured threads to simulate a set of
 * try-catch-finally or try-with-resources blocks.
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
     * From the configuration parameters previously provided in the thread constructor,
     * a new thread will be configured and executed so that it can simulate a try-catch-finally
     * code block or a try-with-resources code block.
     *
     * @see AbstractTryAsyncBuilder#execute(java.util.concurrent.Callable, java.util.function.Consumer)
     */
    @Override
    public void execute() {
        execute(attemptedExecution);
    }
}
