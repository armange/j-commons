package br.com.armange.commons.thread.builder;

public class ThreadBuilder {

    private final int corePoolSize;
    
    private ThreadBuilder() {
        corePoolSize = 1;
    }
    
    private ThreadBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    
    public static ThreadBuilder newBuilder(final int corePoolSize) {
        return new ThreadBuilder(corePoolSize);
    }
    
    public static ThreadBuilder newBuilder() {
        return new ThreadBuilder();
    }
    
    public TimingRunnableThreadBuilder<?> setExecution(final Runnable execution) {
        final TimingRunnableThreadBuilder<?> builder = new TimingRunnableThreadBuilder<>(corePoolSize);
        
        return builder.setExecution(execution);
    }
}
