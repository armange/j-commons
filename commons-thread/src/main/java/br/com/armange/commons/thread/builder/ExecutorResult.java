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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The futures of one submission batch and the executor that accepted them.
 * {@link ClosedExecutorResult} means orderly shutdown was requested, not that
 * the futures completed or the executor terminated. The caller must shut down
 * the executor returned by an {@link OpenExecutorResult}.
 */
public sealed interface ExecutorResult<T> permits OpenExecutorResult, ClosedExecutorResult {

    /** Returns the immutable futures of this batch in submission order. */
    List<Future<T>> getFutures();

    /** Returns the future at its zero-based submission index. */
    default Future<T> getFuture(final int index) {
        return getFutures()
                .get(index);
    }

    /**
     * Waits for and returns the result of the future at the zero-based index.
     * Equivalent to {@code getFuture(index).get()}; it does not change the
     * executor lifecycle. A runnable task returns {@code null}. An active
     * periodic series does not normally complete, so this call may keep waiting.
     *
     * @param index the zero-based submission index
     * @return the task result, or {@code null} for a runnable task
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException if the task failed
     * @throws CancellationException if the future was cancelled
     * @throws IndexOutOfBoundsException if the index does not identify a future
     */
    default T getFutureResult(final int index) throws InterruptedException, ExecutionException {
        return getFuture(index)
                .get();
    }

    /** Returns the executor, including after orderly shutdown was requested. */
    ExecutorService getExecutorService();

}
