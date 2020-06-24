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

/**
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class SimpleRunnableThreadBuilder<S> extends AbstractThreadBuilder<S, Runnable, SimpleRunnableThreadBuilder<S>>{

    protected SimpleRunnableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    protected static <T> SimpleRunnableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new SimpleRunnableThreadBuilder<>(corePoolSize);
    }

    @Override
    Class<Runnable> getExecutionClass() {
        return Runnable.class;
    }

    @Override
    public SimpleRunnableThreadBuilder<S> setExecution(final Runnable execution) {
        return super.setExecution(execution);
    }
}
