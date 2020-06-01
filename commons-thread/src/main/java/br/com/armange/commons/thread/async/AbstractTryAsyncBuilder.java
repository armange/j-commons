package br.com.armange.commons.thread.async;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import br.com.armange.commons.thread.builder.AbstractThreadBuilder;
import br.com.armange.commons.thread.builder.ThreadBuilder;

public abstract class AbstractTryAsyncBuilder<T extends AbstractTryAsyncBuilder<T>> {
    protected final Map<Class<Throwable>, Consumer<Throwable>> exceptionConsumers = new HashMap<>();
    protected final List<Runnable> finalizingExecutables = new LinkedList<>();
    
    @SuppressWarnings("unchecked")
    public T addCatcher(
            final Class<? extends Throwable> exceptionClass, 
            final Consumer<Throwable> exceptionConsumer) {
        exceptionConsumers.put((Class<Throwable>)exceptionClass, exceptionConsumer);
        
        return (T) this;
    }
    
    public T finallyAsync(final Runnable runnable) {
        return addFinalizer(runnable);
    }
    
    @SuppressWarnings("unchecked")
    protected T addFinalizer(final Runnable runnable) {
        finalizingExecutables.add(runnable);
        
        return (T) this;
    }
    
    protected void execute(final Runnable attemptedExecution) {
        ThreadBuilder
            .newBuilder()
            .setExecution(attemptedExecution)
            .setUncaughtExceptionConsumer(consumeExceptionOrThrowRuntimeException())
            .setAfterExecuteConsumer((runnable, throwable) -> finalizingExecutables.forEach(Runnable::run))
            .start();
    }
    
    protected Consumer<Throwable> consumeExceptionOrThrowRuntimeException() {
        return throwable -> {
            exceptionConsumers
                .keySet()
                .stream()
                .filter(isMatchingException(throwable))
                .findFirst()
                .ifPresent(catchException(throwable));
        };
    }

    private Predicate<? super Class<Throwable>> isMatchingException(final Throwable throwable) {
        return expectedException -> {
            return throwable.getClass().equals(expectedException)
                    || throwable.getCause().getClass().equals(expectedException)
                    && throwable.getClass().equals(ExecutionException.class);
        };
    }
    
    private Consumer<? super Class<Throwable>> catchException(final Throwable throwable) {
        return exceptionClass -> {
            exceptionConsumers
                .get(exceptionClass)
                .accept(throwable);
        };
    }
    
    public abstract void execute();
}
