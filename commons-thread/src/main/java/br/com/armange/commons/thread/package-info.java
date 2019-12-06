/**
 * Utility package for thread creation.<br>
 * <ul>
 * <li><b>{@link br.com.armange.commons.thread.ThreadUtil}</b></li>
 * <p>Useful structure for handling the current thread.</p>
 * <pre>
 * ThreadUtil.sleepUnchecked(1000);
 * final URL url = ThreadUtil.getCurrentThreadResource("path");
 * final InputStream is = ThreadUtil.getCurrentThreadResourceAsStream("path");
 * </pre>
 * <li><b>{@link br.com.armange.commons.thread.ThreadBuilder}</b></li>
 * <p>Useful structure for thread creation</p>
 * <pre>
 * final ExecutorService thread = ThreadBuilder
 *          .newBuilder() //New object to build a new thread.
 *          .setTimeout(4000) //The thread will be canceled after four seconds.
 *          .setAfterExecuteConsumer(afterExecuteConsumer) //A consumer will be called after thread execution.
 *          .setUncaughtExceptionConsumer(throwableConsumer) //A consumer will be called after any exception thrown.
 *          .start();
 * </pre>
 * </ul>
 * */
package br.com.armange.commons.thread;