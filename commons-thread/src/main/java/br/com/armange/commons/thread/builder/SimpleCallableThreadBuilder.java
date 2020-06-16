package br.com.armange.commons.thread.builder;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class SimpleCallableThreadBuilder<S> extends AbstractThreadBuilder<S, Callable, SimpleCallableThreadBuilder<S>>{

    protected SimpleCallableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }

    protected static <T> SimpleCallableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new SimpleCallableThreadBuilder<>(corePoolSize);
    }

    @Override
    public SimpleCallableThreadBuilder<S> setThreadResultConsumer(final Consumer<S> threadResultConsumer) {
        return super.setThreadResultConsumer(threadResultConsumer);
    }

    @Override
    Class<Callable> getExecutionClass() {
        return Callable.class;
    }

    @Override
    public SimpleCallableThreadBuilder<S> setExecution(final Callable execution) {
        return super.setExecution(execution);
    }
}
