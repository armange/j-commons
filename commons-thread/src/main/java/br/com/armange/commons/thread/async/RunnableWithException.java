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
package br.com.armange.commons.thread.async;

/**
 * Represents an operation without arguments, without result and may throw an exception.
 *
 * @author Diego Armange Costa
 * @see Runnable
 * @since 2022-01-26 V1.1.0 (JDK 1.8)
 */
@FunctionalInterface
public interface RunnableWithException {

    /**
     * @throws Exception if the implementation fails.
     * @see Runnable#run()
     */
    void run() throws Exception;
}
