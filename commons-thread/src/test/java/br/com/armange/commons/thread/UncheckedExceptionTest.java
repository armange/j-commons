package br.com.armange.commons.thread;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UncheckedExceptionTest {

    @Test
    public void shouldConstructWithCause() {
        final NullPointerException nullPointerException = new NullPointerException();
        final UncheckedException uncheckedException = new UncheckedException(nullPointerException);

        assertEquals(nullPointerException, uncheckedException.getCause());
    }
}
