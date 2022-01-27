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
 * It is a thread builder that assists in creating structured threads to simulate a set of
 * try-catch-finally or try-with-resources blocks.<br><br>
 *
 * <h3>Example without resources</h3>
 * <pre>
 *     &#064;Test
 *     public void shouldSayHello() throws InterruptedException {
 *         final StringBuilder message = new StringBuilder();
 *         final RunnableWithException sayHello = () -&gt; {
 *             message.append("Hello ");
 *             throw new Exception("Just a test");
 *         };
 *
 *         // try {
 *         //     message.append("Hello ");
 *         //     throw new Exception("Just a test");
 *         // } catch (final Exception e) {
 *         //     message.append("world");
 *         // } finally {
 *         //     message.append("!");
 *         // }
 *         tryAsync(sayHello)
 *                 .addCatcher(Exception.class,exception -&gt;
 *                         message.append("world"))
 *                 .addFinalizer(() -&gt; message.append("!"))
 *                 .execute();
 *
 *         Thread.sleep(1000);
 *         Assert.assertEquals("Hello world!",message.toString());
 *     }
 * </pre>
 *
 * <h3>Example with I/O</h3>
 * <pre>
 *     &#064;Test
 *     public void shouldWriteHello() throws IOException {
 *         final Path tempPath = Files.createTempFile("hello", ".txt");
 *         final File tempFile = new File(tempPath.toString());
 *         final FileWriter fileWriter = new FileWriter(tempFile);
 *         final String fileContent = "Hello world!";
 *
 *         // try (final FileWriter fileWriter = new FileWriter(tempFile)) {
 *         //     fileWriter.write(fileContent);
 *         //     fileWriter.flush();
 *         // } catch (final Exception e) {
 *         //     ...
 *         // }
 *         tryAsync(fileWriter, f -&gt; {
 *             fileWriter.write(fileContent);
 *             fileWriter.flush();
 *         }).execute();
 *
 *         final String content = Files.readAllLines(tempPath).get(0);
 *
 *         Assert.assertEquals(fileContent, content);
 *     }
 * </pre>
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public final class TryAsyncBuilder {

    private TryAsyncBuilder() {
    }

    /**
     * It creates a new thread builder that supports creating threads that are structured
     * to simulate a try-catch block code.
     *
     * @param execution The Runnable interface should be implemented by any class whose
     *                  instances are intended to be executed by a thread. The class must
     *                  define a method of no arguments called run.
     * @return A thread constructor with a defined thread implementation.
     */
    public static RunnableTryAsyncBuilder tryAsync(final RunnableWithException execution) {
        return RunnableTryAsyncBuilder.tryAsync(execution);
    }

    /**
     * This creates a new thread builder that supports creating threads that are structured
     * to simulate a try-catch block code.
     *
     * @param callable       A task that returns a result and may throw an exception.
     * @param resultConsumer The consumer for the thread's execution result.
     * @param <T>            The type of the object resulting from the execution of the thread.
     * @return A thread constructor with a defined thread implementation and an implementation
     * of a consumer for the object resulting from the thread's execution.
     */
    public static <T> CallableTryAsyncBuilder<T> tryAsync(final Callable<T> callable,
                                                          final Consumer<T> resultConsumer) {
        return CallableTryAsyncBuilder.tryAsync(callable, resultConsumer);
    }

    /**
     * It creates a new thread builder that supports creating threads that are structured
     * to simulate a try-with-resources block code.<br>
     * The try-with-resources block will be simulated through a thread that will execute a
     * consumer receiving a closeable object as a parameter. The consumer of the closeable object
     * will be the only implementation executed within the thread, so this implementation
     * represents the try block of a set of try-with-resources blocks. After the thread runs
     * the closeable object will be closed. Any exception (which extends the
     * {@code Exception} class) that occurs within the execution (implementation)
     * of the thread, will be encapsulated in the {@code AsyncRuntimeException} class.
     *
     * @param closeable          A Closeable is a source or destination of data that can be closed.
     *                           The close method is invoked to release resources that the object
     *                           is holding (such as open files). This parameter corresponds to the
     *                           declaration of one or more items within the
     *                           try-with-resources block, which will later be automatically closed.
     * @param attemptedExecution The implementation of a consumer that will consume
     *                           an implementation of a Closeable.
     * @return A thread constructor with a defined thread implementation.
     */
    public static ResourceTryAsyncBuilder tryAsync(final Closeable closeable,
                                                   final ResourceConsumer attemptedExecution) {
        return ResourceTryAsyncBuilder.tryAsync(closeable, attemptedExecution);
    }

    /**
     * It creates a new thread builder that supports creating threads that are structured
     * to simulate a try-with-resources block code.<br>
     * The try-with-resources block will be simulated through a thread that will execute a
     * consumer receiving a closeable object as a parameter. The consumer of the closeable object
     * will be the only implementation executed within the thread, so this implementation
     * represents the try block of a set of try-with-resources blocks. After the thread runs
     * the closeable object will be closed. Any exception (which extends the
     * {@code Exception} class) that occurs within the execution (implementation)
     * of the thread, will be encapsulated in the {@code AsyncRuntimeException} class.
     *
     * @param attemptedExecution The implementation of a consumer that will consume
     *                           implementations of a Closeable.
     * @param resources          A Closeable is a source or destination of data that can be closed.
     *                           The close method is invoked to release resources that the object
     *                           is holding (such as open files). This parameter corresponds to the
     *                           declaration of one or more items within the try-with-resources
     *                           block, which will later be automatically closed.
     * @return A thread constructor with a defined thread implementation.
     */
    public static ResourcesTryAsyncBuilder tryAsync(final ResourcesConsumer attemptedExecution,
                                                    final Closeable... resources) {
        return ResourcesTryAsyncBuilder.tryAsync(attemptedExecution, resources);
    }

    /**
     * It creates a new thread builder that supports creating threads that are structured
     * to simulate a try-with-resources block code.<br>
     * The try-with-resources block will be simulated through a thread that will execute a
     * consumer receiving a closeable object as a parameter. The consumer of the closeable object
     * will be the only implementation executed within the thread, so this implementation
     * represents the try block of a set of try-with-resources blocks. After the thread runs
     * the closeable object will be closed. Any exception (which extends the
     * {@code Exception} class) that occurs within the execution (implementation)
     * of the thread, will be encapsulated in the {@code AsyncRuntimeException} class.
     *
     * @param closeableMap       A Closeable is a source or destination of data that can be closed.
     *                           The close method is invoked to release resources that the object
     *                           is holding (such as open files). This parameter corresponds to the
     *                           declaration of one or more items within the
     *                           try-with-resources block, which will later be automatically closed.
     * @param attemptedExecution The implementation of a consumer that will consume
     *                           implementations of a Closeable.
     * @return A thread constructor with a defined thread implementation.
     */
    public static MappedResourcesTryAsyncBuilder tryAsync(
            final Map<Object, Closeable> closeableMap,
            final ResourcesMapConsumer attemptedExecution) {
        return MappedResourcesTryAsyncBuilder.tryAsync(closeableMap, attemptedExecution);
    }
}
