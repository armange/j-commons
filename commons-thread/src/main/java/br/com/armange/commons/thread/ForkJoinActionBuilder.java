package br.com.armange.commons.thread;

public class ForkJoinActionBuilder extends ForkJoinBuilder {
    private ForkJoinActionBuilder(final int corePoolSize) {
        super(corePoolSize);
    }
    
    public static ForkJoinActionBuilder newForkJoinBuilder(final int corePoolSize) {
        return new ForkJoinActionBuilder(corePoolSize);
    }
}
