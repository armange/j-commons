package br.com.armange.commons.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonMessagesTest {

    @Test
    public void requiredParameterHasMessage() {
        Assertions.assertEquals("The \"{0}\" parameter is required.", CommonMessages.REQUIRED_PARAMETER.getMessage());
    }
}
