package br.com.armange.commons.thread.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public class TryAsyncWithResourceBuilder extends AbstractTryAsyncBuilder<TryAsyncWithResourceBuilder> {
    private final Closeable closeable;
    private final Consumer<Closeable> attemptedExecution;

    private TryAsyncWithResourceBuilder(final Closeable closeable, final Consumer<Closeable> attemptedExecution) {
        this.closeable = closeable;
        this.attemptedExecution = attemptedExecution;
        
        addFinalizer(() -> {
            try {
                closeable.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public void execute() {
        execute(() -> attemptedExecution.accept(closeable));
    }

}
