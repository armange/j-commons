package br.com.armange.commons.thread.message;

import br.com.armange.commons.message.MessageFormat;

public enum ExceptionMessage implements MessageFormat {
    ILLEGAL_STATE_THREAD_TIMER_CONFIG("Illegal timing-configuration state '{0}'."),
    ILLEGAL_ARGUMENT_THREAD_EXECUTOR_TYPE("Illegal executor-type '{0}'."),
    ;
    
    private String message;
    
    ExceptionMessage(final String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
