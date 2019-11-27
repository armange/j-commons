package br.com.armange.commons.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

/**
 * A thread-factory that sets a {@link java.lang.Thread.UncaughtExceptionHandler} if non-null.
 * @author Diego Armange Costa
 * @since 2019-11-26 V1.0.0 (JDK 1.8)
 * @see java.util.concurrent.ThreadFactory
 * @see java.lang.Thread#setUncaughtExceptionHandler(UncaughtExceptionHandler)
 */
public class CaughtExecutorThreadFactory implements ThreadFactory {
    private final UncaughtExceptionHandler uncaughtExceptionHandler;
    
    public CaughtExecutorThreadFactory(final UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    /**
     * @see java.util.concurrent.ThreadFactory#newThread(Runnable)
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        
        Optional.ofNullable(uncaughtExceptionHandler).ifPresent(thread::setUncaughtExceptionHandler);
        
        return thread;
    }
}
