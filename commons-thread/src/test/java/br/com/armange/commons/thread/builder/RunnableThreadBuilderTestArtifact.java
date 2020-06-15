package br.com.armange.commons.thread.builder;

public class RunnableThreadBuilderTestArtifact<S>
        extends AbstractThreadBuilder<S, Runnable, RunnableThreadBuilderTestArtifact<S>> {

    RunnableThreadBuilderTestArtifact(final int corePoolSize) {
        super(corePoolSize, false);
    }

    @Override
    Class<Runnable> getExecutionClass() {
        return Runnable.class;
    }

    static <T> RunnableThreadBuilderTestArtifact<T> newBuilder() {
        return new RunnableThreadBuilderTestArtifact<>(1);
    }
}
