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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Exposes the future of a task submitted by an open stage and its executor.
 * The caller owns executor shutdown. Future completion can occur after this
 * result is returned.
 *
 * @param <T> the task result type, or {@link Void} for a {@link Runnable}
 */
public sealed interface SingleThreadExecutorResult<T> permits SingleThreadResultSnapshot {

    /**
     * @return the future of the submitted task
     */
    Future<T> getFuture();

    /**
     * @return the caller-owned executor that accepted the task
     */
    ExecutorService getExecutorService();
}
