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

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Constructs a new thread simulating a try-catch-finally implementation that
 * generates a result value. The thread can contain one (or more) exception catcher,
 * such as a "catch" block. It can also contain a finisher implementation, such as a "finally" block.
 *
 * @author Diego Armange Costa
 * @see br.com.armange.commons.thread.async.TryAsyncBuilder
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class CallableTryAsyncBuilder<S> extends AbstractTryAsyncBuilder<CallableTryAsyncBuilder<S>> {
    private final Callable<S> attemptedExecution;
    private final Consumer<S> resultConsumer;

    private CallableTryAsyncBuilder(final Callable<S> callable, final Consumer<S> resultConsumer) {
        attemptedExecution = callable;
        this.resultConsumer = resultConsumer;
    }

    /**
     * Generates a new thread builder to configure its operations simulating a try-catch-finally
     * implementation.
     *
     * @param callable       the thread implementation representing the try-block.
     * @param resultConsumer the implementation that will consumes the thread result value.
     * @param <S>            the type of the object resulting from the execution of the thread.
     * @return a new thread builder
     */
    protected static <S> CallableTryAsyncBuilder<S> tryAsync(final Callable<S> callable, final Consumer<S> resultConsumer) {
        return new CallableTryAsyncBuilder<>(callable, resultConsumer);
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
        execute(attemptedExecution, resultConsumer);
    }
}
