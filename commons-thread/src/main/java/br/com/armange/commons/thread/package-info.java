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
 * Utility package for thread creation.<br>
 * <ul>
 * <li><b>{@link br.com.armange.commons.thread.ThreadUtil}</b>
 * <p>Useful structure for handling the current thread.</p>
 * <pre>
 * ThreadUtil.sleepUnchecked(1000);
 * final URL url = ThreadUtil.getCurrentThreadResource("path");
 * final InputStream is = ThreadUtil.getCurrentThreadResourceAsStream("path");
 * </pre></li>
 * <li><b>{@link br.com.armange.commons.thread.ThreadBuilder}</b>
 * <p>Useful structure for thread creation</p>
 * <pre>
 * final ExecutorService thread = ThreadBuilder
 *          .newBuilder() //New object to build a new thread.
 *          .setTimeout(4000) //The thread will be canceled after four seconds.
 *          .setAfterExecuteConsumer(afterExecuteConsumer) //A consumer will be called after thread execution.
 *          .setUncaughtExceptionConsumer(throwableConsumer) //A consumer will be called after any exception thrown.
 *          .start();
 * </pre></li>
 * </ul>
 * */
package br.com.armange.commons.thread;