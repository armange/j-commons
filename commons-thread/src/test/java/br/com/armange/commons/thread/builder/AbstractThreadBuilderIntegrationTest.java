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
package br.com.armange.commons.thread.builder;

import br.com.armange.commons.thread.util.ThreadUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AbstractThreadBuilderIntegrationTest {
    private static final String EMPTY = "";

    @Test
    public void getThreadFactoryMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
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

        ThreadUtil.sleepUnchecked(500);

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
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

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
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        final Future<?> submit = builder.submit();

        assertNotNull(submit);
    }

    @Test
    public void submitCallableMethod() {
        final TimingCallableThreadBuilderTestArtifact<?> builder = spy(TimingCallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null).createExecutorAndRunThread();

        final Future<?> submit = builder.submit();

        assertNotNull(submit);
    }

    @Test
    public void handleExceptionMethodAndConsumesExceptionPrintingStacktrace() throws ExecutionException, InterruptedException {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        // PrintStream spiedPrintStream = spy(System.err);

        //System.setErr(spiedPrintStream);
        builder.uncaughtExceptionConsumer = spy(Optional.empty());
        builder.setSilentInterruption(false).setExecution(() -> ThreadUtil.sleepUnchecked(1000)).start();
        builder.executorResult.getFutures().get(0).cancel(true);
        ThreadUtil.sleepUnchecked(1000);

        //final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        //verify(spiedPrintStream, Mockito.atLeast(1)).println(captor.capture());

        //final List<String> stringList = captor.getAllValues().stream().map(String::valueOf).collect(Collectors.toList());

        //assertThat(stringList, hasItem(containsString(CancellationException.class.getName())));

        final Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = builder.uncaughtExceptionConsumer;

        assertFalse(uncaughtExceptionConsumer.isPresent());
        verify(uncaughtExceptionConsumer).orElseGet(any());
    }

    @Test
    public void handleExceptionMethodAndDoNotConsumesException() throws ExecutionException, InterruptedException {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        //final PrintStream spiedPrintStream = spy(System.err);

        //System.setErr(spiedPrintStream);
        builder.uncaughtExceptionConsumer = spy(Optional.empty());
        builder.setSilentInterruption(true).setExecution(() -> ThreadUtil.sleepUnchecked(1000)).start();
        builder.executorResult.getFutures().get(0).cancel(true);
        ThreadUtil.sleepUnchecked(1000);

        //final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        //verify(spiedPrintStream, Mockito.atLeast(1)).println(captor.capture());

        //final List<String> stringList = captor.getAllValues().stream().map(String::valueOf).collect(Collectors.toList());

        //assertThat(stringList, hasItem(containsString(CancellationException.class.getName())));

        final Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = builder.uncaughtExceptionConsumer;

        assertFalse(uncaughtExceptionConsumer.isPresent());
        verify(uncaughtExceptionConsumer, never()).orElseGet(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consumeFutureMethodWithRunnable() throws ExecutionException, InterruptedException {
        final Future<Void> future = mock(Future.class);

        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        builder.consumesFuture(future);

        verify(future).get();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consumeFutureMethodWithCallableWithoutResultConsumer() throws ExecutionException, InterruptedException {
        final String futureResult = "futureResult";
        final Future<String> future = mock(Future.class);
        final TimingCallableThreadBuilderTestArtifact<String> builder = TimingCallableThreadBuilderTestArtifact.newBuilder();

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
        final TimingCallableThreadBuilderTestArtifact<String> builder = TimingCallableThreadBuilderTestArtifact
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
        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        final ExecutorResult<Void> executorResult = new ExecutorResult<>(builder.executor);

        builder.executorResult = executorResult;
        builder.setExecution(() -> {
        }).start();

        assertThat(executorResult, is(builder.executorResult));
    }

    @Test
    public void theThreadRunsBeforeRegisteringAfterExecutionConsumer() {
        final EmptyUncaughtExceptionConsumer exceptionConsumer = spy(new EmptyUncaughtExceptionConsumer());
        final RuntimeException runtimeException = new RuntimeException();

        SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact
                .newBuilder());

        doAnswer(invocation -> {
            invocation.callRealMethod();

            ThreadUtil.sleepUnchecked(1000);

            return null;
        }).when(builder).submit();

        builder
                .setExecution(() -> {
                    throw runtimeException;
                }).setUncaughtExceptionConsumer(exceptionConsumer)
                .start();

        verify(exceptionConsumer).accept(any());
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
