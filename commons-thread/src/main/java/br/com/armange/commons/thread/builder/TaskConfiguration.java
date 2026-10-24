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

import java.time.Duration;
import java.util.Optional;

/** Settings owned by one pending or submitted task. */
record TaskConfiguration<T>(T execution, Optional<Duration> delay,
                            Optional<Duration> interval, Optional<Duration> timeout,
                            boolean mayInterruptIfRunning) {

    static <T> TaskConfiguration<T> empty() {
        return new TaskConfiguration<>(null, Optional.empty(), Optional.empty(),
                Optional.empty(), false);
    }

    TaskConfiguration<T> withExecution(final T task) {
        return new TaskConfiguration<>(task, delay, interval, timeout, mayInterruptIfRunning);
    }

    TaskConfiguration<T> withDelay(final Duration value) {
        return new TaskConfiguration<>(execution, Optional.of(value), interval, timeout,
                mayInterruptIfRunning);
    }

    TaskConfiguration<T> withInterval(final Duration value) {
        return new TaskConfiguration<>(execution, delay, Optional.of(value), timeout,
                mayInterruptIfRunning);
    }

    TaskConfiguration<T> withTimeout(final Duration value) {
        return new TaskConfiguration<>(execution, delay, interval, Optional.of(value),
                mayInterruptIfRunning);
    }

    TaskConfiguration<T> withMayInterruptIfRunning(final boolean flag) {
        return new TaskConfiguration<>(execution, delay, interval, timeout, flag);
    }
}
