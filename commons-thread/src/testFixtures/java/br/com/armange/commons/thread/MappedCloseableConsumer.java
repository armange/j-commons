package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.ResourcesMapConsumer;

import java.io.Closeable;
import java.util.Map;

public class MappedCloseableConsumer implements ResourcesMapConsumer {
    @Override
    public void accept(final Map<Object, Closeable> closeableMap) {
        System.out.println("Mapped closeable consumer fired successfully.");
    }
}
