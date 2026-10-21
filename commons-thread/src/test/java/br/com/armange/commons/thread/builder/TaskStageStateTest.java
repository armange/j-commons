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

import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskStageStateTest {

    @Test
    void failedFirstIntermediateSubmissionShutsDownUndeliveredExecutor() throws Exception {
        final FailingBuilder builder = new FailingBuilder(1);
        final TaskStageState<Void> state = new TaskStageState<>(builder);

        assertSame(builder.rejection, assertThrows(RejectedExecutionException.class,
                state::beginMultiple));
        assertTrue(builder.executor.isShutdown());
        assertThrows(IllegalStateException.class, state::startSingle);
        assertTrue(builder.executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void failedIntermediateSubmissionShutsDownBeforeFirstResult() throws Exception {
        final FailingBuilder builder = new FailingBuilder(2);
        final TaskStageState<Void> state = new TaskStageState<>(builder);

        state.keepExecutorOpenSingle();
        final long batchId = state.beginMultiple();

        assertSame(builder.rejection, assertThrows(RejectedExecutionException.class,
                () -> state.submitMultiple(batchId)));
        assertTrue(builder.executor.isShutdown());
        assertThrows(IllegalStateException.class, () -> state.configureSingle(() -> {
        }));
        assertThrows(IllegalStateException.class, () -> state.submitMultiple(batchId));
        assertTrue(builder.executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void failureAfterIntermediateSubmissionShutsDownBeforeFirstResultEvenWithKeepOpen() throws Exception {
        final FailingBuilder builder = new FailingBuilder(2);
        final TaskStageState<Void> state = new TaskStageState<>(builder);

        state.keepExecutorOpenSingle();
        final long batchId = state.beginMultiple();

        assertSame(builder.rejection, assertThrows(RejectedExecutionException.class,
                () -> state.finishMultiple(batchId)));
        assertTrue(builder.executor.isShutdown());
        assertThrows(IllegalStateException.class, () -> state.configureSingle(() -> {
        }));
        assertThrows(IllegalStateException.class, () -> state.submitMultiple(batchId));
        assertTrue(builder.executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void failureAfterOwnershipTransferNeverShutsDownCallerExecutor() throws Exception {
        final FailingBuilder builder = new FailingBuilder(2);
        final TaskStageState<Void> state = new TaskStageState<>(builder);

        state.keepExecutorOpenSingle();
        final SingleTaskExecutorResult<Void> first = state.startSingle();

        try {
            assertFalse(first.getExecutorService()
                    .isShutdown());
            assertSame(builder.rejection, assertThrows(RejectedExecutionException.class,
                    state::startSingle));
            assertFalse(first.getExecutorService()
                    .isShutdown());
            assertThrows(IllegalStateException.class, state::startSingle);
            first.getFuture()
                    .get(5, TimeUnit.SECONDS);
        } finally {
            first.getExecutorService()
                    .shutdown();
        }

        assertTrue(first.getExecutorService()
                .awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void failedIntermediateSubmissionAfterOwnershipTransferLeavesExecutorWithCaller() throws Exception {
        final FailingBuilder builder = new FailingBuilder(3);
        final TaskStageState<Void> state = new TaskStageState<>(builder);

        state.keepExecutorOpenSingle();
        final SingleTaskExecutorResult<Void> first = state.startSingle();

        try {
            final long batchId = state.beginMultiple();

            assertSame(builder.rejection, assertThrows(RejectedExecutionException.class,
                    () -> state.submitMultiple(batchId)));
            assertFalse(first.getExecutorService()
                    .isShutdown());
            assertThrows(IllegalStateException.class, state::startSingle);
            first.getFuture()
                    .get(5, TimeUnit.SECONDS);
        } finally {
            first.getExecutorService()
                    .shutdown();
        }

        assertTrue(first.getExecutorService()
                .awaitTermination(5, TimeUnit.SECONDS));
    }

    private static final class FailingBuilder extends AbstractThreadBuilder<Void, Runnable, FailingBuilder> {
        private final int failOnSubmission;
        private final RejectedExecutionException rejection = new RejectedExecutionException("planned rejection");
        private int submissions;

        private FailingBuilder(final int failOnSubmission) {
            super(1);
            this.failOnSubmission = failOnSubmission;
            setExecution(() -> {
            });
        }

        @Override
        Class<Runnable> getExecutionClass() {
            return Runnable.class;
        }

        @Override
        protected Future<Void> submit() {
            submissions++;

            if (submissions == failOnSubmission) {
                throw rejection;
            }

            return super.submit();
        }
    }
}
