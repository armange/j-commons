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

@SuppressWarnings("rawtypes")
public class TimingCallableThreadBuilder<S>
        extends AbstractTimingThreadBuilder<S, Callable, TimingCallableThreadBuilder<S>> {

    TimingCallableThreadBuilder() {}

    TimingCallableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }
    
    @Override
    public TimingCallableThreadBuilder setThreadResultConsumer(final Consumer<S> threadResultConsumer) {
        return super.setThreadResultConsumer(threadResultConsumer);
    }

    @Override
    Class<Callable> getExceutionClass() {
        return Callable.class;
    }
}
