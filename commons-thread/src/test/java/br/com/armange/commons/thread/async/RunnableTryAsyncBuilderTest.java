package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.*;
import org.junit.Test;

import java.io.IOException;

import static br.com.armange.commons.thread.Constants.JDK_BEHAVIOR;
import static br.com.armange.commons.thread.async.TryAsyncBuilder.tryAsync;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RunnableTryAsyncBuilderTest {

    @Test
    public void shouldTryRunnableWithoutExceptionsWithoutFinalizer() {
        final RunnableWithDelay runnableWithDelay = spy(new RunnableWithDelay(500));

        tryAsync(runnableWithDelay).execute();
        sleepUnchecked(1000);
        verify(runnableWithDelay).run();
    }

    @Test
    public void shouldTryRunnableWithExceptionAndCatchingItWithoutFinalizer() {
        final RunnableWithDelayAndException runnableWithDelayAndException =
                spy(new RunnableWithDelayAndException(500));
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(runnableWithDelayAndException)
                .addCatcher(RuntimeException.class, exceptionConsumer)
                .execute();
        sleepUnchecked(1000);
        verify(runnableWithDelayAndException).run();
        verify(exceptionConsumer).accept(any());
    }

    @Test
    public void shouldTryRunnableWithExceptionAndCatchingItAndFinalizer() {
        final RunnableWithDelayAndException runnableWithDelayAndException =
                spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumer exceptionConsumer = spy(new ExceptionConsumer());

        tryAsync(runnableWithDelayAndException)
                .addCatcher(RuntimeException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable)
                .execute();
        verify(simpleRunnable, never()).run();
        sleepUnchecked(1000);
        verify(runnableWithDelayAndException).run();
        verify(exceptionConsumer).accept(any());
        verify(simpleRunnable).run();
    }

    @Test
    public void shouldTryRunnableWithExceptionAndCatchingItAndRethrowingAndFinalizer() {
        final RunnableWithDelayAndException runnableWithDelayAndException =
                spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer =
                spy(new ExceptionConsumerThrowingException());

        tryAsync(runnableWithDelayAndException)
                .addCatcher(RuntimeException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable)
                .execute();
        verify(simpleRunnable, never()).run();
        sleepUnchecked(2000);
        verify(runnableWithDelayAndException).run();
        // It will pass in exception consumer and thread uncaught exception method.
        // br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor.afterExecute (2 times)
        // java.lang.Thread.dispatchUncaughtException (2 times in debug mode)
        // Because of this, the consumer must be verified two times.
        verify(exceptionConsumer, times(JDK_BEHAVIOR)).accept(any());
        verify(simpleRunnable, times(JDK_BEHAVIOR)).run();
    }

    @Test
    public void shouldTryRunnableWithExceptionWithoutCatchingItAndFinalizer() {
        final RunnableWithDelayAndException runnableWithDelayAndException =
                spy(new RunnableWithDelayAndException(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());
        final ExceptionConsumerThrowingException exceptionConsumer =
                spy(new ExceptionConsumerThrowingException());

        tryAsync(runnableWithDelayAndException)
                .addCatcher(IOException.class, exceptionConsumer)
                .finallyAsync(simpleRunnable)
                .execute();
        verify(simpleRunnable, never()).run();
        sleepUnchecked(1000);
        verify(runnableWithDelayAndException).run();
        verify(simpleRunnable).run();
    }

    @Test
    public void shouldTryRunnableWithoutExceptionWithFinalizer() {
        final RunnableWithDelay runnableWithDelay = spy(new RunnableWithDelay(500));
        final SimpleRunnable simpleRunnable = spy(new SimpleRunnable());

        tryAsync(runnableWithDelay).
                finallyAsync(simpleRunnable)
                .execute();
        verify(simpleRunnable, never()).run();
        sleepUnchecked(1000);
        verify(runnableWithDelay).run();
        verify(simpleRunnable).run();
    }
}
