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
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

public class TimingCallableThreadBuilderTest {

    @Test
    public void singleCallableThread() throws Exception {
        final LocalCallableString localCallableString = new LocalCallableString();

        final ExecutorResult<String> result = TimingCallableBuilder
                .<String>newBuilder(1)
                .setScheduling(localCallableString)
                .start();

        assertEquals(LocalCallableString.THIS_IS_A_CALLABLE_THREAD,
                result.getFuture(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    public void scheduleCallableMethod() {
        final TimingCallableThreadBuilderTestArtifact<?> builder = spy(TimingCallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null);
        final TaskConfiguration<Callable> task = builder.taskConfiguration;
        builder.createExecutorAndRunThread();

        final Future<?> schedule = builder.schedule(task, 0, TimeUnit.MILLISECONDS);

        assertNotNull(schedule);
        builder.executor.shutdown();
    }

    @Test
    public void scheduleAtFixedRateCallableMethod() {
        final TimingCallableThreadBuilderTestArtifact<?> builder = spy(TimingCallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null);
        final TaskConfiguration<Callable> task = builder.taskConfiguration;
        builder.createExecutorAndRunThread();

        assertThrows(UnsupportedOperationException.class,
                () -> builder.scheduleAtFixedRate(task, 0, 1, TimeUnit.MILLISECONDS));
        builder.executor.shutdown();
    }

    @Test
    public void setIntervalRejectsCallableBeforeCreatingExecutorAndKeepsTaskUsable() throws Exception {
        final TimingCallableBuilder<Integer> builder = ThreadBuilder
                .newScheduling((Callable<Integer>) () -> 42);
        final TaskConfiguration<Callable<Integer>> configuredTask = builder.taskConfiguration;

        final UnsupportedOperationException failure = assertThrows(
                UnsupportedOperationException.class, () -> builder.setInterval(100));

        assertEquals("Periodic Callable scheduling is not supported", failure.getMessage());
        assertSame(configuredTask, builder.taskConfiguration);
        assertNull(builder.executor);

        final ExecutorResult<Integer> result = builder
                .setDelay(10)
                .start();

        assertEquals(42, result.getFutureResult(0));
        assertTrue(result.getExecutorService().isShutdown());
    }

    @Test
    public void openCallableStageRejectsIntervalBeforeCreatingExecutor() throws Exception {
        final OpenTimingCallableBuilder<Integer> builder = ThreadBuilder
                .newScheduling((Callable<Integer>) () -> 42)
                .keepExecutorOpen();
        final TaskConfiguration<Callable<Integer>> configuredTask = builder.taskConfiguration;

        assertThrows(UnsupportedOperationException.class, () -> builder.setInterval(100));
        assertSame(configuredTask, builder.taskConfiguration);
        assertNull(builder.executor);

        final OpenExecutorResult<Integer> result = builder.start();

        try (var executor = result.getExecutorService()) {
            assertEquals(42, result.getFutureResult(0));
        }
    }

    private static class LocalCallableString implements Callable<String> {
        private static final String THIS_IS_A_CALLABLE_THREAD = "This is a callable thread.";

        @Override
        public String call() {
            ThreadUtil.sleepUnchecked(1000);

            return THIS_IS_A_CALLABLE_THREAD;
        }
    }

}
