package br.com.armange.commons.message;

import org.junit.Assert;
import org.junit.Test;

public class CommonMessagesTest {

    @Test
    public void requiredParameterHasMessage() {
        Assert.assertEquals("The \"{0}\" parameter is required.", CommonMessages.REQUIRED_PARAMETER.getMessage());
    }
}
