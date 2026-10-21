/*
 * Copyright [2026] [Diego Armange Costa]
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
package br.com.armange.commons.thread.builder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/** Guards single and multiple task aliases sharing one common builder. */
final class TaskStageState<S> {
    private final AbstractThreadBuilder<S, ?, ?> builder;
    private boolean multipleActive;
    private boolean keepOpen;
    private boolean callerOwnsExecutor;
    private boolean terminal;
    private long batchId;
    private int batchStart;

    TaskStageState(final AbstractThreadBuilder<S, ?, ?> builder) {
        this.builder = builder;
    }

    synchronized void configureSingle(final Runnable mutation) {
        requireSingle();
        mutation.run();
    }

    synchronized void configureMultiple(final long expectedBatchId, final Runnable mutation) {
        requireMultiple(expectedBatchId);
        mutation.run();
    }

    synchronized void keepExecutorOpenSingle() {
        requireSingle();
        keepOpen = true;
    }

    synchronized void keepExecutorOpenMultiple(final long expectedBatchId) {
        requireMultiple(expectedBatchId);
        keepOpen = true;
    }

    synchronized SingleTaskExecutorResult<S> startSingle() {
        requireSingle();
        try {
            final ExecutorResult<S> result = builder.start();
            final List<Future<S>> futures = result.getFutures();
            final SingleTaskExecutorResult<S> snapshot = new SingleTaskResultSnapshot<>(
                    futures.get(futures.size() - 1), result.getExecutorService());
            completeFinalStart(result.getExecutorService());

            return snapshot;
        } catch (final RuntimeException | Error failure) {
            failFinalStart(failure);

            throw failure;
        }
    }

    synchronized long beginMultiple() {
        requireSingle();
        try {
            final int nextBatchStart = currentFuturesCount();
            builder.startAndBuildOther();
            batchStart = nextBatchStart;
            multipleActive = true;

            return ++batchId;
        } catch (final RuntimeException | Error failure) {
            failFinalStart(failure);

            throw failure;
        }
    }

    synchronized void submitMultiple(final long expectedBatchId) {
        requireMultiple(expectedBatchId);
        try {
            builder.startAndBuildOther();
        } catch (final RuntimeException | Error failure) {
            failFinalStart(failure);

            throw failure;
        }
    }

    synchronized MultipleTaskExecutorResult<S> finishMultiple(final long expectedBatchId) {
        requireMultiple(expectedBatchId);
        try {
            final ExecutorResult<S> result = builder.start();
            final List<Future<S>> futures = result.getFutures();
            final List<Future<S>> batchFutures = List.copyOf(
                    futures.subList(batchStart, futures.size()));
            final MultipleTaskExecutorResult<S> snapshot = new MultipleTaskResultSnapshot<>(
                    batchFutures, result.getExecutorService());
            completeFinalStart(result.getExecutorService());

            return snapshot;
        } catch (final RuntimeException | Error failure) {
            failFinalStart(failure);

            throw failure;
        }
    }

    private void completeFinalStart(final ExecutorService executor) {
        multipleActive = false;

        if (keepOpen) {
            callerOwnsExecutor = true;

            return;
        }

        terminal = true;
        executor.shutdown();
    }

    private void failFinalStart(final Throwable failure) {
        multipleActive = false;
        terminal = true;

        if (!callerOwnsExecutor && builder.executor != null) {
            try {
                builder.executor.shutdown();
            } catch (final RuntimeException | Error shutdownFailure) {
                failure.addSuppressed(shutdownFailure);
            }
        }
    }

    private int currentFuturesCount() {
        if (builder.executorResult == null) {
            return 0;
        }

        return builder.executorResult.getFutures()
                .size();
    }

    private void requireSingle() {
        requireOpen();

        if (multipleActive) {
            throw new IllegalStateException("Single-task alias is unavailable during a multiple-task batch");
        }
    }

    private void requireMultiple(final long expectedBatchId) {
        requireOpen();

        if (!multipleActive || batchId != expectedBatchId) {
            throw new IllegalStateException("Multiple-task stage is no longer active");
        }
    }

    private void requireOpen() {
        if (terminal) {
            throw new IllegalStateException("Task builder is no longer active");
        }
    }
}
