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

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Entry point for asynchronous task builders with exception catchers, finalizers
 * and overloads for already-open {@link Closeable} resources. This API is not
 * structured concurrency or Java's try-with-resources: {@code execute()} returns
 * {@code void}, creates an internal platform-thread executor and exposes neither
 * a {@code Future} nor manual executor closure. Shutdown is requested after
 * submission and termination follows task callbacks, without blocking
 * {@code execute()}.
 *
 * <p>For example, inside a method that declares {@code throws Exception}, a
 * caller can arrange a completion signal through a finalizer:
 * <pre>{@code
 * java.util.concurrent.CountDownLatch finished =
 *         new java.util.concurrent.CountDownLatch(1);
 * TryAsyncBuilder.tryAsync((RunnableWithException) () -> System.out.println("work"))
 *         .addCatcher(Exception.class, Throwable::printStackTrace)
 *         .finallyAsync(finished::countDown)
 *         .execute();
 * if (!finished.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
 *     throw new IllegalStateException("Task did not finish");
 * }
 * }</pre>
 * The signal observes that this finalizer ran; it does not supply a task result,
 * prove the internal executor has terminated at that instant or guarantee a
 * signal if an earlier finalizer fails.
 *
 * <p>For I/O, an already-open resource is passed to the task and closed later by
 * an after-execution callback. Await an explicit completion signal before reading
 * its output; calling {@code execute()} alone does not wait:
 * <pre>{@code
 * java.nio.file.Path file = java.nio.file.Files.createTempFile("hello", ".txt");
 * java.io.FileWriter writer = new java.io.FileWriter(file.toFile());
 * java.util.concurrent.CountDownLatch closed =
 *         new java.util.concurrent.CountDownLatch(1);
 * TryAsyncBuilder.tryAsync(writer, out ->
 *         ((java.io.FileWriter) out).write("Hello world!"))
 *         .finallyAsync(closed::countDown)
 *         .execute();
 * if (!closed.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
 *     throw new IllegalStateException("Resource finalization did not finish");
 * }
 * String content = java.nio.file.Files.readString(file);
 * }</pre>
 * The resource-closing finalizer precedes the added signal. If it fails, the
 * signal may not run and this API provides no {@code Future} for that failure.
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public final class TryAsyncBuilder {

    private TryAsyncBuilder() {
    }

    /**
     * Creates a builder for an asynchronous task with optional catchers and
     * finalizers. Its {@code execute()} method returns {@code void}.
     *
     * @param execution the task, which may throw a checked exception
     * @return a builder for the task
     */
    public static RunnableTryAsyncBuilder tryAsync(final RunnableWithException execution) {
        return RunnableTryAsyncBuilder.tryAsync(execution);
    }

    /**
     * Creates a builder for an asynchronous {@link Callable} and result
     * consumer. The consumer runs after the task's internal future completes,
     * before finalizers, and is not awaited by a caller of {@code execute()}.
     *
     * @param callable       the task that returns a value
     * @param resultConsumer the callback for a successful value
     * @param <T>            the result type
     * @return a builder for the task and result consumer
     */
    public static <T> CallableTryAsyncBuilder<T> tryAsync(final Callable<T> callable,
                                                          final Consumer<T> resultConsumer) {
        return CallableTryAsyncBuilder.tryAsync(callable, resultConsumer);
    }

    /**
     * Creates a builder that passes an already-open resource to an asynchronous
     * task. A finalizer closes the resource after task completion; this is not
     * Java try-with-resources and {@code execute()} does not await closure.
     * An {@code Exception} from the task is wrapped in
     * {@link AsyncRuntimeException}; an {@code IOException} from closing is
     * wrapped in an unchecked exception in the after-execution callback and
     * logged there, without a public {@code Future} for the closing failure.
     *
     * @param closeable          the already-open resource passed to and later
     *                           closed after the task
     * @param attemptedExecution the task that receives the resource
     * @return a builder for the resource task
     */
    public static ResourceTryAsyncBuilder tryAsync(final Closeable closeable,
                                                   final ResourceConsumer attemptedExecution) {
        return ResourceTryAsyncBuilder.tryAsync(closeable, attemptedExecution);
    }

    /**
     * Creates a builder for an already-open array of resources. The task
     * receives that array; an after-execution finalizer closes its elements in
     * array order. Closing stops at the first {@code IOException} and does not
     * aggregate suppressed exceptions like Java try-with-resources.
     * This variant currently has a fluent-type defect: chaining
     * {@code addCatcher()} or {@code finallyAsync()} can throw
     * {@code ClassCastException}.
     *
     * @param attemptedExecution the task that receives the resource array
     * @param resources          already-open resources closed after the task
     * @return a builder for the resource task
     */
    public static ResourcesTryAsyncBuilder tryAsync(final ResourcesConsumer attemptedExecution,
                                                    final Closeable... resources) {
        return ResourcesTryAsyncBuilder.tryAsync(attemptedExecution, resources);
    }

    /**
     * Creates a builder for an already-open map of resources. The task receives
     * the map; an after-execution finalizer closes {@code values()} in map
     * iteration order. Closing stops at the first {@code IOException} and does
     * not aggregate suppressed exceptions like Java try-with-resources.
     * This variant currently has a fluent-type defect: chaining
     * {@code addCatcher()} or {@code finallyAsync()} can throw
     * {@code ClassCastException}.
     *
     * @param closeableMap       already-open resources passed to the task and
     *                           closed after it
     * @param attemptedExecution the task that receives the resource map
     * @return a builder for the resource task
     */
    public static MappedResourcesTryAsyncBuilder tryAsync(
            final Map<Object, Closeable> closeableMap,
            final ResourcesMapConsumer attemptedExecution) {
        return MappedResourcesTryAsyncBuilder.tryAsync(closeableMap, attemptedExecution);
    }
}
