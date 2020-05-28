package br.com.armange.commons.thread;

public class ForkJoinTaskBuilder extends ForkJoinBuilder {
    private ForkJoinTaskBuilder(final int corePoolSize) {
        super(corePoolSize);
    }
    
    public static ForkJoinTaskBuilder newForkJoinBuilder(final int corePoolSize) {
        return new ForkJoinTaskBuilder(corePoolSize);
    }
}
