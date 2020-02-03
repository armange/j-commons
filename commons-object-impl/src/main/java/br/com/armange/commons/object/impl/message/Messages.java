package br.com.armange.commons.object.impl.message;

import br.com.armange.commons.message.MessageFormat;

public enum Messages implements MessageFormat {
    DEFAULT_CONSTRUCTOR_NOT_FOUND("The default constructor was not found in the class \"{0}\"");

    private final String message;
    
    Messages(final String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
