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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/** Mutable submission state retained until the final start of a batch. */
final class TaskBatch<T> {
    private final ExecutorService executor;
    private final List<Future<T>> futures = new ArrayList<>();

    TaskBatch(final ExecutorService executor) {
        this.executor = executor;
    }

    ExecutorService executor() {
        return executor;
    }

    void addFuture(final Future<T> future) {
        futures.add(future);
    }

    List<Future<T>> futures() {
        return List.copyOf(futures);
    }

}
