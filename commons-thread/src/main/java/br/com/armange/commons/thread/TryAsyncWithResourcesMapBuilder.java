package br.com.armange.commons.thread;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class TryAsyncWithResourcesMapBuilder extends AbstractTryAsyncBuilder<TryAsyncWithResourceBuilder> {
    private final Map<Object, Closeable> closeableMap;
    private final Consumer<Map<Object, Closeable>> attemptedExecution;

    private TryAsyncWithResourcesMapBuilder(final Map<Object, Closeable> closeableMap, final Consumer<Map<Object, Closeable>> attemptedExecution) {
        this.closeableMap = closeableMap;
        this.attemptedExecution = attemptedExecution;
        
        addFinalizer(() -> {
            closeableMap.values().forEach(closeable -> {
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
        execute(() -> attemptedExecution.accept(closeableMap));
    }

}
