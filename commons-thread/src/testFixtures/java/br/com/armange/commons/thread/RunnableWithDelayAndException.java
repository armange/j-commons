package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.RunnableWithException;

import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;

public class RunnableWithDelayAndException implements RunnableWithException {
    private final long delay;

    public RunnableWithDelayAndException(final long delay) {
        this.delay = delay;
    }

    @Override
    public void run() {
        System.out.println("An exception will be thrown!");

        sleepUnchecked(delay);

        throw new RuntimeException("This is a expected esception.");
    }
}
