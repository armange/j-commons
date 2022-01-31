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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TimingCallableThreadBuilderTest {

    @Test
    public void singleCallableThread() {
        final LocalCallableString localCallableString = new LocalCallableString();
        final ThreadResultStringConsumer threadResultStringConsumer = spy(new ThreadResultStringConsumer());

        // @formatter:off
        TimingCallableThreadBuilder
                .<String>newBuilder(1)
                .setScheduling(localCallableString)
                .setThreadResultConsumer(threadResultStringConsumer)
                .start();
        // @formatter:on

        ThreadUtil.sleepUnchecked(1500);

        verify(threadResultStringConsumer).accept(LocalCallableString.THIS_IS_A_CALLABLE_THREAD);
    }

    @Test
    public void scheduleCallableMethod() {
        final TimingCallableThreadBuilderTestArtifact<?> builder = spy(TimingCallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null).createExecutorAndRunThread();

        final Future<?> schedule = builder.schedule(0, TimeUnit.MILLISECONDS);

        assertNotNull(schedule);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void scheduleAtFixedRateCallableMethod() {
        final TimingCallableThreadBuilderTestArtifact<?> builder = spy(TimingCallableThreadBuilderTestArtifact.newBuilder());

        builder.setExecution(() -> null).createExecutorAndRunThread();

        builder.scheduleAtFixedRate(0, 1, TimeUnit.MILLISECONDS);
    }

    private static class LocalCallableString implements Callable<String> {
        private static final String THIS_IS_A_CALLABLE_THREAD = "This is a callable thread.";

        @Override
        public String call() {
            ThreadUtil.sleepUnchecked(1000);

            return THIS_IS_A_CALLABLE_THREAD;
        }
    }

    private static class ThreadResultStringConsumer implements Consumer<String> {
        @Override
        public void accept(final String value) {
            System.out.println(value);
        }

    }
}
