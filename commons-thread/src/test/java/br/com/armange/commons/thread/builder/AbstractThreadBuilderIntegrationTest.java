package br.com.armange.commons.thread.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import br.com.armange.commons.thread.util.ThreadUtil;

public class AbstractThreadBuilderIntegrationTest {
    private static final String EMPTY = "";

    @Test
    public void getThreadFactoryMethod() throws InterruptedException {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());
        final int threadPriority = 1;
        final Consumer<? super Throwable> uncaughtExceptionConsumer = spy(new EmptyUncaughtExceptionConsumer());
        final Supplier<String> threadNameSupplier = () -> EMPTY;
        final IntSupplier threadPrioritySupplier = () -> threadPriority;

        final ThreadFactory threadFactory = builder.setUncaughtExceptionConsumer(uncaughtExceptionConsumer)
                .setThreadNameSupplier(threadNameSupplier).setThreadPrioritySupplier(threadPrioritySupplier)
                .getThreadFactory();

        assertNotNull(threadFactory);

        final Thread newThread = threadFactory.newThread(() -> {
            throw new RuntimeException();
        });

        newThread.start();

        Thread.sleep(500);

        assertEquals(EMPTY, newThread.getName());
        assertEquals(threadPriority, newThread.getPriority());
        verify(uncaughtExceptionConsumer).accept(any());
        assertNotNull(builder.uncaughtExceptionConsumer);
        assertEquals(uncaughtExceptionConsumer, builder.uncaughtExceptionConsumer.get());
        assertNotNull(builder.threadNameSupplier);
        assertEquals(threadNameSupplier, builder.threadNameSupplier.get());
        assertNotNull(builder.threadPrioritySupplier);
        assertEquals(threadPrioritySupplier, builder.threadPrioritySupplier.get());
        verify(builder, times(3)).getSelf();
    }

    @Test
    public void runThreadMethod() {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        assertNotNull(builder.executorResult);
        assertNotNull(builder.executor);
        assertThat(builder.executor.getAfterExecuteConsumers(), hasSize(1));
        assertThat(builder.executorResult.getFutures(), hasSize(1));
        verify(builder).handleException(any());
        verify(builder).submit();
        verify(builder).getSelf();
        verify(builder).getThreadFactory();
    }

    @Test
    public void submitRunnableMethod() {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        final Future<?> submit = builder.submit();

        assertNotNull(submit);
    }

    @Test
    public void submitCallableMethod() {
        final CallableThreadBuilderTestArtifact<?> builder = spy(CallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null).createExecutorAndRunThread();

        final Future<?> submit = builder.submit();

        assertNotNull(submit);
    }

    @Test
    public void scheduleRunnableMethod() {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        final Future<?> schedule = builder.schedule(0, TimeUnit.MILLISECONDS);

        assertNotNull(schedule);
    }

    @Test
    public void scheduleCallableMethod() {
        final CallableThreadBuilderTestArtifact<?> builder = spy(CallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null).createExecutorAndRunThread();

        final Future<?> schedule = builder.schedule(0, TimeUnit.MILLISECONDS);

        assertNotNull(schedule);
    }

    @Test
    public void scheduleAtFixedRateRunnableMethod() {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        final Future<?> scheduleAtFixedRate = builder.scheduleAtFixedRate(0, 1, TimeUnit.MILLISECONDS);

        assertNotNull(scheduleAtFixedRate);
    }

    @Test
    public void handleExceptionMethodAndConsumesExceptionPrintingStacktrace() throws ExecutionException, InterruptedException {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());
        final PrintStream spiedPrintStream = spy(System.err);

        System.setErr(spiedPrintStream);
        builder.uncaughtExceptionConsumer = spy(Optional.empty());
        builder.setSilentInterruption(false).setExecution(() -> ThreadUtil.sleepUnchecked(1000)).start();
        builder.executorResult.getFutures().get(0).cancel(true);
        ThreadUtil.sleepUnchecked(1000);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(spiedPrintStream, Mockito.atLeast(1)).println(captor.capture());

        final List<String> stringList = captor.getAllValues().stream().map(String::valueOf).collect(Collectors.toList());

        assertThat(stringList, hasItem(containsString(CancellationException.class.getName())));

        final Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = builder.uncaughtExceptionConsumer;

        assertFalse(uncaughtExceptionConsumer.isPresent());
        verify(uncaughtExceptionConsumer).orElseGet(any());
    }

    @Test
    public void handleExceptionMethodAndDoNotConsumesException() throws ExecutionException, InterruptedException {
        final RunnableThreadBuilderTestArtifact<?> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());
        final PrintStream spiedPrintStream = spy(System.err);

        System.setErr(spiedPrintStream);
        builder.uncaughtExceptionConsumer = spy(Optional.empty());
        builder.setSilentInterruption(true).setExecution(() -> ThreadUtil.sleepUnchecked(1000)).start();
        builder.executorResult.getFutures().get(0).cancel(true);
        ThreadUtil.sleepUnchecked(1000);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(spiedPrintStream, Mockito.atLeast(1)).println(captor.capture());

        final List<String> stringList = captor.getAllValues().stream().map(String::valueOf).collect(Collectors.toList());

        assertThat(stringList, hasItem(containsString(CancellationException.class.getName())));

        final Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = builder.uncaughtExceptionConsumer;

        assertFalse(uncaughtExceptionConsumer.isPresent());
        verify(uncaughtExceptionConsumer, never()).orElseGet(any());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void consumeFutureMethodWithRunnable() throws ExecutionException, InterruptedException {
        final Future<Void> future = mock(Future.class);

        final RunnableThreadBuilderTestArtifact<Void> builder = spy(RunnableThreadBuilderTestArtifact.newBuilder());

        builder.consumesFuture(future);

        verify(future).get();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consumeFutureMethodWithCallableWithoutResultConsumer() throws ExecutionException, InterruptedException {
        final String futureResult = "futureResult";
        final Future<String> future = mock(Future.class);
        final CallableThreadBuilderTestArtifact<String> builder = CallableThreadBuilderTestArtifact.newBuilder();

        doReturn(futureResult).when(future).get();
        builder.executorResult = mock(ExecutorResult.class);
        builder.threadResultConsumer = spy(builder.threadResultConsumer);

        builder.consumesFuture(future);

        verify(future).get();
        verify(builder.executorResult).setThreadResult(futureResult);
        verify(builder.threadResultConsumer).ifPresent(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consumeFutureMethodWithCallableWithResultConsumer() throws ExecutionException, InterruptedException {
        final PrintStream spiedOutPrintStream = spy(System.out);

        System.setOut(spiedOutPrintStream);

        final String futureResult = "futureResult";
        final Future<String> future = mock(Future.class);
        final ThreadResultConsumer threadResultConsumer = spy(new ThreadResultConsumer());
        final CallableThreadBuilderTestArtifact<String> builder = CallableThreadBuilderTestArtifact
                .<String>newBuilder()
                .setThreadResultConsumer(threadResultConsumer);

        doReturn(futureResult).when(future).get();
        builder.executorResult = mock(ExecutorResult.class);
        doReturn(futureResult).when(builder.executorResult).getThreadResult();
        builder.threadResultConsumer = spy(builder.threadResultConsumer);

        builder.consumesFuture(future);

        verify(future).get();
        verify(builder.executorResult).setThreadResult(futureResult);
        verify(builder.threadResultConsumer).ifPresent(any());
        verify(builder.executorResult).getThreadResult();
        verify(threadResultConsumer).accept(futureResult);

        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        verify(spiedOutPrintStream).println(stringCaptor.capture());
        assertEquals(futureResult, stringCaptor.getValue());
    }
    
    @Test
    public void theExecutorResultIsNotReplaced() {
        final RunnableThreadBuilderTestArtifact<Void> builder = RunnableThreadBuilderTestArtifact.newBuilder();
        final ExecutorResult<Void> executorResult = new ExecutorResult<>(builder.executor);
        
        builder.executorResult = executorResult;
        builder.setExecution(() -> {}).start();
        
        assertThat(executorResult, is(builder.executorResult));
    }

    private static class EmptyUncaughtExceptionConsumer implements Consumer<Throwable> {
        @Override
        public void accept(final Throwable t) {
            t.printStackTrace();
        }
    }

    private static class ThreadResultConsumer implements Consumer<String> {
        @Override
        public void accept(final String value) {
            System.out.println(value);
        }
    }
}
