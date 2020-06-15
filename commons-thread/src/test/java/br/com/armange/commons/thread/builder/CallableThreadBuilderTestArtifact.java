package br.com.armange.commons.thread.builder;

import java.util.concurrent.Callable;

@SuppressWarnings("rawtypes")
public class CallableThreadBuilderTestArtifact<S>
        extends AbstractThreadBuilder<S, Callable, CallableThreadBuilderTestArtifact<S>> {

    CallableThreadBuilderTestArtifact(final int corePoolSize) {
        super(corePoolSize, false);
    }

    @Override
    Class<Callable> getExecutionClass() {
        return Callable.class;
    }

    static <T> CallableThreadBuilderTestArtifact<T> newBuilder() {
        return new CallableThreadBuilderTestArtifact<>(1);
    }
}
