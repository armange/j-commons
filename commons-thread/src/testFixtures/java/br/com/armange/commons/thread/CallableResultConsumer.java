package br.com.armange.commons.thread;

import java.util.function.Consumer;

public class CallableResultConsumer implements Consumer<String> {
    public String result;

    public CallableResultConsumer(final String result) {
        this.result = result;
    }

    @Override
    public void accept(final String value) {
        System.out.println(value);

        result = value;
    }
}
