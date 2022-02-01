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

import br.com.armange.commons.thread.exception.UncheckedException;

import java.io.InputStream;
import java.net.URL;
import java.util.function.BooleanSupplier;

/**
 * Useful structure for handling the current segment.
 * Some methods that require handling a checked exception
 * are rewritten in this class, however, throwing an unchecked exception.
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class ThreadUtil {

    private ThreadUtil() {}
    
    /**
     * Wraps a thread-sleep execution in a try-catch block and rethrow a
     * {@link java.lang.RuntimeException#RuntimeException(Throwable)}
     * if any exception is thrown.
     * @param millis the length of time to sleep in milliseconds
     * @see java.lang.Thread#sleep(long)
     */
    public static void sleepUnchecked(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedException(e);
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

    /**
     * Keeps the thread in sleep state until the given condition is true.
     * @param millis the length of time to sleep in milliseconds
     * @param condition The condition for the current thread to continue sleeping.
     *                  When it has the true value the thread will continue to sleep.
     * @throws InterruptedException if any thread has interrupted the current thread. The
     * <i>interrupted status</i> of the current thread is cleared when this exception is thrown.
     * @see java.lang.Thread#sleep(long)
     */
    public static void sleepUntil(final long millis, final BooleanSupplier condition)
            throws InterruptedException {
        while(condition.getAsBoolean()) {
            Thread.sleep(millis);
        }
    }

    /**
     * Wraps a thread-sleep execution in a try-catch block and rethrow a
     * {@link java.lang.RuntimeException#RuntimeException(Throwable)}
     * if any exception is thrown. Keeps the thread in sleep state until the given
     * condition is true.
     * @param millis the length of time to sleep in milliseconds
     * @param condition The condition for the current thread to continue sleeping.
     *                  When it has the true value the thread will continue to sleep.
     * @see ThreadUtil#sleepUnchecked(long)
     */
    public static void sleepUncheckedUntil(final long millis, final BooleanSupplier condition) {
        while(condition.getAsBoolean()) {
            sleepUnchecked(millis);
        }
    }
}
