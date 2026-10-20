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
package br.com.armange.commons.thread.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

final class ThreadFactories {
    enum Mode {
        PLATFORM, VIRTUAL
    }

    enum Purpose {
        TASK, TIMER
    }

    private ThreadFactories() {
    }

    static ThreadFactory select(final Mode mode, final Purpose purpose) {
        if (purpose == Purpose.TIMER) {
            return Executors.defaultThreadFactory();
        }

        if (mode == Mode.VIRTUAL) {
            return Thread.ofVirtual().factory();
        }

        return Thread::new;
    }
}
