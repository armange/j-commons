package br.com.armange.commons.thread.builder;

public class TimingRunnableThreadBuilderTestArtifact<S>
        extends AbstractTimingThreadBuilder<S, Runnable, TimingRunnableThreadBuilderTestArtifact<S>> {

    TimingRunnableThreadBuilderTestArtifact(final int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    Class<Runnable> getExecutionClass() {
        return Runnable.class;
    }

    static <T> TimingRunnableThreadBuilderTestArtifact<T> newBuilder() {
        return new TimingRunnableThreadBuilderTestArtifact<>(1);
    }
}
