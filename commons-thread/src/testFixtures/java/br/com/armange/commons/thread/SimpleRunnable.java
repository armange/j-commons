package br.com.armange.commons.thread;

import static java.lang.System.out;

public class SimpleRunnable implements Runnable {
    @Override
    public void run() {
        out.println("SimpleRunnable class was executed successfully");
    }
}
