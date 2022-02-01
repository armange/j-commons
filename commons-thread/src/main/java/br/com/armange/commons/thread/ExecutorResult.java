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
package br.com.armange.commons.thread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Diego Armange Costa
 * @see ScheduledCaughtExecutorService
 * @see br.com.armange.commons.thread.ThreadBuilder#start()
 * @see br.com.armange.commons.thread.ThreadBuilder
 * @since 2019-11-26 V1.0.0 (JDK 1.8)
 * @deprecated Consider to use {@link br.com.armange.commons.thread.builder.ExecutorResult}
 * A bean that wrappes a thread result, composed by a ExecutorService,
 * a Future list and another ExecutorResult list for timeouted threads.<br>
 * The ExecutorResult list will be present when some thread has a timeout.<br>
 * One Future will be present by each thread started.<br>
 * Consider seeing the logical relationship between
 * {@link java.util.concurrent.ExecutorService} and
 * {@link java.util.concurrent.Future} in their respective documentation.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class ExecutorResult {

    private final ExecutorService executorService;
    @SuppressWarnings("rawtypes")
    private final List<Future> futures = new LinkedList<>();
    private final List<ExecutorResult> timeoutExecutorResults = new LinkedList<>();

    public ExecutorResult(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Returns the {@link java.util.concurrent.ExecutorService} used to
     * start and control the thread.
     * @return the {@link java.util.concurrent.ExecutorService} used to
     * start and control the thread.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Returns the list of {@link Future} objects.
     * @return the list of {@link Future} objects.
     */
    @SuppressWarnings("rawtypes")
    public List<Future> getFutures() {
        return futures;
    }

    /**
     * Whenever a thread times out, its respective ExecutorService and Future will be present in this list.
     *
     * @return the timeout thread's {@link ExecutorResult}s
     */
    public List<ExecutorResult> getTimeoutExecutorResults() {
        return timeoutExecutorResults;
    }
}