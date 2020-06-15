package br.com.armange.commons.thread;

import java.util.concurrent.Callable;

import static br.com.armange.commons.thread.Constants.THIS_EXCEPTION_WAS_EXPECTED;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;

public class CallableWithDelayAndException implements Callable<String> {
    public final long delay;

    public CallableWithDelayAndException(final long delay) {
        this.delay = delay;
    }

    @Override
    public String call() {
        sleepUnchecked(delay);

        throw new RuntimeException(THIS_EXCEPTION_WAS_EXPECTED);
    }
}
