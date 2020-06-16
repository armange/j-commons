package br.com.armange.commons.thread.builder;

import java.util.concurrent.Callable;

@SuppressWarnings("rawtypes")
public class TimingCallableThreadBuilderTestArtifact<S>
        extends AbstractTimingThreadBuilder<S, Callable, TimingCallableThreadBuilderTestArtifact<S>> {

    TimingCallableThreadBuilderTestArtifact(final int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    Class<Callable> getExecutionClass() {
        return Callable.class;
    }

    static <T> TimingCallableThreadBuilderTestArtifact<T> newBuilder() {
        return new TimingCallableThreadBuilderTestArtifact<>(1);
    }
}
