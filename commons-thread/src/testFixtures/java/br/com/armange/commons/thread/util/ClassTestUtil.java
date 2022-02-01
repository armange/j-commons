package br.com.armange.commons.thread.util;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsArrayWithSize;
import org.junit.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClassTestUtil {

    public static void assertPrivateDefaultConstructor(final Class<?> sourceClass)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        final Constructor<?>[] declaredConstructors = sourceClass.getDeclaredConstructors();

        MatcherAssert.assertThat(declaredConstructors,
                IsArrayWithSize.arrayWithSize(Matchers.greaterThanOrEqualTo(1)));

        final Constructor<?> declaredConstructor = declaredConstructors[0];

        try {
            declaredConstructor.newInstance();
            Assert.fail("The default constructor must be private.");
        } catch (final IllegalAccessException e) {
            declaredConstructor.setAccessible(true);
            MatcherAssert.assertThat(declaredConstructor.newInstance(), Matchers.notNullValue());
        }
    }
}
