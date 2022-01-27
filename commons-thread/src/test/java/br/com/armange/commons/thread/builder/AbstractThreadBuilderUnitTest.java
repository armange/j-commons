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

import br.com.armange.commons.thread.builder.AbstractThreadBuilder.ExecutionType;
import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AbstractThreadBuilderUnitTest {
    private static final String EMPTY = "";

    @Test
    public void AbstractThreadBuilderConstructor() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        assertNotNull(builder.executionType);
        assertEquals(ExecutionType.RUNNABLE, builder.executionType);
    }

    @Test
    public void setAfterExecuteConsumerMethod() {
        final BiConsumer<Runnable, Throwable> afterExecuteConsumer = (a, b) -> {
        };
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setAfterExecuteConsumer(afterExecuteConsumer);

        assertNotNull(builder.afterExecuteConsumer);
        assertTrue(builder.afterExecuteConsumer.isPresent());
        assertNotNull(builder.afterExecuteConsumer.get());
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setUncaughtExceptionConsumerMethod() {
        final Consumer<? super Throwable> uncaughtExceptionConsumer = a -> {
        };
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setUncaughtExceptionConsumer(uncaughtExceptionConsumer);

        assertNotNull(builder.uncaughtExceptionConsumer);
        assertTrue(builder.uncaughtExceptionConsumer.isPresent());
        assertNotNull(builder.uncaughtExceptionConsumer.get());
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setThreadNameSupplierMethod() {
        final Supplier<String> threadNameSupplier = () -> EMPTY;
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setThreadNameSupplier(threadNameSupplier);

        assertNotNull(builder.threadNameSupplier);
        assertTrue(builder.threadNameSupplier.isPresent());
        assertNotNull(builder.threadNameSupplier.get());
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setThreadPrioritySupplierMethod() {
        final int threadPriority = 1;
        final IntSupplier threadPrioritySupplier = () -> threadPriority;
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setThreadPrioritySupplier(threadPrioritySupplier);

        assertNotNull(builder.threadPrioritySupplier);
        assertTrue(builder.threadPrioritySupplier.isPresent());
        assertNotNull(builder.threadPrioritySupplier.get());
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setExecutionMethod() {
        final Runnable execution = () -> {
        };
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setExecution(execution);

        assertNotNull(builder.execution);
        verify(builder).requireExecutionNonNull();
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setMayInterruptIfRunningMethod() {
        final boolean flag = false;
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setThreadNameSupplier(() -> "t1");

        assertTrue(builder.threadNameSupplier.isPresent());
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setSilentInterruptionMethod() {
        final boolean flag = false;
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.setSilentInterruption(flag);

        assertFalse(builder.silentInterruption);
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void setThreadResultConsumerMethod() {
        final Consumer<String> threadResultConsumer = a -> {
        };
        final SimpleCallableThreadBuilderTestArtifact<String> builder = spy(SimpleCallableThreadBuilderTestArtifact.newBuilder());
        final SimpleCallableThreadBuilderTestArtifact<String> result = builder.setThreadResultConsumer(threadResultConsumer);

        assertNotNull(builder.threadResultConsumer);
        assertTrue(builder.threadResultConsumer.isPresent());
        assertNotNull(builder.threadResultConsumer.get());
        assertEquals(builder, result);
        verify(builder).getSelf();
    }

    @Test
    public void getSelfMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());
        final SimpleRunnableThreadBuilderTestArtifact result = builder.getSelf();

        assertEquals(builder, result);
    }

    @Test
    public void startMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        final ExecutorResult<?> executorResult = builder.setExecution(() -> {
        }).start();

        assertEquals(builder.executorResult, executorResult);
        verify(builder).createExecutorAndRunThread();
        verify(builder, times(1)).getSelf();
    }

    @Test
    public void startAndBuildOtherMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        final SimpleRunnableThreadBuilderTestArtifact result = builder.setExecution(() -> {
        }).startAndBuildOther();

        assertEquals(builder, result);
        verify(builder).createExecutorAndRunThread();
        verify(builder, times(2)).getSelf();
    }

    @Test
    public void createExecutorAndRunThreadMethod() {
        final SimpleRunnableThreadBuilderTestArtifact builder = spy(SimpleRunnableThreadBuilderTestArtifact.newBuilder());

        builder.setAfterExecuteConsumer((a, b) -> {
        }).setExecution(() -> {
        }).createExecutorAndRunThread();

        assertNotNull(builder.executor);
        assertThat(builder.executor, instanceOf(ScheduledThreadBuilderExecutor.class));
        assertThat(builder.executor.getAfterExecuteConsumers(), hasSize(2));
        verify(builder, times(2)).requireExecutionNonNull();
        verify(builder).runThread();
        verify(builder, times(2)).getSelf();
        verify(builder).getThreadFactory();
    }

}
