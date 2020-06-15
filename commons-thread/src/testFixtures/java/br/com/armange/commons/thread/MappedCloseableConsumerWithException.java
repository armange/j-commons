package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.ResourcesMapConsumer;

import java.io.Closeable;
import java.util.Map;

import static br.com.armange.commons.thread.Constants.THIS_EXCEPTION_WAS_EXPECTED;

public class MappedCloseableConsumerWithException implements ResourcesMapConsumer {
    @Override
    public void accept(final Map<Object, Closeable> closeableMap) {
        throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
    }
}
