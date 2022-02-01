package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.ResourceConsumer;

import java.io.Closeable;

public class CloseableConsumer implements ResourceConsumer {
    @Override
    public void accept(final Closeable closeable) {
        System.out.println("Closeable consumer fired successfully.");
    }
}
