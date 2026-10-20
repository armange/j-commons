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
 */
package br.com.armange.commons.thread.core;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * Result of a virtual task or a whole periodic series.
 *
 * <p>A periodic result remains incomplete after successful cycles. Its
 * {@link #get()} ends only after cancellation or failure. Cancellation makes
 * {@code get()} throw {@link java.util.concurrent.CancellationException}; it
 * does not prove that an already started body has stopped.
 *
 * @param <V> value type; {@link Void} for a Runnable or periodic series
 */
public interface VirtualScheduledTask<V> extends Future<V> {

    /**
     * Observes the absence of an active body or pending wait after this
     * task or series has terminated. The returned stage cannot change the
     * internal signal. It completes exceptionally if a body fails, including
     * when the failure occurs after {@link #cancel(boolean)} and can no longer
     * be reported by {@code get()}.
     *
     * @return a read-only completion signal for the task or series
     */
    CompletionStage<Void> quiescence();
}
