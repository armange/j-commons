package br.com.armange.commons.thread;

import java.io.InputStream;
import java.net.URL;

/**
 * Useful structure for handling the current thread.
 * @author Diego Armange Costa
 * @since 2019-11-26 V1.0.0
 */
public class ThreadUtil {

    private ThreadUtil() {}
    
    /**
     * It wraps a thread-sleep execution in a try-catch block and rethrow a {@Link java.lang.RuntimeException.RuntimeException(Throwable)} 
     * if any exception is thrown.
     * @param millis the time in milliseconds to sleep the current thread.
     * @see java.lang.Thread#sleep(long)
     */
    public static void sleepUnchecked(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @param relativePath the resource relative path.
     * @return The input stream found or null if not found.
     * @see java.lang.ClassLoader.getResourceAsStream(String)
     * @see java.lang.Thread.getContextClassLoader()
     * @see java.lang.Thread.currentThread()
     */
    public static InputStream getCurrentThreadResourceAsStream(final String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
    }
    
    /**
     * @param relativePath the resource relative path.
     * @return The URL found or null if not found.
     * @see java.lang.ClassLoader.getResource(String)
     * @see java.lang.Thread.getContextClassLoader()
     * @see java.lang.Thread.currentThread()
     */
    public static URL getCurrentThreadResource(final String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResource(relativePath);
    }
}
