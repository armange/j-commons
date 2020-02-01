package br.com.armange.commons.reflection.stream;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class ReflectionStreamSupportTest {

    @Test
    public void throwExceptionIfConstruct() {
        final Constructor<?>[] declaredConstructors = ReflectionStreamSupport.class.getDeclaredConstructors();
        
        final Constructor<?> constructor = declaredConstructors[0];
        
        constructor.setAccessible(true);
        
        try {
            constructor.newInstance(new Object[] {});
            
            fail("Cannot be constructed.");
        } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            if (e.getCause() instanceof java.lang.IllegalStateException) {
                if (!e.getCause().getMessage().equals("Utility class")) {
                    fail("Unknown error message.");
                }
            } else {
                fail("Unknown error.");
            }
        }
    }
}
