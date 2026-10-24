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

import br.com.armange.commons.thread.core.ExceptionCatcherThreadFactory;
import br.com.armange.commons.thread.util.ThreadUtil;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
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
    public void preservesSuppliedFactoryAndAppliesBuilderThreadName() {
        final ExceptionCatcherThreadFactory suppliedFactory = new ExceptionCatcherThreadFactory();
        suppliedFactory.setThreadName("original");

        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        builder.threadFactory = Optional.of(suppliedFactory);

        final ThreadFactory configuredFactory = builder
                .setThreadNameSupplier(() -> "configured")
                .getThreadFactory();
        final Thread thread = configuredFactory.newThread(() -> {
        });

        assertSame(suppliedFactory, configuredFactory);
        assertEquals("configured", thread.getName());
    }

    @Test
    public void rejectsInvalidDefaultPlatformPriorityWhenCreatingFactory() {
        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        builder.setThreadPrioritySupplier(() -> Thread.MAX_PRIORITY + 1);

        assertThrows(IllegalArgumentException.class, builder::getThreadFactory);
    }

    @Test
    public void consultsNameSupplierForEachPlatformThread() {
        final AtomicInteger nameRequests = new AtomicInteger();
        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        final ThreadFactory factory = builder
                .setThreadNameSupplier(() -> "worker-" + nameRequests.incrementAndGet())
                .getThreadFactory();

        assertEquals(0, nameRequests.get());

        final Thread firstThread = factory.newThread(() -> {
        });
        final Thread secondThread = factory.newThread(() -> {
        });

        assertEquals("worker-1", firstThread.getName());
        assertEquals("worker-2", secondThread.getName());
        assertEquals(2, nameRequests.get());
    }

    @Test
    public void configuresSuppliedFactoryNameOnceUsingOverriddenSetter() {
        final AtomicInteger nameRequests = new AtomicInteger();
        final AtomicInteger setterCalls = new AtomicInteger();
        final ExceptionCatcherThreadFactory suppliedFactory = new ExceptionCatcherThreadFactory() {
            @Override
            public void setThreadName(final String threadName) {
                setterCalls.incrementAndGet();
                super.setThreadName("custom-" + threadName);
            }
        };
        suppliedFactory.setThreadName("original");

        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        builder.threadFactory = Optional.of(suppliedFactory);

        final ThreadFactory factory = builder
                .setThreadNameSupplier(() -> "worker-" + nameRequests.incrementAndGet())
                .getThreadFactory();

        assertSame(suppliedFactory, factory);
        assertEquals(1, nameRequests.get());
        assertEquals(2, setterCalls.get());

        final Thread firstThread = factory.newThread(() -> {
        });
        final Thread secondThread = factory.newThread(() -> {
        });

        assertEquals("custom-worker-1", firstThread.getName());
        assertEquals("custom-worker-1", secondThread.getName());
        assertEquals(1, nameRequests.get());

        suppliedFactory.setThreadName("fixed");

        final Thread fixedNameThread = factory.newThread(() -> {
        });

        assertEquals("custom-fixed", fixedNameThread.getName());
        assertEquals(1, nameRequests.get());
        assertEquals(3, setterCalls.get());
    }

    @Test
    public void keepsGeneratedThreadNameWhenSupplierReturnsNull() {
        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        final ThreadFactory factory = builder
                .setThreadNameSupplier(() -> null)
                .getThreadFactory();
        final Thread thread = factory.newThread(() -> {
        });

        assertFalse(thread.getName().isEmpty());
    }

    @Test
    public void runThreadMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        }).createExecutorAndRunThread();

        assertNotNull(builder.batch);
        assertNotNull(builder.executor);
        assertThat(builder.batch.futures(), hasSize(1));
        verify(builder).submit(any());
        verify(builder).getSelf();
        verify(builder).getThreadFactory();
    }

    @Test
    public void submitRunnableMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> {
        });
        final TaskConfiguration<Runnable> task = builder.taskConfiguration;
        builder.createExecutorAndRunThread();

        final Future<?> submit = builder.submit(task);

        assertNotNull(submit);
    }

    @Test
    public void submitCallableMethod() {
        final TimingCallableThreadBuilderTestArtifact<?> builder = spy(TimingCallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null);
        final TaskConfiguration<Callable> task = builder.taskConfiguration;
        builder.createExecutorAndRunThread();

        final Future<?> submit = builder.submit(task);

        assertNotNull(submit);
    }

    @Test
    public void finalStartRetainsItsResult() {
        final SimpleRunnableThreadBuilderTestArtifact builder = SimpleRunnableThreadBuilderTestArtifact.newBuilder();
        final ExecutorResult<Void> executorResult = builder.setExecution(() -> {
        }).start();

        assertThat(executorResult, is(builder.executorResult));
    }

    @Test
    public void submittedTaskFailureRemainsInFuture() {
        final EmptyUncaughtExceptionConsumer exceptionConsumer = spy(new EmptyUncaughtExceptionConsumer());
        final RuntimeException runtimeException = new RuntimeException();
        final ExecutorResult<Void> result = SimpleRunnableThreadBuilderTestArtifact
                .newBuilder()
                .setExecution(() -> {
                    throw runtimeException;
                }).setUncaughtExceptionConsumer(exceptionConsumer)
                .start();

        final ExecutionException failure = assertThrows(ExecutionException.class,
                () -> result.getFuture(0).get(5, TimeUnit.SECONDS));
        assertSame(runtimeException, failure.getCause());
        verifyNoInteractions(exceptionConsumer);
    }

    private static class EmptyUncaughtExceptionConsumer implements Consumer<Throwable> {
        @Override
        public void accept(final Throwable t) {
            t.printStackTrace();
        }
    }

}
