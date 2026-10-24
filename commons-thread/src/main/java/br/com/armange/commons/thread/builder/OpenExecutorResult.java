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

/** A batch result whose executor remains open and must be closed by the caller. */
public final class OpenExecutorResult<T> implements ExecutorResult<T> {
    private final TaskBatch<T> batch;
    private final List<Future<T>> futures;

    OpenExecutorResult(final TaskBatch<T> batch) {
        this.batch = batch;
        futures = batch.futures();
    }

    @Override
    public List<Future<T>> getFutures() {
        return futures;
    }

    @Override
    public ExecutorService getExecutorService() {
        return batch.executor();
    }

}
