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

import static br.com.armange.commons.thread.async.TryAsyncBuilder.tryAsync;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.junit.Test;

public class TryAsyncBuilderTest {
    private static final String THIS_EXCEPTION_WAS_EXPECTED = " -> This Exception was expected.";

    @Test
    public void tryRunnableWithoutExceptions() {
        final RunnableWithDelay runnableWithDelay = spy(new RunnableWithDelay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(runnableWithDelay).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(runnableWithDelay).run();
    }

    @Test
    public void tryRunnableWithExceptions() {
        final RunnableWithDelayAndException runnableWithDelayAndException = spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(runnableWithDelayAndException).addCatcher(RuntimeException.class, exceptionConsumer).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(runnableWithDelayAndException).run();
        verify(exceptionConsumer).accept(any());
    }

    @Test
    public void tryRunnableCatchExceptionAndFinalizer() {
        final RunnableWithDelayAndException runnableWithDelayAndException = spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(runnableWithDelayAndException).addCatcher(RuntimeException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(runnableWithDelayAndException).run();
        verify(exceptionConsumer).accept(any());
        verify(simpleRunnable, times(2)).run();
    }

    @Test
    public void tryRunnableWithExceptionsAndFinalizerAndCatchThrowing() {
        final RunnableWithDelayAndException runnableWithDelayAndException = spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer = spy(new ExceptionConsumerThrowingException());

        tryAsync(runnableWithDelayAndException).addCatcher(RuntimeException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(runnableWithDelayAndException).run();
        // Pass in exception consumer and thread uncaught exception method.
        // Because of this, the consumer must be verified two times.
        verify(exceptionConsumer, times(2)).accept(any());
        verify(simpleRunnable, times(3)).run();
    }

    @Test
    public void tryRunnableWithExceptionsAndFinalizer() {
        final RunnableWithDelayAndException runnableWithDelayAndException = spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer = spy(new ExceptionConsumerThrowingException());

        tryAsync(runnableWithDelayAndException).addCatcher(IOException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(runnableWithDelayAndException).run();
        verify(simpleRunnable, times(2)).run();
    }

    @Test
    public void tryRunnableAndThrowExceptionWithoutCatch() {
        final RunnableWithDelayAndException runnableWithDelayAndException = spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(runnableWithDelayAndException).finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(runnableWithDelayAndException).run();
        verify(simpleRunnable, times(2)).run();
    }

    /*
     * * * * * * * * Callable tests. * * * * * * * *
     */

    @Test
    public void tryCallableWithoutExceptions() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeay callableWithDeayAndException = spy(new CallableWithDeay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(callableWithDeayAndException, callableResultConsumer).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(callableWithDeayAndException).call();
        assertNotNull(callableResultConsumer.result);
    }

    @Test
    public void tryCallableWithExceptions() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeayAndException callableWithDeayAndException = spy(new CallableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(callableWithDeayAndException, callableResultConsumer)
                .addCatcher(RuntimeException.class, exceptionConsumer).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        verify(exceptionConsumer).accept(any());
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void tryCallableWithExceptionsAndFinalizerAndCatchThrowing() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeayAndException callableWithDeayAndException = spy(new CallableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer = spy(new ExceptionConsumerThrowingException());

        tryAsync(callableWithDeayAndException, callableResultConsumer)
                .addCatcher(RuntimeException.class, exceptionConsumer).finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        // Pass in exception consumer and thread uncaught exception method.
        // Because of this, the consumer must be verified two times.
        verify(exceptionConsumer, times(2)).accept(any());
        verify(simpleRunnable, times(3)).run();
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void tryCallableCatchExceptionAndFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeayAndException callableWithDeayAndException = spy(new CallableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(callableWithDeayAndException, callableResultConsumer)
                .addCatcher(RuntimeException.class, exceptionConsumer).finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        verify(exceptionConsumer).accept(any());
        verify(simpleRunnable, times(2)).run();
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void tryCallableAndThrowExceptionWithoutCatch() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeayAndException callableWithDeayAndException = spy(new CallableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(callableWithDeayAndException, callableResultConsumer).finallyAsync(simpleRunnable).execute();

        simpleRunnable.run();

        verify(simpleRunnable).run();

        sleepUnchecked(2000);

        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        verify(simpleRunnable, times(2)).run();
        assertNull(callableResultConsumer.result);
    }

    /*
     * * * * * * * * Resources tests. * * * * * * * *
     */

    @Test
    public void tryWithResource() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final CloseableConsumer closeableConsumer = spy(new CloseableConsumer());

        TryAsyncBuilder.tryAsync(inputStream, closeableConsumer).execute();

        sleepUnchecked(1500);

        verify(closeableConsumer).accept(inputStream);
        verify(inputStream).close();
    }

    @Test
    public void tryWithResources() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final CloseableArrayConsumer closeableArrayConsumer = spy(new CloseableArrayConsumer());

        TryAsyncBuilder.tryAsync(closeableArrayConsumer, inputStream, inputStream2).execute();

        sleepUnchecked(1500);

        verify(closeableArrayConsumer).accept(new Closeable[] { inputStream, inputStream2 });
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void tryWithMappedResource() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final MappedCloseableConsumer mappedCloseableConsumer = spy(new MappedCloseableConsumer());
        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);
        map.put(2, inputStream2);

        TryAsyncBuilder.tryAsync(map, mappedCloseableConsumer).execute();

        sleepUnchecked(1500);

        verify(mappedCloseableConsumer).accept(map);
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void tryWithResourceWithException() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final CloseableConsumerWithException closeableConsumerWithException = spy(new CloseableConsumerWithException());

        TryAsyncBuilder.tryAsync(inputStream, closeableConsumerWithException).execute();

        sleepUnchecked(1500);

        verify(closeableConsumerWithException).accept(inputStream);
        verify(inputStream).close();
    }

    @Test
    public void tryWithResourcesWithException() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final CloseableArrayConsumerWithException closeableArrayConsumerWithException = spy(
                new CloseableArrayConsumerWithException());

        TryAsyncBuilder.tryAsync(closeableArrayConsumerWithException, inputStream, inputStream2).execute();

        sleepUnchecked(1500);

        verify(closeableArrayConsumerWithException).accept(new Closeable[] { inputStream, inputStream2 });
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void tryWithMappedResourceWithException() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final MappedCloseableConsumerWithException mappedCloseableConsumerWithException = spy(
                new MappedCloseableConsumerWithException());
        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);
        map.put(2, inputStream2);

        TryAsyncBuilder.tryAsync(map, mappedCloseableConsumerWithException).execute();

        sleepUnchecked(1500);

        verify(mappedCloseableConsumerWithException).accept(map);
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void throwExceptionWhenClosingOneResource() throws IOException {
        final InputStream inputStream = mock(InputStream.class);

        doThrow(IOException.class).when(inputStream).close();

        TryAsyncBuilder.tryAsync(inputStream, e -> {}).execute();

        sleepUnchecked(1500);

        verify(inputStream, times(2)).close();
    }

    @Test
    public void throwExceptionWhenClosingOneResourceFromArray() throws IOException {
        final InputStream inputStream = mock(InputStream.class);

        doThrow(IOException.class).when(inputStream).close();

        TryAsyncBuilder.tryAsync(e -> {}, inputStream).execute();

        sleepUnchecked(1500);

        verify(inputStream, times(2)).close();
    }

    @Test
    public void throwExceptionWhenClosingOneResourceFromMap() throws IOException {
        final InputStream inputStream = mock(InputStream.class);

        doThrow(IOException.class).when(inputStream).close();

        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);

        TryAsyncBuilder.tryAsync(map, e -> {}).execute();

        sleepUnchecked(1500);

        verify(inputStream, times(2)).close();
    }

    private static class RunnableWithDelay implements Runnable {
        private final long delay;

        private RunnableWithDelay(final long delay) {
            this.delay = delay;
        }

        @Override
        public void run() {
            sleepUnchecked(delay);

            out.println("RunnableWithDeay class was executed successfully");
        }

    }

    private static class RunnableWithDelayAndException implements Runnable {
        private final long delay;

        private RunnableWithDelayAndException(final long delay) {
            this.delay = delay;
        }

        @Override
        public void run() {
            System.out.println("An exception will be thrown!");

            sleepUnchecked(delay);

            throw new RuntimeException("This is a expected esception.");
        }
    }

    private static class SimpleRunnable implements Runnable {
        @Override
        public void run() {
            out.println("SimpleRunnable class was executed successfully");
        }
    }

    private static class ExceptionConsumer implements Consumer<Throwable> {
        @Override
        public void accept(final Throwable throwable) {
            out.println(throwable.getClass().getName() + THIS_EXCEPTION_WAS_EXPECTED);
        }
    }

    private static class ExceptionConsumerThrowingException implements Consumer<Throwable> {
        @Override
        public void accept(final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static class CallableWithDeay implements Callable<String> {
        private static final String CALLABLE_RESULT = "CallableWithDeay class was executed successfully";
        private final long delay;

        private CallableWithDeay(final long delay) {
            this.delay = delay;
        }

        @Override
        public String call() {
            sleepUnchecked(delay);

            return CALLABLE_RESULT;
        }
    }

    private static class CallableResultConsumer implements Consumer<String> {
        private String result;

        private CallableResultConsumer(final String result) {
            this.result = result;
        }

        @Override
        public void accept(final String value) {
            System.out.println(value);

            result = value;
        }
    }

    private static class CallableWithDeayAndException implements Callable<String> {
        private final long delay;

        private CallableWithDeayAndException(final long delay) {
            this.delay = delay;
        }

        @Override
        public String call() {
            sleepUnchecked(delay);

            throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
        }
    }

    private static class CloseableConsumer implements Consumer<Closeable> {
        @Override
        public void accept(final Closeable closeable) {
            System.out.println("Closeable consumer fired successfully.");
        }
    }

    private static class CloseableArrayConsumer implements Consumer<Closeable[]> {
        @Override
        public void accept(final Closeable[] closeableArray) {
            System.out.println("Closeable array consumer fired successfully.");
        }
    }

    private static class MappedCloseableConsumer implements Consumer<Map<Object, Closeable>> {
        @Override
        public void accept(final Map<Object, Closeable> closeableMap) {
            System.out.println("Mapped closeable consumer fired successfully.");
        }
    }

    private static class CloseableConsumerWithException implements Consumer<Closeable> {
        @Override
        public void accept(final Closeable closeable) {
            throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
        }
    }

    private static class CloseableArrayConsumerWithException implements Consumer<Closeable[]> {
        @Override
        public void accept(final Closeable[] closeableArray) {
            throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
        }
    }

    private static class MappedCloseableConsumerWithException implements Consumer<Map<Object, Closeable>> {
        @Override
        public void accept(final Map<Object, Closeable> closeableMap) {
            throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
        }
    }
}
