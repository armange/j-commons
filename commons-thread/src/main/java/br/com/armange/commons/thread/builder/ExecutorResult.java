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
 * A bean that encapsulates a thread result, composed of an ExecutorService,
 * a Future list, and another ExecutorResult list for timed threads.<br>
 * The ExecutorResult list will be present when some thread has a timeout.<br>
 * A {@link Future} object will be present by each thread started.<br>
 * Consider seeing the logical relationship between
 * {@link java.util.concurrent.ExecutorService} and
 * {@link java.util.concurrent.Future} in their respective documentation.
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
     * @return the {@link java.util.concurrent.ExecutorService}
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * @return the {@link Future}s list.
     */
    @SuppressWarnings("rawtypes")
    public List<Future<T>> getFutures() {
        return futures;
    }

    /**
     * Whenever a thread times out, its respective ExecutorService and Future will be present in this list.
     *
     * @return the timeout thread's {@link ExecutorResult}s
     */
    public List<ExecutorResult<T>> getTimeoutExecutorResults() {
        return timeoutExecutorResults;
    }

    /**
     * @return the thread result object.
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
