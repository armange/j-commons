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

public class ThreadBuilder {

    private final int corePoolSize;
    
    private ThreadBuilder() {
        corePoolSize = 1;
    }
    
    private ThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    
    public static ThreadBuilder newBuilder(final int corePoolSize) {
        return new ThreadBuilder(corePoolSize);
    }
    
    public static ThreadBuilder newBuilder() {
        return new ThreadBuilder();
    }
    
    public TimingRunnableThreadBuilder<?> setExecution(final Runnable execution) {
        final TimingRunnableThreadBuilder<?> builder = new TimingRunnableThreadBuilder<>(corePoolSize);
        
        return builder.setExecution(execution);
    }
    
    public <S> TimingCallableThreadBuilder<S> setExecution(final Callable<S> execution) {
        final TimingCallableThreadBuilder<S> builder = new TimingCallableThreadBuilder<>(corePoolSize);
        
        return builder.setExecution(execution);
    }
}
