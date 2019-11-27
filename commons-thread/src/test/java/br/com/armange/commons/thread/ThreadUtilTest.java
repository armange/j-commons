package br.com.armange.commons.thread;

import java.io.InputStream;
import java.net.URL;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import br.com.armange.commons.thread.ThreadUtil;

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
