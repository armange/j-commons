package br.com.armange.commons.thread.builder;

import static org.junit.Assert.fail;

import org.junit.Test;

import br.com.armange.commons.thread.builder.AbstractThreadBuilder.ExecutionType;

public class ExecutionTypeTest {
    @Test()
    public void shouldThrowExceptionForInvalidEnum() {
        try {
            ExecutionType.valueOf(String.class);
            
            fail("IllegalArgumentException was expected");
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
