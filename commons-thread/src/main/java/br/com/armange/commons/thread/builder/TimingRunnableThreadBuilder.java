package br.com.armange.commons.thread.builder;

public class TimingRunnableThreadBuilder<S>
        extends AbstractTimingThreadBuilder<S, Runnable, TimingRunnableThreadBuilder<S>> {

    TimingRunnableThreadBuilder() {}
    
    TimingRunnableThreadBuilder(final int corePoolSize) {
        super(corePoolSize);
    }
    
    @Override
    Class<Runnable> getExceutionClass() {
        return Runnable.class;
    }

}
