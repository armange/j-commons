package br.com.armange.commons.thread.builder;

public class SimpleRunnableThreadBuilder<S> extends AbstractThreadBuilder<S, Runnable, SimpleRunnableThreadBuilder<S>>{

    protected SimpleRunnableThreadBuilder(final int corePoolSize) {
        super(corePoolSize, true);
    }

    protected static <T> SimpleRunnableThreadBuilder<T> newBuilder(final int corePoolSize) {
        return new SimpleRunnableThreadBuilder<>(corePoolSize);
    }

    @Override
    Class<Runnable> getExecutionClass() {
        return Runnable.class;
    }

}
