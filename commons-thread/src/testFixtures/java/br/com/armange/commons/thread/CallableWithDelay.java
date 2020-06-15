package br.com.armange.commons.thread;

import java.util.concurrent.Callable;

import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;

public class CallableWithDelay implements Callable<String> {
    private static final String CALLABLE_RESULT = "CallableWithDeay class was executed successfully";
    private final long delay;

    public CallableWithDelay(final long delay) {
        this.delay = delay;
    }

    @Override
    public String call() {
        sleepUnchecked(delay);

        return CALLABLE_RESULT;
    }
}
