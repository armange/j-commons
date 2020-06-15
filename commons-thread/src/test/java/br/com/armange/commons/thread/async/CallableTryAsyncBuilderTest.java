package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.*;
import org.junit.Test;

import static br.com.armange.commons.thread.Constants.JDK_BEHAVIOR;
import static br.com.armange.commons.thread.async.TryAsyncBuilder.tryAsync;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CallableTryAsyncBuilderTest {

    @Test
    public void shouldTryCallableWithoutExceptionsWithoutFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDelay callableWithDelayAndException = spy(new CallableWithDelay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(callableWithDelayAndException, callableResultConsumer).execute();
        simpleRunnable.run();
        verify(simpleRunnable).run();
        sleepUnchecked(1000);
        verify(callableWithDelayAndException).call();
        assertNotNull(callableResultConsumer.result);
    }

    @Test
    public void shouldTryCallableWithExceptionAndCatchingItWithoutFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDelayAndException callableWithDelayAndException = spy(new CallableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(callableWithDelayAndException, callableResultConsumer)
                .addCatcher(RuntimeException.class, exceptionConsumer)
                .execute();
        simpleRunnable.run();
        verify(simpleRunnable).run();
        sleepUnchecked(1000);
        verify(callableWithDelayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        verify(exceptionConsumer).accept(any());
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void shouldTryCallableWithExceptionAndCatchingItAndFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDelayAndException callableWithDeayAndException = spy(new CallableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(callableWithDeayAndException, callableResultConsumer)
                .addCatcher(RuntimeException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable)
                .execute();
        simpleRunnable.run();
        verify(simpleRunnable).run();
        sleepUnchecked(1000);
        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        verify(exceptionConsumer).accept(any());
        verify(simpleRunnable, times(2)).run();
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void shouldTryCallableWithExceptionsAndCatchingItAndRethrowingAndFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDelayAndException callableWithDeayAndException = spy(new CallableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer = spy(new ExceptionConsumerThrowingException());

        tryAsync(callableWithDeayAndException, callableResultConsumer)
                .addCatcher(RuntimeException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable)
                .execute();
        simpleRunnable.run();
        verify(simpleRunnable).run();
        sleepUnchecked(1000);
        verify(callableWithDeayAndException).call();
        verify(callableResultConsumer, times(0)).accept(any());
        // It will pass in exception consumer and thread uncaught exception method.
        // br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor.afterExecute (2 times in debug mode)
        // java.lang.Thread.dispatchUncaughtException (2 times in debug mode)
        // Because of this, the consumer must be verified two times.
        verify(exceptionConsumer, times(JDK_BEHAVIOR)).accept(any());
        verify(simpleRunnable, times(JDK_BEHAVIOR + 1)).run();
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void shouldTryCallableWithExceptionWithoutCatchingItAndFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDelayAndException callableWithDelayAndException = spy(new CallableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(callableWithDelayAndException, callableResultConsumer)
                .finallyAsync(simpleRunnable)
                .execute();
        verify(simpleRunnable, never()).run();
        sleepUnchecked(1000);
        verify(callableWithDelayAndException).call();
        verify(callableResultConsumer, never()).accept(any());
        verify(simpleRunnable).run();
        assertNull(callableResultConsumer.result);
    }

    @Test
    public void shouldTryCallableWithoutExceptionWithFinalizer() {
        final CallableResultConsumer callableResultConsumer = spy(new CallableResultConsumer(null));
        final CallableWithDelay callableWithDelay = spy(new CallableWithDelay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(callableWithDelay, callableResultConsumer).
                finallyAsync(simpleRunnable)
                .execute();
        verify(simpleRunnable, never()).run();
        sleepUnchecked(1000);
        verify(callableWithDelay).call();
        verify(simpleRunnable).run();
    }
}
