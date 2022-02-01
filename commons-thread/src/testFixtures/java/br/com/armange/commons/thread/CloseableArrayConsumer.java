package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.ResourcesConsumer;

import java.io.Closeable;

public class CloseableArrayConsumer implements ResourcesConsumer {
    @Override
    public void accept(final Closeable[] closeableArray) {
        System.out.println("Closeable array consumer fired successfully.");
    }
}
