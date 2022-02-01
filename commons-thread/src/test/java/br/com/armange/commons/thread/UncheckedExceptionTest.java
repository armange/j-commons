package br.com.armange.commons.thread;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UncheckedExceptionTest {

    @Test
    public void shouldConstructWithCause() {
        final NullPointerException nullPointerException = new NullPointerException();
        final UncheckedException uncheckedException = new UncheckedException(nullPointerException);

        assertEquals(nullPointerException, uncheckedException.getCause());
    }
}
