package br.com.armange.commons.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

/**
 * A thread-factory that sets some thread parameters if non-null:
 * <ul>
 * <li>Name</li>
 * <li>Priority</li>
 * <li>Uncaught exception handler</li>
 * </ul>
 * @author Diego Armange Costa
 * @since 2019-11-26 V1.0.0 (JDK 1.8)
 * @see java.util.concurrent.ThreadFactory
 */
public class CaughtExecutorThreadFactory implements ThreadFactory {
    private Optional<UncaughtExceptionHandler> uncaughtExceptionHandler = Optional.empty();
    private Optional<String> threadName = Optional.empty();
    private Optional<Integer> threadPriority = Optional.empty();
    
    /**
     * Sets some thread parameters before return a new thread:
     * <ul>
     * <li>Name</li>
     * <li>Priority</li>
     * <li>Uncaught exception handler</li>
     * </ul>
     * These thread parameters will be used if they are not null.
     * @see #setUncaughtExceptionHandler(UncaughtExceptionHandler)
     * @see #setThreadName(String)
     * @see #setThreadPriority(Integer)
     * @see java.util.concurrent.ThreadFactory#newThread(Runnable)
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        
        uncaughtExceptionHandler.ifPresent(thread::setUncaughtExceptionHandler);
        
        threadName.ifPresent(thread::setName);
        
        threadPriority.ifPresent(thread::setPriority);
        
        return thread;
    }
    
    /**
     * Sets the uncaught exception handler to be used in a new thread.
     * @param uncaughtExceptionHandler the uncaught exception handler.
     */
    public void setUncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = Optional.ofNullable(uncaughtExceptionHandler);
    }
    
    /**
     * Sets the thread name to be used in a new thread.
     * @param threadName the thread name.
     */
    public void setThreadName(final String threadName) {
        this.threadName = Optional.ofNullable(threadName);
    }
    
    /**
     * Sets the thread priority to be used in a new thread.
     * @param threadPriority the thread priority
     */
    public void setThreadPriority(final Integer threadPriority) {
        this.threadPriority = Optional.ofNullable(threadPriority);
    }
}
