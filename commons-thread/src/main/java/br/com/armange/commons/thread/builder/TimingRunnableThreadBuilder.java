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

public class TimingRunnableThreadBuilder<S>
        extends AbstractTimingThreadBuilder<S, Runnable, TimingRunnableThreadBuilder<S>> {

    private TimingRunnableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }
    
    protected static <T> TimingRunnableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new TimingRunnableThreadBuilder<>(corePoolSize);
    }
    
    @Override
    Class<Runnable> getExceutionClass() {
        return Runnable.class;
    }

}
