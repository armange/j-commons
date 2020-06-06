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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import br.com.armange.commons.thread.builder.ThreadBuilder;

public class ThreadUtilTest {

    private static final String TEST_FILE_RELATIVE_PATH = "TestFile.txt";
    
    private class SleepUntilSupplier implements Supplier<Boolean> {
        byte count = 3;
        
        @Override
        public Boolean get() {
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
        
        Assert.assertThat((Long)(end - start), Matchers.greaterThanOrEqualTo(1000L));
        Assert.assertThat((Long)(end - start), Matchers.lessThanOrEqualTo(1200L));
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
        verify(sleepUntilSupplier, times(4)).get();
        
        ThreadUtil.sleepUnchecked(1000);
        
        verify(sleepUntilSupplier, times(4)).get();
    }
    
    @Test
    public void sleepUncheckedUntil() {
        final SleepUntilSupplier sleepUntilSupplier = spy(new SleepUntilSupplier());
        
        ThreadUtil.sleepUncheckedUntil(1000, sleepUntilSupplier);
        
        //Tree time for true and one for false.
        verify(sleepUntilSupplier, times(4)).get();
        
        ThreadUtil.sleepUnchecked(1000);
        
        verify(sleepUntilSupplier, times(4)).get();
    }
    
    @Test
    public void throwInSleepUncheckedUntil() {
        final SleepUntilSupplier sleepUntilSupplier = spy(new SleepUntilSupplier());
        
        ThreadBuilder
            .newBuilder()
            .setExecution(() -> ThreadUtil.sleepUncheckedUntil(2000, sleepUntilSupplier))
            .setTimeout(500)
            .setMayInterruptIfRunning(true)
            .start();
        
        ThreadUtil.sleepUnchecked(1500);
        
        verify(sleepUntilSupplier).get();
    }
}
