/*
 * Copyright [2020] [Diego Armange Costa]
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

import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Holds the builder's executor, task futures, optional timeout-executor
 * results and the latest callable value observed by an executor callback.
 * The lists are mutable and reused when the same builder submits more tasks.
 * The caller owns shutdown of the task executor and every separate timeout
 * executor; {@code Future.get()} does not await result consumers or other
 * after-execution callbacks.
 *
 * @author Diego Armange Costa
 * @see ScheduledThreadBuilderExecutor
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class ExecutorResult<T> {

    private final ExecutorService executorService;
    @SuppressWarnings("rawtypes")
    private final List<Future<T>> futures = new LinkedList<>();
    private final List<ExecutorResult<T>> timeoutExecutorResults = new LinkedList<>();
    private T threadResult;

    public ExecutorResult(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * @return the task executor, which the caller must shut down
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * @return the mutable list of submitted task futures
     */
    @SuppressWarnings("rawtypes")
    public List<Future<T>> getFutures() {
        return futures;
    }

    /**
     * Each task configured with a timeout adds a result holding its separate
     * cancellation executor. Its future list contains the original task
     * future, not a future for the cancellation action. The caller must shut
     * down each cancellation executor, even when the task finishes early.
     *
     * @return the timeout thread's {@link ExecutorResult}s
     */
    public List<ExecutorResult<T>> getTimeoutExecutorResults() {
        return timeoutExecutorResults;
    }

    /**
     * @return the latest callable value stored by the after-execution callback;
     *         it can still be unset when a task future has already completed
     */
    public T getThreadResult() {
        return threadResult;
    }

    /**
     * Sets the thread result object.
     *
     * @param threadResult the thread result object.
     */
    void setThreadResult(final T threadResult) {
        this.threadResult = threadResult;
    }
}
