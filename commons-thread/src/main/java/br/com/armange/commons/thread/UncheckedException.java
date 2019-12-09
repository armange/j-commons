package br.com.armange.commons.thread;

/**
 * Unchecked exceptions may be thrown instead of the RuntimeException option to allow 
 * differentiation between an unmapped fault and a controlled fault.
 * @author Diego Armange Costa
 * @since 2019-12-08 V1.1.0
 * @see java.lang.RuntimeException
 */
@SuppressWarnings("serial")
public class UncheckedException extends RuntimeException {

    /**
     * The builder will exclusively call SUPER ({@code RuntimeException#RuntimeException(Throwable)})
     * @param cause the cause
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public UncheckedException(final Throwable cause) {
        super(cause);
    }
}
