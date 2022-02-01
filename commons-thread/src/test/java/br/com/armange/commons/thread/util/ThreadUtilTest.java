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
package br.com.armange.commons.thread.util;

import br.com.armange.commons.thread.builder.ThreadBuilder;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.BooleanSupplier;

import static org.mockito.Mockito.*;

public class ThreadUtilTest {

    private static final String TEST_FILE_RELATIVE_PATH = "TestFile.txt";

    private class SleepUntilSupplier implements BooleanSupplier {
        byte count = 3;

        @Override
        public boolean getAsBoolean() {
            if (count <= 0) {
                return false;
            }

            count--;

            return true;
        }
    }

    @Test
    public void findResourceURL() {
        final URL url = ThreadUtil.getCurrentThreadResource(TEST_FILE_RELATIVE_PATH);

        Assert.assertNotNull(url);
    }

    @Test
    public void findResourceInputStream() {
        final InputStream is = ThreadUtil.getCurrentThreadResourceAsStream(TEST_FILE_RELATIVE_PATH);

        Assert.assertNotNull(is);
    }

    @Test
    public void sleepUnchecked() {
        final long start = System.currentTimeMillis();

        ThreadUtil.sleepUnchecked(1000);

        final long end = System.currentTimeMillis();

        Assert.assertThat((Long) (end - start), Matchers.greaterThanOrEqualTo(1000L));
        Assert.assertThat((Long) (end - start), Matchers.lessThanOrEqualTo(1200L));
    }

    @Test
    public void interruptSleepUnchecked() {
        final Thread t1 = spy(new Thread(() -> System.out.println("Thread 01 executed.")));
        final Thread t2 = spy(new Thread(() -> {
            System.out.println("Thread 02 executed.");
            ThreadUtil.sleepUnchecked(1000);
            t1.start();
        }));

        t2.start();
        ThreadUtil.sleepUnchecked(100);
        t2.interrupt();

        verifyNoInteractions(t1);
        verify(t2).run();
    }

    @Test(expected = RuntimeException.class)
    public void sleepUncheckedThrowRuntimeException() {
        ThreadUtil.sleepUnchecked(-1);
    }

    @Test
    public void sleepUntil() throws InterruptedException {
        final SleepUntilSupplier sleepUntilSupplier = spy(new SleepUntilSupplier());

        ThreadUtil.sleepUntil(1000, sleepUntilSupplier);

        //Tree time for true and one for false.
        verify(sleepUntilSupplier, times(4)).getAsBoolean();

        ThreadUtil.sleepUnchecked(1000);

        verify(sleepUntilSupplier, times(4)).getAsBoolean();
    }

    @Test
    public void sleepUncheckedUntil() {
        final SleepUntilSupplier sleepUntilSupplier = spy(new SleepUntilSupplier());

        ThreadUtil.sleepUncheckedUntil(1000, sleepUntilSupplier);

        //Tree time for true and one for false.
        verify(sleepUntilSupplier, times(4)).getAsBoolean();

        ThreadUtil.sleepUnchecked(1000);

        verify(sleepUntilSupplier, times(4)).getAsBoolean();
    }

    @Test
    public void throwInSleepUncheckedUntil() {
        final SleepUntilSupplier sleepUntilSupplier = spy(new SleepUntilSupplier());

        ThreadBuilder
                .newBuilder()
                .setScheduling(() -> ThreadUtil.sleepUncheckedUntil(2000, sleepUntilSupplier))
                .setTimeout(500)
                .setMayInterruptIfRunning(true)
                .start();

        ThreadUtil.sleepUnchecked(1500);

        verify(sleepUntilSupplier).getAsBoolean();
    }

    @Test
    public void shouldHaveAPrivateDefaultConstructor() throws InvocationTargetException,
            InstantiationException, IllegalAccessException {
        ClassTestUtil.assertPrivateDefaultConstructor(ThreadUtil.class);
    }
}
