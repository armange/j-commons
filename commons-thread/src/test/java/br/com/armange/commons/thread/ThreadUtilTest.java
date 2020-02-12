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
package br.com.armange.commons.thread;

import java.io.InputStream;
import java.net.URL;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ThreadUtilTest {

    private static final String TEST_FILE_RELATIVE_PATH = "TestFile.txt";
    
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
        
        Assert.assertThat(new Long(end - start), Matchers.greaterThanOrEqualTo(1000L));
        Assert.assertThat(new Long(end - start), Matchers.lessThanOrEqualTo(1200L));
    }
    
    @Test(expected = RuntimeException.class)
    public void sleepUncheckedThrowRuntimeException() {
        ThreadUtil.sleepUnchecked(-1);
    }
}
