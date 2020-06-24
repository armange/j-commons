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
package br.com.armange.commons.thread.builder;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
@SuppressWarnings("rawtypes")
public class SimpleCallableThreadBuilder<S> extends AbstractThreadBuilder<S, Callable, SimpleCallableThreadBuilder<S>>{

    protected SimpleCallableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    protected static <T> SimpleCallableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new SimpleCallableThreadBuilder<>(corePoolSize);
    }

    @Override
    public SimpleCallableThreadBuilder<S> setThreadResultConsumer(final Consumer<S> threadResultConsumer) {
        return super.setThreadResultConsumer(threadResultConsumer);
    }

    @Override
    Class<Callable> getExecutionClass() {
        return Callable.class;
    }

    @Override
    public SimpleCallableThreadBuilder<S> setExecution(final Callable execution) {
        return super.setExecution(execution);
    }
}
