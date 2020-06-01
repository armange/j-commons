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
//import java.util.concurrent.Future;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//
///**
// * Useful structure for thread creation in the following scenarios:
// * <ul>
// * <li><em><b>Timeout</b></em><br>
// * <p>The thread will be active only until the timeout be fired.</p></li>
// * <li><em><b>Delay</b></em><br>
// * <p>The thread will be active only after the time delay be completed.</p></li>
// * <li><em><b>Interval</b></em><br>
// * <p>The thread will be repeated after the time interval be completed.</p></li>
// * <li><em><b>Exception handling</b></em><br>
// * <p>Handles of uncaught exceptions can be thrown and handled within threads.</p></li>
// * </ul>
// * <b>Note:</b><br>
// * <em>The thread will wait a minimum delay
// * ({@link br.com.armange.commons.thread.AbstractThreadBuilder#MINIMAL_REQUIRED_DELAY}) if and only if a
// * ({@link br.com.armange.commons.thread.AbstractThreadBuilder#setAfterExecuteConsumer(BiConsumer)}) 
// * or a ({@link br.com.armange.commons.thread.AbstractThreadBuilder#setUncaughtExceptionConsumer(Consumer)}) 
// * is present.</em>
// * 
// * <pre>
// * <b>Example:</b>
// * 
// * final ExecutorService thread = ThreadBuilder
// *          .newBuilder() //New object to build a new thread.
// *          .setDelay(1000) //The thread will wait one second before start.
// *          .setTimeout(4000) //The thread will be canceled after four seconds.
// *          .setInterval(1000) //The thread will be repeated every second. 
// *          .setAfterExecuteConsumer(afterExecuteConsumer) //A consumer will be called after thread execution.
// *          .setUncaughtExceptionConsumer(throwableConsumer) //A consumer will be called after any exception thrown.
// *          .setMayInterruptIfRunning(true) //The thread interruption/cancellation will not wait execution.
// *          .setSilentInterruption(true) //Interruption and Cancellation exceptions will not be thrown.
// *          .setExecution(anyRunnable) //The thread execution.
// *          .setThreadNameSupplier(() -&gt; "Thread name")
// *          .setThreadPrioritySupplier(() -&gt; 4)
// *          .start();
// * </pre>
// * 
// * @author Diego Armange Costa
// * @since 2019-11-26 V1.0.0 (JDK 1.8)
// * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService
// */
//public class RunnableBuilder extends AbstractThreadBuilder<Void, Runnable, RunnableBuilder> {
//    
//    public RunnableBuilder() {}
//    
//    public RunnableBuilder(final int corePoolSize) {
//        super(corePoolSize);
//    }
//
//    public static ForkJoinBuilder forkingRecursiveTask(final int corePoolSize) {
//        return ForkJoinTaskBuilder.newForkJoinBuilder(corePoolSize);
//    }
//    
//    public static ForkJoinBuilder forkingRecursiveAction(final int corePoolSize) {
//        return ForkJoinActionBuilder.newForkJoinBuilder(corePoolSize);
//    }
//
//    /**
//     * @return a new object to perform a thread creation.
//     */
//    public static RunnableBuilder newBuilder() {
//        return new RunnableBuilder();
//    }
//    
//    /**
//     * @param corePoolSize the {@link ScheduledCaughtExecutorService} pool size.
//     * @return a new object to perform a thread creation.
//     * @see br.com.armange.commons.thread.ScheduledCaughtExecutorService#ScheduledCaughtExecutorService(int)
//     */
//    public static RunnableBuilder newBuilder(final int corePoolSize) {
//        return new RunnableBuilder(corePoolSize);
//    }
//    
//    /**
//     * Starts the thread.
//     * @return the executor's result after starting thread.
//     * @see ExecutorResult
//     */
//    public ExecutorResult start() {
//        createExecutorAndRunThread();
//
//        return executorResult;
//    }
//    
//    /**
//     * Starts the thread.
//     * @return the current thread builder to prepare another thread.
//     */
//    public RunnableBuilder startAndBuildOther() {
//        createExecutorAndRunThread();
//
//        return this;
//    }
//    
////    private void runWithNoSchedule() {
////        final Future<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);
////
////        executor.addAfterExecuteConsumer(handleException(future));
////        newExecutorResultIfNull();
////        executorResult.getFutures().add(future);
////    }
////
////    private void runWithDelay() {
////        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);
////
////        executor.addAfterExecuteConsumer(handleException(future));
////        newExecutorResultIfNull();
////        executorResult.getFutures().add(future);
////    }
////
////    private void runWithDelayAndTimeout() {
////        final ScheduledFuture<?> future = executor.schedule(execution, handleDelay(), TimeUnit.MILLISECONDS);
////
////        executor.addAfterExecuteConsumer(handleException(future));
////
////        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
////        
////        newExecutorResultIfNull();
////        executorResult.getFutures().add(future);
////        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
////    }
////
////    private void runWithDelayAndInterval() {
////        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
////                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);
////
////        executor.addAfterExecuteConsumer(handleException(future));
////        newExecutorResultIfNull();
////        executorResult.getFutures().add(future);
////    }
////    
////    private void newExecutorResultIfNull() {
////        executorResult = executorResult == null ? new ExecutorResult(executor) : executorResult;
////    }
////
////    private void runWithAllTimesControls() {
////        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(execution, handleDelay(),
////                interval.orElse(Duration.ofMillis(0)).toMillis(), TimeUnit.MILLISECONDS);
////        
////        executor.addAfterExecuteConsumer(handleException(future));
////
////        final ExecutorResult timeoutExecutorResult = handleInterruption(future);
////        
////        newExecutorResultIfNull();
////        executorResult.getFutures().add(future);
////        executorResult.getTimeoutExecutorResults().add(timeoutExecutorResult);
////    }
//
//    @Override
//    protected ScheduledFuture<Void> createScheduledFuture() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//}
