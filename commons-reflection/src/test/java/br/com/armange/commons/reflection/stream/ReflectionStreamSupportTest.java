/*
 * Copyright [2019] [Diego Armange Costa]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */
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
