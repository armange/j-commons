package br.com.armange.commons.thread;

abstract class ForkJoinBuilder {
    protected final int corePoolSize;
    
    protected ForkJoinBuilder(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
}
