package br.com.armange.commons.thread;

import java.util.function.Consumer;

import static br.com.armange.commons.thread.Constants.THIS_EXCEPTION_WAS_EXPECTED;
import static java.lang.System.out;

public class ExceptionConsumer implements Consumer<Throwable> {
    @Override
    public void accept(final Throwable throwable) {
        out.println(throwable.getClass().getName() + THIS_EXCEPTION_WAS_EXPECTED);
    }
}
