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

import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.armange.commons.thread.core.ScheduledCaughtExceptionExecutorService;
import br.com.armange.commons.thread.util.ThreadUtil;

/**
 * @author Diego Armange Costa
 * @since 2019-11-18 V1.0.0 (JDK 1.8)
 */
public class ThreadBuilderTest {
    
    private static final String ACTIVE_THREADS = "Active threads: %d";
    private static final String ACTIVE_THREADS_BEFORE_THREADS = "Active threads before test: %d";

//    private void changeexecutionTypeToNull(final TimingRunnableThreadBuilder<?> builder) {
//        try {
//            final Field field = AbstractThreadBuilder.class.getDeclaredField("executionType");
//            
//            field.setAccessible(true);
//            
//            final Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//            
//            field.set(builder, null);
//        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//            e.printStackTrace();
//            fail(e.getMessage(), e);
//        }
//    }
    
    @Test
    public void xxxxxxxxx() {
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
        
        assertThat(result.getExecutorService(), instanceOf(ScheduledCaughtExceptionExecutorService.class));
        
        final ScheduledCaughtExceptionExecutorService executor = (ScheduledCaughtExceptionExecutorService) result
                .getExecutorService();
        
        assertEquals(corePoolSize, executor.getActiveCount());
        
        ThreadUtil.sleepUnchecked(1500);
        
        final int activeCountAfterTest = Thread.activeCount();
        final int newThreadsCount = activeCountAfterTest - activeCountBeforeTest;
        
        out.println(String.format(ACTIVE_THREADS, newThreadsCount));
        
        assertThat(newThreadsCount, greaterThanOrEqualTo(5));
    }
    
    @Test
    public void yyyyyyyyyy() {
        final int activeCountBeforeTest = Thread.activeCount();
        final int corePoolSize = 1;
        
        out.println(String.format(ACTIVE_THREADS_BEFORE_THREADS, activeCountBeforeTest));
        
        final ExecutorResult<?> result = ThreadBuilder
            .newBuilder(corePoolSize)
            .setScheduling(() -> ThreadUtil.sleepUnchecked(10000))
            .start();
        
        assertThat(result.getExecutorService(), instanceOf(ScheduledCaughtExceptionExecutorService.class));
        
        final ScheduledCaughtExceptionExecutorService executor = (ScheduledCaughtExceptionExecutorService) result
                .getExecutorService();
        
        assertEquals(corePoolSize, executor.getActiveCount());
        
        ThreadUtil.sleepUnchecked(1500);
        
        final int activeCountAfterTest = Thread.activeCount();
        final int newThreadsCount = activeCountAfterTest - activeCountBeforeTest;
        
        out.println(String.format(ACTIVE_THREADS, newThreadsCount));
        
        assertThat(newThreadsCount, lessThanOrEqualTo(1));
    }
}
