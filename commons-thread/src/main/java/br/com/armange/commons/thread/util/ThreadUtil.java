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
 * Utilities for sleeping on the current thread and looking up resources
 * through its context class loader. The unchecked sleep methods wrap failures
 * in {@code UncheckedException}; they do not provide scheduling or a timeout.
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
public class ThreadUtil {

    private ThreadUtil() {}
    
    /**
     * Sleeps the current thread for the requested duration. On interruption,
     * restores the interrupted status and throws {@code UncheckedException}
     * with the {@code InterruptedException} as cause. Other exceptions from
     * {@code Thread.sleep}, including an invalid duration, are also wrapped.
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
     * Looks up a resource with the current thread's context class loader.
     * The caller owns closing a non-null stream. A null context class loader
     * causes {@code NullPointerException}.
     * @param relativePath the resource name relative to that class loader
     * @return the input stream, or {@code null} if the resource is not found
     * @see java.lang.ClassLoader#getResourceAsStream(String)
     * @see java.lang.Thread#getContextClassLoader()
     * @see java.lang.Thread#currentThread()
     */
    public static InputStream getCurrentThreadResourceAsStream(final String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
    }
    
    /**
     * Looks up a resource URL with the current thread's context class loader.
     * A null context class loader causes {@code NullPointerException}.
     * @param relativePath the resource name relative to that class loader
     * @return the URL, or {@code null} if the resource is not found
     * @see java.lang.ClassLoader#getResource(String)
     * @see java.lang.Thread#getContextClassLoader()
     * @see java.lang.Thread#currentThread()
     */
    public static URL getCurrentThreadResource(final String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResource(relativePath);
    }

    /**
     * Evaluates the condition before each sleep. Sleeps while it returns
     * {@code true}; returns when it becomes {@code false}. This method does not
     * enforce a deadline, and a permanently true condition can wait forever.
     * @param millis the length of each sleep in milliseconds
     * @param condition supplies {@code true} while another sleep is required
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
     * Evaluates the condition before each unchecked sleep. Sleeps while it
     * returns {@code true}; returns when it becomes {@code false}. Interruption
     * is reasserted and wrapped as in {@link #sleepUnchecked(long)}. A
     * permanently true condition can wait forever.
     * @param millis the length of each sleep in milliseconds
     * @param condition supplies {@code true} while another sleep is required
     * @see ThreadUtil#sleepUnchecked(long)
     */
    public static void sleepUncheckedUntil(final long millis, final BooleanSupplier condition) {
        while(condition.getAsBoolean()) {
            sleepUnchecked(millis);
        }
    }
}
