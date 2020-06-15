package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.ResourceConsumer;

import java.io.Closeable;

import static br.com.armange.commons.thread.Constants.THIS_EXCEPTION_WAS_EXPECTED;

public class CloseableConsumerWithException implements ResourceConsumer {
    @Override
    public void accept(final Closeable closeable) {
        throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
    }
}
