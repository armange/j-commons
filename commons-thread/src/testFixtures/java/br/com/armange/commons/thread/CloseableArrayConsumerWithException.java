package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.ResourcesConsumer;

import java.io.Closeable;

import static br.com.armange.commons.thread.Constants.THIS_EXCEPTION_WAS_EXPECTED;

public class CloseableArrayConsumerWithException implements ResourcesConsumer {
    @Override
    public void accept(final Closeable[] closeableArray) {
        throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
    }
}
