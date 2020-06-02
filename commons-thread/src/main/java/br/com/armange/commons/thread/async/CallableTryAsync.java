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

public class CallableTryAsync <S> extends AbstractTryAsyncBuilder<CallableTryAsync<S>>{
    private final Callable<S> attemptedExecution;
    private final Consumer<S> resultConsumer;
    
    private CallableTryAsync(final Callable<S> callable, final Consumer<S> resultConsumer) {
        attemptedExecution = callable;
        this.resultConsumer = resultConsumer;
    }
    
    public static <S> CallableTryAsync<S> tryAsync(final Callable<S> callable, final Consumer<S> resultConsumer) {
        return new CallableTryAsync<S>(callable, resultConsumer);
    }
    
    @Override
    public void execute() {
        execute(attemptedExecution, resultConsumer);
    }
}
