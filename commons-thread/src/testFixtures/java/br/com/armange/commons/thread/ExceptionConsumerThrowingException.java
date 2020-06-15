package br.com.armange.commons.thread;

import java.util.function.Consumer;

public class ExceptionConsumerThrowingException implements Consumer<Throwable> {
    @Override
    public void accept(final Throwable throwable) {
        throw new RuntimeException(throwable);
    }
}
