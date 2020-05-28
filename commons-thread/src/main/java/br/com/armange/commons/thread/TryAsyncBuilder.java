package br.com.armange.commons.thread;

public class TryAsyncBuilder extends AbstractTryAsyncBuilder<TryAsyncBuilder>{
    private final Runnable attemptedExecution;
    
    private TryAsyncBuilder(final Runnable runnable) {
        attemptedExecution = runnable;
    }
    
    public static TryAsyncBuilder tryAsync(final Runnable runnable) {
        return new TryAsyncBuilder(runnable);
    }
    
    @Override
    public void execute() {
        execute(attemptedExecution);
    }
}
