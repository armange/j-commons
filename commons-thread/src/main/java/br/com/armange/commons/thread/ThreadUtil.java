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
import java.util.function.Supplier;

/**
 * Useful structure for handling the current thread.
 * @author Diego Armange Costa
 * @since 2019-11-26 V1.0.0
 */
public class ThreadUtil {

    private ThreadUtil() {}
    
    /**
     * It wraps a thread-sleep execution in a try-catch block and rethrow a {@link java.lang.RuntimeException#RuntimeException(Throwable)} 
     * if any exception is thrown.
     * @param millis the time in milliseconds to sleep the current thread.
     * @see java.lang.Thread#sleep(long)
     */
    public static void sleepUnchecked(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final Exception e) {
            throw new UncheckedException(e);
        }
    }
    
    /**
     * @param relativePath the resource relative path.
     * @return The input stream found or null if not found.
     * @see java.lang.ClassLoader#getResourceAsStream(String)
     * @see java.lang.Thread#getContextClassLoader()
     * @see java.lang.Thread#currentThread()
     */
    public static InputStream getCurrentThreadResourceAsStream(final String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
    }
    
    /**
     * @param relativePath the resource relative path.
     * @return The URL found or null if not found.
     * @see java.lang.ClassLoader#getResource(String)
     * @see java.lang.Thread#getContextClassLoader()
     * @see java.lang.Thread#currentThread()
     */
    public static URL getCurrentThreadResource(final String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResource(relativePath);
    }
    
    public static void sleepUntil(final long millis, final Supplier<Boolean> condition) throws InterruptedException {
        while(condition.get()) {
            Thread.sleep(millis);
        }
    }
    
    public static void sleepUncheckedUntil(final long millis, final Supplier<Boolean> condition) {
        while(condition.get()) {
            try {
                Thread.sleep(millis);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
