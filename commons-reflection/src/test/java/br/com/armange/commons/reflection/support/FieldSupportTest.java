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
package br.com.armange.commons.reflection.support;

import java.lang.reflect.InaccessibleObjectException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FieldSupportTest {
    @Test
    public void readsAndWritesPrivateFieldOfApplicationClass() throws NoSuchFieldException {
        final FieldHolder holder = new FieldHolder();
        final FieldSupport support = FieldSupport.from(FieldHolder.class.getDeclaredField("value"));

        Assertions.assertEquals("before", support.getValue(holder));

        support.setValue(holder, "after");

        Assertions.assertEquals("after", support.getValue(holder));
    }

    @Test
    public void rejectsReadingPrivateFieldOfJdkClass() throws NoSuchFieldException {
        Assertions.assertThrows(InaccessibleObjectException.class,
                () -> FieldSupport.from(String.class.getDeclaredField("value")).getValue("text"));
    }

    @Test
    public void rejectsWritingPrivateFieldOfJdkClass() throws NoSuchFieldException {
        Assertions.assertThrows(InaccessibleObjectException.class,
                () -> FieldSupport.from(String.class.getDeclaredField("value")).setValue("text", new byte[0]));
    }

    private static class FieldHolder {
        private String value = "before";
    }
}
