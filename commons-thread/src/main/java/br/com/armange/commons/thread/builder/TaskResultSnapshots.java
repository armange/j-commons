/*
 * Copyright [2026] [Diego Armange Costa]
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

record SingleTaskResultSnapshot<T>(Future<T> future, ExecutorService executorService)
        implements SingleTaskExecutorResult<T> {

    SingleTaskResultSnapshot {
        Objects.requireNonNull(future);
        Objects.requireNonNull(executorService);
    }

    @Override
    public Future<T> getFuture() {
        return future;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}

record MultipleTaskResultSnapshot<T>(List<Future<T>> futures, ExecutorService executorService)
        implements MultipleTaskExecutorResult<T> {

    MultipleTaskResultSnapshot {
        futures = List.copyOf(futures);
        Objects.requireNonNull(executorService);
    }

    @Override
    public List<Future<T>> getFutures() {
        return futures;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
