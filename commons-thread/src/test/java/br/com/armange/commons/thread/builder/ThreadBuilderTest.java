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

import br.com.armange.commons.thread.core.ScheduledThreadBuilderExecutor;
import br.com.armange.commons.thread.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.lang.System.out;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Diego Armange Costa
 * @since 2019-11-18 V1.0.0 (JDK 1.8)
 */
public class ThreadBuilderTest {

    private static final String ACTIVE_THREADS = "Active threads: %d";
    private static final String ACTIVE_THREADS_BEFORE_THREADS = "Active threads before test: %d";

    @Test
    public void threadBuilderWith15CorePoolSize() {
        final int activeCountBeforeTest = Thread.activeCount();
        final int corePoolSize = 15;

        out.println(String.format(ACTIVE_THREADS_BEFORE_THREADS, activeCountBeforeTest));

        final ExecutorResult<?> result = ThreadBuilder
                .newBuilder(corePoolSize)
                .setScheduling(() -> ThreadUtil.sleepUnchecked(10000))
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .startAndBuildOther()
                .start();

        assertThat(result.getExecutorService(), instanceOf(ScheduledThreadBuilderExecutor.class));

        final ScheduledThreadBuilderExecutor executor = (ScheduledThreadBuilderExecutor) result
                .getExecutorService();

        assertEquals(corePoolSize, executor.getActiveCount());

        ThreadUtil.sleepUnchecked(1500);

        final int activeCountAfterTest = Thread.activeCount();
        final int newThreadsCount = activeCountAfterTest - activeCountBeforeTest;

        out.println(String.format(ACTIVE_THREADS, newThreadsCount));

        assertThat(newThreadsCount, greaterThanOrEqualTo(5));
    }

    @Test
    public void threadBuilderWithOneCorePoolSize() {
        final int activeCountBeforeTest = Thread.activeCount();
        final int corePoolSize = 1;

        out.println(String.format(ACTIVE_THREADS_BEFORE_THREADS, activeCountBeforeTest));

        final ExecutorResult<?> result = ThreadBuilder
                .newBuilder(corePoolSize)
                .setScheduling(() -> ThreadUtil.sleepUnchecked(10000))
                .start();

        assertThat(result.getExecutorService(), instanceOf(ScheduledThreadBuilderExecutor.class));

        final ScheduledThreadBuilderExecutor executor = (ScheduledThreadBuilderExecutor) result
                .getExecutorService();

        assertEquals(corePoolSize, executor.getActiveCount());

        ThreadUtil.sleepUnchecked(1500);

        final int activeCountAfterTest = Thread.activeCount();
        final int newThreadsCount = activeCountAfterTest - activeCountBeforeTest;

        out.println(String.format(ACTIVE_THREADS, newThreadsCount));

        assertThat(newThreadsCount, lessThanOrEqualTo(1));
    }

    @Test
    public void singleCallableThread() {
        final LocalCallableString localCallableString = new LocalCallableString();
        final ThreadResultStringConsumer threadResultStringConsumer = spy(new ThreadResultStringConsumer());

        // @formatter:off
        ThreadBuilder
                .newBuilder()
                .setScheduling(localCallableString)
                .setThreadResultConsumer(threadResultStringConsumer)
                .start();
        // @formatter:on

        ThreadUtil.sleepUnchecked(1500);

        verify(threadResultStringConsumer).accept(LocalCallableString.THIS_IS_A_CALLABLE_THREAD);
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
