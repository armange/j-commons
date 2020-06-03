package br.com.armange.commons.thread.builder;

import org.junit.Test;

import br.com.armange.commons.thread.builder.AbstractThreadBuilder.ExecutionType;

public class ExecutionTypeTest {
    private static final String EMPTY = "";

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForInvalidEnum() {
        ExecutionType.valueOf(EMPTY);
    }
}
