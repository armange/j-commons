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
/**
 * Organizes task builders, asynchronous callbacks and executor utilities in
 * subpackages. Platform-task builders are in {@code builder}; the separate
 * virtual scheduling API is
 * {@link br.com.armange.commons.thread.core.VirtualScheduledThreadBuilderExecutor}
 * in {@code core}. It does not turn legacy builders into virtual-thread builders.
 *
 * <p>Use {@link br.com.armange.commons.thread.builder.ThreadBuilder} to create
 * tasks with the available builders and
 * {@link br.com.armange.commons.thread.async.TryAsyncBuilder} for the existing
 * asynchronous try-style API. Its {@code execute()} method returns
 * {@code void}, creates an internal executor and does not expose a
 * {@code Future} or close method.
 */
package br.com.armange.commons.thread;
