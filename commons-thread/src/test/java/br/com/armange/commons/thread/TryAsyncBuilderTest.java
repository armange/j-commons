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
package br.com.armange.commons.thread;

import static br.com.armange.commons.thread.async.TryAsyncBuilder.tryAsync;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.junit.Test;

public class TryAsyncBuilderTest {
    private static final String THIS_EXCEPTION_WAS_EXPECTED = " -> This Exception was expected.";
    
    private static class RunnableWithDeay implements Runnable {
        private final long delay;
        
        private RunnableWithDeay(final long delay) {
            this.delay = delay;
        }

        @Override
        public void run() {
            sleepUnchecked(delay);
            
            out.println("RunnableWithDeay class was executed successfully");
        }
        
    }
    
    private static class RunnableWithDeayAndException implements Runnable {
        private final long delay;
        
        private RunnableWithDeayAndException(final long delay) {
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
    

    @Test
    public void tryRunnableWithoutExceptions() {
        final RunnableWithDeay runnableWithDeay = spy(new RunnableWithDeay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        
        tryAsync(runnableWithDeay).execute();
        
        simpleRunnable.run();
        
        verify(runnableWithDeay, times(0)).run();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(runnableWithDeay).run();
    }
    
    @Test
    public void tryRunnableWithExceptions() {
        final RunnableWithDeayAndException runnableWithDeayAndException = spy(new RunnableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());
        
        tryAsync(runnableWithDeayAndException)
            .addCatcher(RuntimeException.class, exceptionConsumer)
            .execute();
        
        simpleRunnable.run();
        
        verify(runnableWithDeayAndException, times(0)).run();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(runnableWithDeayAndException).run();
        verify(exceptionConsumer).accept(any());
    }
    
    @Test
    public void tryRunnableCatchExceptionAndFinalizer() {
        final RunnableWithDeayAndException runnableWithDeayAndException = spy(new RunnableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());
        
        tryAsync(runnableWithDeayAndException)
            .addCatcher(RuntimeException.class, exceptionConsumer)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(runnableWithDeayAndException, times(0)).run();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(runnableWithDeayAndException).run();
        verify(exceptionConsumer).accept(any());
        verify(simpleRunnable, times(2)).run();
    }
    
    @Test
    public void tryRunnableWithExceptionsAndFinalizerAndCatchThrowing() {
        final RunnableWithDeayAndException runnableWithDeayAndException = spy(new RunnableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer = spy(new ExceptionConsumerThrowingException());
        
        tryAsync(runnableWithDeayAndException)
            .addCatcher(RuntimeException.class, exceptionConsumer)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(runnableWithDeayAndException, times(0)).run();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(runnableWithDeayAndException).run();
        // Pass in exception consumer and thread uncaught exception method.
        // Because of this, the consumer must be verified two times.
        verify(exceptionConsumer, times(2)).accept(any());
        verify(simpleRunnable, times(2)).run();
    }
    
    @Test
    public void tryRunnableWithExceptionsAndFinalizer() {
        final RunnableWithDeayAndException runnableWithDeayAndException = spy(new RunnableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer = spy(new ExceptionConsumerThrowingException());
        
        tryAsync(runnableWithDeayAndException)
            .addCatcher(IOException.class, exceptionConsumer)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(runnableWithDeayAndException, times(0)).run();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(runnableWithDeayAndException).run();
        verify(simpleRunnable, times(2)).run();
    }
    
    @Test
    public void tryRunnableAndThrowExceptionWithoutCatch() {
        final RunnableWithDeayAndException runnableWithDeayAndException = spy(new RunnableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        
        tryAsync(runnableWithDeayAndException)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(runnableWithDeayAndException, times(0)).run();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(runnableWithDeayAndException).run();
        verify(simpleRunnable, times(2)).run();
    }
    
    /* * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * * *
     * * * * * Callable tests. * * * * *
     * * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * * *
     * */
    
    @Test
    public void tryCallableWithoutExceptions() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeay callableWithDeayAndException = spy(new CallableWithDeay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        
        
        tryAsync(callableWithDeayAndException, callableResultConsumer)
            .execute();
        
        simpleRunnable.run();
        
        verify(callableWithDeayAndException, times(0)).call();
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
            .addCatcher(RuntimeException.class, exceptionConsumer)
            .execute();
        
        simpleRunnable.run();
        
        verify(callableWithDeayAndException, times(0)).call();
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
            .addCatcher(RuntimeException.class, exceptionConsumer)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(callableWithDeayAndException, times(0)).call();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        // Pass in exception consumer and thread uncaught exception method.
        // Because of this, the consumer must be verified two times.
        verify(exceptionConsumer, times(2)).accept(any());
        verify(simpleRunnable, times(2)).run();
        assertNull(callableResultConsumer.result);
    }
    
    @Test
    public void tryCallableCatchExceptionAndFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDeayAndException callableWithDeayAndException = spy(new CallableWithDeayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());
        
        tryAsync(callableWithDeayAndException, callableResultConsumer)
            .addCatcher(RuntimeException.class, exceptionConsumer)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(callableWithDeayAndException, times(0)).call();
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
        
        tryAsync(callableWithDeayAndException, callableResultConsumer)
            .finallyAsync(simpleRunnable)
            .execute();
        
        simpleRunnable.run();
        
        verify(callableWithDeayAndException, times(0)).call();
        verify(simpleRunnable).run();
        
        sleepUnchecked(2000);
        
        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        verify(simpleRunnable, times(2)).run();
        assertNull(callableResultConsumer.result);
    }
}
