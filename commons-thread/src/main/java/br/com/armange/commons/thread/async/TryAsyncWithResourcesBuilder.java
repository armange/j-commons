package br.com.armange.commons.thread.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TryAsyncWithResourcesBuilder extends AbstractTryAsyncBuilder<TryAsyncWithResourceBuilder> {
    private final Closeable[] closeables;
    private final Consumer<Closeable[]> attemptedExecution;

    private TryAsyncWithResourcesBuilder(final Consumer<Closeable[]> attemptedExecution, final Closeable... closeables) {
        this.closeables = closeables;
        this.attemptedExecution = attemptedExecution;
        
        addFinalizer(() -> {
            Stream.of(closeables).forEach(closeable -> {
                try {
                    closeable.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
    
    @Override
    public void execute() {
        execute(() -> attemptedExecution.accept(closeables));
    }

}
