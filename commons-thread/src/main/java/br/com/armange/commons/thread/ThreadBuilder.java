package br.com.armange.commons.thread;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Useful structure for thread creation in the following scenarios:
 * <ul>
 * <li><em><b>Timeout</b></em><br>
 * <p>The thread will be active only until the timeout be fired.</p></li>
 * <li><em><b>Delay</b></em><br>
 * <p>The thread will be active only after the time delay be completed.</p></li>
 * <li><em><b>Interval</b></em><br>
 * <p>The thread will be repeated after the time interval be completed.</p></li>
 * <li><em><b>Exception handling</b></em><br>
 * <p>Handles of uncaught exceptions can be thrown and handled within threads.</p></li>
 * </ul>
 * <b>Note:</b><br>
 * <em>The thread will wait a minimum delay
 * ({@link br.com.armange.commons.thread.ThreadBuilder#MINIMAL_REQUIRED_DELAY}) if and only if a
 * ({@link br.com.armange.commons.thread.ThreadBuilder#setAfterExecuteConsumer(BiConsumer)}) 
 * or a ({@link br.com.armange.commons.thread.ThreadBuilder#setUncaughtExceptionConsumer(Consumer)}) 
 * is present.</em>
 * 
 * <pre>
 * <b>Example:</b>
 * 
 * final ExecutorService thread = ThreadBuilder
 *          .newBuilder() //New object to build a new thread.
 *          .setDelay(1000) //The thread will wait one second before start.
 *          .setTimeout(4000) //The thread will be canceled after four seconds.
 *          .setInterval(1000) //The thread will be repeated every second. 
 *          .setAfterExecuteConsumer(afterExecuteConsumer) //A consumer will be called after thread execution.
 *          .setUncaughtExceptionConsumer(throwableConsumer) //A consumer will be called after any exception thrown.
 *          .setMayInterruptIfRunning(true) //The thread interruption/cancellation will not wait execution.
 *          .setSilentInterruption(true) //Interruption and Cancellation exceptions will not be thrown.
 *          .setExecution(anyRunnable) //The thread execution.
 *          .start();
 * </pre>
 * 
 * @author Diego Armange Costa
 * @since 2019-11-26 V1.0.0 (JDK 1.8)
 * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService
 */
public class ThreadBuilder {
    /**
     * 1000 milliseconds as a minimal delay.
     */
    public static final long MINIMAL_REQUIRED_DELAY = 1000;
    private Optional<Duration> timeout = Optional.empty();
    private Optional<Duration> delay = Optional.empty();
    private Optional<Duration> interval = Optional.empty();
    private Optional<BiConsumer<Runnable, Throwable>> afterExecuteConsumer = Optional.empty();
    private Optional<Consumer<Throwable>> uncaughtExceptionConsumer = Optional.empty();
    private Runnable execution;
    private ScheduledCaughtExecutorService executor;
    private ExecutorResult executorResult;
    private boolean mayInterruptIfRunning;
    private boolean silentInterruption;
    private final int corePoolSize;

    private ThreadBuilder() {
        corePoolSize = 1;
    }
    
    private ThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * @return a new object to perform a thread creation.
     */
    public static ThreadBuilder newBuilder() {
        return new ThreadBuilder();
    }
    
    /**
     * @param corePoolSize the {@link ScheduledCaughtExecutorService} pool size.
     * @return a new object to perform a thread creation.
     * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService#ScheduledCaughtExecutorService(int)
     */
    public static ThreadBuilder newBuilder(final int corePoolSize) {
        return new ThreadBuilder(corePoolSize);
    }

    /**
     * Sets the timeout value.
     * @param milliseconds the timeout value in milliseconds. 
     * @return the current thread builder.
     */
    public ThreadBuilder setTimeout(final long milliseconds) {
        timeout = Optional.of(Duration.ofMillis(milliseconds));

        return this;
    }

    /**
     * Sets the delay value.
     * @param milliseconds the delay value in milliseconds.
     * @return the current thread builder.
     */
    public ThreadBuilder setDelay(final long milliseconds) {
        delay = Optional.of(Duration.ofMillis(milliseconds));

        return this;
    }

    /**
     * Sets the repeating interval value.
     * @param milliseconds the repeating interval value in milliseconds.
     * @return the current thread builder.
     */
    public ThreadBuilder setInterval(final long milliseconds) {
        interval = Optional.of(Duration.ofMillis(milliseconds));

        return this;
    }

    /**
     * Sets the consumer to be called after thread execution.
     * @param afterExecuteConsumer the consumer to be called after thread execution.
     * @return the current thread builder.
     * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService#afterExecute(Runnable, Throwable)
     */
    public ThreadBuilder setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteConsumer) {
        this.afterExecuteConsumer = Optional.ofNullable(afterExecuteConsumer);

        return this;
    }

    /**
     * Sets the consumer to be called after exception throwing. This consumer will be called as a first after-executes 
     * consumer.
     * @param uncaughtExceptionConsumer the consumer to be called after exception throwing.
     * @return the current thread builder.
     */
    public ThreadBuilder setUncaughtExceptionConsumer(final Consumer<Throwable> uncaughtExceptionConsumer) {
        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);

        return this;
    }

    /**
     * Sets the thread execution.
     * @param execution the thread execution({@link java.lang.Runnable})
     * @return the current thread builder.
     */
    public ThreadBuilder setExecution(final Runnable execution) {
        this.execution = execution;

        requireExecutionNonNull();

        return this;
    }

    /**
     * Sets the thread-interrupting-flag.
     * @param flag true if the thread executing this task should be interrupted; 
     * otherwise, in-progress tasks are allowed to complete.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    public ThreadBuilder setMayInterruptIfRunning(final boolean flag) {
        mayInterruptIfRunning = flag;

        return this;
    }

    /**
     * Sets the thread-silent-interrupting-flag.
     * @param flag true if the Interruption/Cancellation exceptions should be ignored.
     * @return the current thread builder.
     * @see java.util.concurrent.Future#cancel(boolean)
     * @see java.util.concurrent.CancellationException
     * @see java.lang.InterruptedException
     */
    public ThreadBuilder setSilentInterruption(final boolean flag) {
        silentInterruption = flag;

        return this;
    }

    /**
     * Starts the thread.
     * @return the executor's result after starting thread.
     * @see ExecutorResult
     */
    public ExecutorResult start() {
        createExecutorAndRunThread();

        return executorResult;
    }
    
    /**
     * Starts the thread.
     * @return the current thread builder to prepare another thread.
     */
    public ThreadBuilder startAndBuildOther() {
        createExecutorAndRunThread();

        return this;
    }

    private void createExecutorAndRunThread() {
        requireExecutionNonNull();

        executor = new ScheduledCaughtExecutorService(corePoolSize, getThreadFactory());
        
        runThread();

        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
    }

    private ThreadFactory getThreadFactory() {
        return uncaughtExceptionConsumer
                .map(ueh -> new CaughtExecutorThreadFactory((thread, throwable) -> ueh.accept(throwable)))
                .orElse(new CaughtExecutorThreadFactory(null));
    }

    private void runThread() {
        if (noSchedule()) {
            runWithNoSchedule();
        } else if (onlyDelay()) {
            runWithDelay();
        } else if (onlyTimeout()) {
            runWithTimeout();
        } else if (onlyInterval()) {
            repeatWithInterval();
        } else if (delayAndTimeout()) {
            runWithDelayAndTimeout();
        } else if (delayAndInterval()) {
            runWithDelayAndInterval();
        } else if (timeoutAndInterval()) {
            runWithTimeoutAndInterval();
        } else /* All */ {
            runWithAllTimesControls();
        }
    }

    private void requireExecutionNonNull() {
        Objects.requireNonNull(execution, "The {execution} parameter is required");
    }

    private boolean noSchedule() {
        return !delay.isPresent() && !timeout.isPresent() && !interval.isPresent();
    }

    private boolean onlyDelay() {
        return delay.isPresent() && !timeout.isPresent() && !interval.isPresent();
    }

    private boolean onlyTimeout() {
        return !delay.isPresent() && timeout.isPresent() && !interval.isPresent();
    }

    private boolean onlyInterval() {
        return !delay.isPresent() && !timeout.isPresent() && interval.isPresent();
    }

    private boolean delayAndTimeout() {
        return delay.isPresent() && timeout.isPresent() && !interval.isPresent();
    }

    private boolean delayAndInterval() {
        return delay.isPresent() && !timeout.isPresent() && interval.isPresent();
    }

    private boolean timeoutAndInterval() {
        return !delay.isPresent() && timeout.isPresent() && interval.isPresent();
    }

    private void runWithNoSchedule() {
        final Future<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithDelay() {
        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithTimeout() {
        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
        
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private void repeatWithInterval() {
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
                interval.get().toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }

    private void runWithDelayAndTimeout() {
        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
        
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private void runWithDelayAndInterval() {
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
                interval.get().toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
    }
    
    private void newExecutorResultIfNull() {
        executorResult = executorResult == null ? new ExecutorResult(executor) : executorResult;
    }

    private void runWithTimeoutAndInterval() {
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
                interval.get().toMillis(), TimeUnit.MILLISECONDS);

        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
        
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private void runWithAllTimesControls() {
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
                interval.get().toMillis(), TimeUnit.MILLISECONDS);
        
        executor.addAfterExecuteConsumer(handleException(future));

        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
        
        newExecutorResultIfNull();
        executorResult.getFutures().add(future);
        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
    }

    private long handleDelay() {
        final long localDelay = delay.orElse(Duration.ofMillis(0)).toMillis();
        
        if (uncaughtExceptionConsumer.isPresent() || afterExecuteConsumer.isPresent()) {
            return localDelay >= MINIMAL_REQUIRED_DELAY ? localDelay : localDelay + MINIMAL_REQUIRED_DELAY;
        } else {
            return localDelay;
        }
    }

    private BiConsumer<Runnable, Throwable> handleException(final Future<?> future) {
        return (a, b) -> {
            try {
                if (future.isDone()) future.get();
            } catch (final InterruptedException | ExecutionException | CancellationException e) {
                if (isNotSilentOrIsExecutionException(e)) {
                    uncaughtExceptionConsumer.ifPresent(consumer -> consumer.accept(e));
                }
            }
        };
    }

    private boolean isNotSilentOrIsExecutionException(final Exception e) {
        return silentInterruption == false
                || !(e instanceof CancellationException) && !(e instanceof InterruptedException);
    }

    private ExecutorResult handleInterruption(final ScheduledFuture<?> future) {
        final ScheduledCaughtExecutorService executor = new ScheduledCaughtExecutorService(1);
        
        executor.addAfterExecuteConsumer(handleException(future));
        executor.schedule(cancelFuture(future), timeout.get().toMillis(), TimeUnit.MILLISECONDS);
        
        final ExecutorResult timeoutExecutorResult = new ExecutorResult(executor);
        
        timeoutExecutorResult.getFutures().add(future);
        
        return timeoutExecutorResult;
    }

    private Runnable cancelFuture(final ScheduledFuture<?> future) {
        return () -> {if(!future.isDone() && !future.isCancelled()) future.cancel(mayInterruptIfRunning);};
    }
}
