package br.com.armange.commons.thread;

import br.com.armange.commons.thread.async.RunnableWithException;

import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static java.lang.System.out;

public class RunnableWithDelay implements RunnableWithException {
    private final long delay;

    public RunnableWithDelay(final long delay) {
        this.delay = delay;
    }

    @Override
    public void run() {
        sleepUnchecked(delay);

        out.println("RunnableWithDeay class was executed successfully");
    }

}
