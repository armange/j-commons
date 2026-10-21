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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Exposes an immutable snapshot of a batch's futures in submission order.
 * By default the builder has requested orderly executor shutdown before this
 * result is returned. With {@code keepExecutorOpen()}, the caller owns its
 * shutdown. Accepted tasks may still be running when the result is returned.
 *
 * @param <T> the task result type, or {@link Void} for {@link Runnable} tasks
 */
public sealed interface MultipleTaskExecutorResult<T> permits MultipleTaskResultSnapshot {

    /**
     * @return an immutable snapshot of this batch's futures, in submission order
     */
    List<Future<T>> getFutures();

    /**
     * Returns a task future by its zero-based submission index.
     *
     * @param index the task's submission index, starting at zero
     * @return the future at the given index
     * @throws IndexOutOfBoundsException if the index is outside the futures list
     */
    default Future<T> getFuture(final int index) {
        return getFutures()
                .get(index);
    }

    /**
     * @return the executor that accepted the tasks, possibly already shut down
     */
    ExecutorService getExecutorService();
}
