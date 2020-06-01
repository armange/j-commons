///*
// * Copyright [2019] [Diego Armange Costa]
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * */
//package br.com.armange.commons.thread;
//
//import java.time.Duration;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.concurrent.CancellationException;
//import java.util.concurrent.Future;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.IntSupplier;
//import java.util.function.Supplier;
//
//import br.com.armange.commons.thread.message.ExceptionMessage;
//
//public abstract class AbstractThreadBuilder<S, T, U extends AbstractThreadBuilder<S, T, U>> {
//    
//    protected static enum ThreadTimeConfig {
//        NO_SCHEDULE,
//        DELAY,
//        TIMEOUT,
//        INTERVAL,
//        DELAY_AND_TIMEOUT,
//        DELAY_AND_INTERVAL,
//        ALL_CONFIGURATION
//    }
//    
//    /**
//     * 1000 milliseconds as a minimal delay.
//     */
//    public static final long MINIMAL_REQUIRED_DELAY = 1000;
//    protected Optional<Duration> timeout = Optional.empty();
//    protected Optional<Duration> delay = Optional.empty();
//    protected Optional<Duration> interval = Optional.empty();
//    protected Optional<BiConsumer<Runnable, Throwable>> afterExecuteConsumer = Optional.empty();
//    protected Optional<Consumer<? super Throwable>> uncaughtExceptionConsumer = Optional.empty();
//    protected Optional<CaughtExecutorThreadFactory> threadFactory = Optional.empty();
//    protected Optional<Supplier<String>> threadNameSupplier = Optional.empty();
//    protected Optional<IntSupplier> threadPrioritySupplier = Optional.empty();
//    protected ThreadTimeConfig threadTimeConfig;
//    protected T execution;
//    protected ScheduledCaughtExecutorService executor;
//    protected ExecutorResult executorResult;
//    protected boolean mayInterruptIfRunning;
//    protected boolean silentInterruption; 
//    protected final int corePoolSize;
//
//    protected AbstractThreadBuilder() {
//        corePoolSize = 1;
//    }
//    
//    protected AbstractThreadBuilder(final int corePoolSize) {
//        this.corePoolSize = corePoolSize;
//    }
//    
//    /**
//     * Sets the timeout value.
//     * @param milliseconds the timeout value in milliseconds. 
//     * @return the current thread builder.
//     */
//    public U setTimeout(final long milliseconds) {
//        timeout = Optional.of(Duration.ofMillis(milliseconds));
//
//        threadTimeConfig = null;
//        
//        return getSelf();
//    }
//
//    /**
//     * Sets the delay value.
//     * @param milliseconds the delay value in milliseconds.
//     * @return the current thread builder.
//     */
//    public U setDelay(final long milliseconds) {
//        delay = Optional.of(Duration.ofMillis(milliseconds));
//        
//        threadTimeConfig = null;
//
//        return getSelf();
//    }
//
//    /**
//     * Sets the repeating interval value.
//     * @param milliseconds the repeating interval value in milliseconds.
//     * @return the current thread builder.
//     */
//    public U setInterval(final long milliseconds) {
//        interval = Optional.of(Duration.ofMillis(milliseconds));
//        
//        threadTimeConfig = null;
//
//        return getSelf();
//    }
//
//    /**
//     * Sets the consumer to be called after thread execution.
//     * @param afterExecuteConsumer the consumer to be called after thread execution.
//     * @return the current thread builder.
//     * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService#afterExecute(Runnable, Throwable)
//     */
//    public U setAfterExecuteConsumer(final BiConsumer<Runnable, Throwable> afterExecuteConsumer) {
//        this.afterExecuteConsumer = Optional.ofNullable(afterExecuteConsumer);
//
//        return getSelf();
//    }
//
//    /**
//     * Sets the consumer to be called after exception throwing. This consumer will be called as a first after-executes 
//     * consumer.
//     * @param uncaughtExceptionConsumer the consumer to be called after exception throwing.
//     * @return the current thread builder.
//     */
//    public U setUncaughtExceptionConsumer(final Consumer<? super Throwable> uncaughtExceptionConsumer) {
//        this.uncaughtExceptionConsumer = Optional.ofNullable(uncaughtExceptionConsumer);
//
//        return getSelf();
//    }
//    
//    /**
//     * Sets the thread name supplier.The thread factory will consume this supplier to generate a thread name 
//     * before return a new thread.
//     * @param threadNameSupplier the thread name supplier.
//     * @return the current thread builder.
//     */
//    public U setThreadNameSupplier(final Supplier<String> threadNameSupplier) {
//        this.threadNameSupplier = Optional.ofNullable(threadNameSupplier);
//        
//        return getSelf();
//    }
//    
//    /**
//     * Sets the thread priority supplier.The thread factory will consume this supplier to generate a thread priority 
//     * before return a new thread.
//     * @param threadPrioritySupplier the thread priority supplier.
//     * @return the current thread builder.
//     */
//    public U setThreadPrioritySupplier(final IntSupplier threadPrioritySupplier) {
//        this.threadPrioritySupplier = Optional.ofNullable(threadPrioritySupplier);
//        
//        return getSelf();
//    }
//
//    /**
//     * Sets the thread execution.
//     * @param execution the thread execution({@link java.lang.Runnable})
//     * @return the current thread builder.
//     */
//    public U setExecution(final T execution) {
//        this.execution = execution;
//
//        requireExecutionNonNull();
//
//        return getSelf();
//    }
//    
//    /**
//     * Sets the thread-interrupting-flag.
//     * @param flag true if the thread executing this task should be interrupted; 
//     * otherwise, in-progress tasks are allowed to complete.
//     * @return the current thread builder.
//     * @see java.util.concurrent.Future#cancel(boolean)
//     */
//    public U setMayInterruptIfRunning(final boolean flag) {
//        mayInterruptIfRunning = flag;
//
//        return getSelf();
//    }
//
//    /**
//     * Sets the thread-silent-interrupting-flag.
//     * @param flag true if the Interruption/Cancellation exceptions should be ignored.
//     * @return the current thread builder.
//     * @see java.util.concurrent.Future#cancel(boolean)
//     * @see java.util.concurrent.CancellationException
//     * @see java.lang.InterruptedException
//     */
//    public U setSilentInterruption(final boolean flag) {
//        silentInterruption = flag;
//
//        return getSelf();
//    }
//
//    @SuppressWarnings("unchecked")
//    private U getSelf() {
//        return (U) this;
//    }
//    
//    protected void createExecutorAndRunThread() {
//        requireExecutionNonNull();
//
//        executor = new ScheduledCaughtExecutorService(corePoolSize, getThreadFactory());
//        
//        readThreadTimeConfig();
//        
//        runThread();
//
//        afterExecuteConsumer.ifPresent(executor::addAfterExecuteConsumer);
//    }
//    
//    private void requireExecutionNonNull() {
//        Objects.requireNonNull(execution, "The {execution} parameter is required");
//    }
//    
//    private ThreadFactory getThreadFactory() {
//        final CaughtExecutorThreadFactory factory = threadFactory.orElse(new CaughtExecutorThreadFactory());
//        
//        uncaughtExceptionConsumer
//                .ifPresent(uec -> factory.setUncaughtExceptionHandler((thread, throwable) -> uec.accept(throwable)));
//        
//        threadNameSupplier.ifPresent(tns -> factory.setThreadName(tns.get()));
//        
//        threadPrioritySupplier.ifPresent(tps -> factory.setThreadPriority(tps.getAsInt()));
//        
//        return factory;
//    }
//    
//    private void readThreadTimeConfig() {
//        if (this.threadTimeConfig == null)
//            if (noSchedule()) {
//                this.threadTimeConfig = ThreadTimeConfig.NO_SCHEDULE;
//            } else if (onlyDelay()) {
//                this.threadTimeConfig = ThreadTimeConfig.DELAY;
//            } else if (onlyTimeout()) {
//                this.threadTimeConfig = ThreadTimeConfig.TIMEOUT;
//            } else if (onlyInterval()) {
//                this.threadTimeConfig = ThreadTimeConfig.INTERVAL;
//            } else if (delayAndTimeout()) {
//                this.threadTimeConfig = ThreadTimeConfig.DELAY_AND_TIMEOUT;
//            } else if (delayAndInterval()) {
//                this.threadTimeConfig = ThreadTimeConfig.DELAY_AND_INTERVAL;
//            } else /* All */ {
//                this.threadTimeConfig = ThreadTimeConfig.ALL_CONFIGURATION;
//            }
//    }
//    
//    private void runThread() {
//        switch(threadTimeConfig) {
//        case DELAY:
//            runWithDelay();
//            break;
//        case DELAY_AND_INTERVAL:
//            runWithDelayAndInterval();
//            break;
//        case DELAY_AND_TIMEOUT:
//            runWithDelayAndTimeout();
//            break;
//        case INTERVAL:
//            runWithDelayAndInterval();
//            break;
//        case NO_SCHEDULE:
//            runWithNoSchedule();
//            break;
//        case TIMEOUT:
//            runWithDelayAndTimeout();
//            break;
//        case ALL_CONFIGURATION:
//            runWithAllTimesControls();
//            break;
//        default:
//            throw new IllegalStateException(
//                    ExceptionMessage
//                        .ILLEGAL_STATE_THREAD_TIMER_CONFIG
//                        .format(threadTimeConfig));
//        }
//    }
//
//    private boolean noSchedule() {
//        return !delay.isPresent() && !timeout.isPresent() && !interval.isPresent();
//    }
//
//    private boolean onlyDelay() {
//        return delay.isPresent() && !timeout.isPresent() && !interval.isPresent();
//    }
//
//    private boolean onlyTimeout() {
//        return !delay.isPresent() && timeout.isPresent() && !interval.isPresent();
//    }
//
//    private boolean onlyInterval() {
//        return !delay.isPresent() && !timeout.isPresent() && interval.isPresent();
//    }
//
//    private boolean delayAndTimeout() {
//        return delay.isPresent() && timeout.isPresent() && !interval.isPresent();
//    }
//
//    private boolean delayAndInterval() {
//        return delay.isPresent() && !timeout.isPresent() && interval.isPresent();
//    }
//    
//    private boolean isNotSilentOrIsExecutionException(final Exception e) {
//        return !silentInterruption || !(e instanceof CancellationException) && !(e instanceof InterruptedException);
//    }
//    
//    private void runWithNoSchedule() {
//        final Future<S> future = createScheduledFuture();
//
//        executor.addAfterExecuteConsumer(handleException(future));
//        newExecutorResultIfNull();
//        executorResult.getFutures().add(future);
//    }
//
//    private void runWithDelay() {
//        final ScheduledFuture<S> future = createScheduledFuture();
//
//        executor.addAfterExecuteConsumer(handleException(future));
//        newExecutorResultIfNull();
//        executorResult.getFutures().add(future);
//    }
//
//    private void runWithDelayAndTimeout() {
//        final ScheduledFuture<S> future = createScheduledFuture();
//
//        executor.addAfterExecuteConsumer(handleException(future));
//
//        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
//        
//        newExecutorResultIfNull();
//        executorResult.getFutures().add(future);
//        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
//    }
//
//    private void runWithDelayAndInterval() {
//        final ScheduledFuture<S> future = createScheduledFuture();
//
//        executor.addAfterExecuteConsumer(handleException(future));
//        newExecutorResultIfNull();
//        executorResult.getFutures().add(future);
//    }
//    
//    private void newExecutorResultIfNull() {
//        executorResult = executorResult == null ? new ExecutorResult(executor) : executorResult;
//    }
//
//    private void runWithAllTimesControls() {
//        final ScheduledFuture<S> future = createScheduledFuture();
//        
//        executor.addAfterExecuteConsumer(handleException(future));
//
//        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
//        
//        newExecutorResultIfNull();
//        executorResult.getFutures().add(future);
//        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
//    }
//    
//    private BiConsumer<Runnable, Throwable> handleException(final Future<S> future) {
//        return (runnable, throwable) -> {
//            if (throwable == null) {
//                try {
//                    if (future.isDone()) future.get();
//                } catch (final Exception e) {
//                    if (isNotSilentOrIsExecutionException(e)) {
//                        uncaughtExceptionConsumer.orElseGet(() -> System.out::println).accept(e);
//                    }
//                }
//            }
//        };
//    }
//
//    private ExecutorResult handleInterruption(final ScheduledFuture<S> future) {
//        final ScheduledCaughtExecutorService localExecutor = new ScheduledCaughtExecutorService(1);
//        
//        localExecutor.addAfterExecuteConsumer(handleException(future));
//        localExecutor.schedule(
//                cancelFuture(future), 
//                timeout.orElse(Duration.ofMillis(0)).toMillis(), 
//                TimeUnit.MILLISECONDS);
//        
//        final ExecutorResult timeoutExecutorResult = new ExecutorResult(localExecutor);
//        
//        timeoutExecutorResult.getFutures().add(future);
//        
//        return timeoutExecutorResult;
//    }
//
//    private Runnable cancelFuture(final ScheduledFuture<S> future) {
//        return () -> {if(!future.isDone() && !future.isCancelled()) future.cancel(mayInterruptIfRunning);};
//    }
//    
//    protected long createDefaultDelay() {
//        final long localDelay = delay.orElse(Duration.ofMillis(0)).toMillis();
//        
//        if (uncaughtExceptionConsumer.isPresent() || afterExecuteConsumer.isPresent()) {
//            return localDelay >= RunnableBuilder.MINIMAL_REQUIRED_DELAY 
//                    ? localDelay : localDelay + RunnableBuilder.MINIMAL_REQUIRED_DELAY;
//        } else {
//            return localDelay;
//        }
//    }
//    
//    protected abstract ScheduledFuture<S> createScheduledFuture();
//}
