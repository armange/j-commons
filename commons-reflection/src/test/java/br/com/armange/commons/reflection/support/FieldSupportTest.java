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

import org.junit.Assert;
import org.junit.Test;

public class FieldSupportTest {
    @Test
    public void readsAndWritesPrivateFieldOfApplicationClass() throws NoSuchFieldException {
        final FieldHolder holder = new FieldHolder();
        final FieldSupport support = FieldSupport.from(FieldHolder.class.getDeclaredField("value"));

        Assert.assertEquals("before", support.getValue(holder));

        support.setValue(holder, "after");

        Assert.assertEquals("after", support.getValue(holder));
    }

    @Test(expected = InaccessibleObjectException.class)
    public void rejectsReadingPrivateFieldOfJdkClass() throws NoSuchFieldException {
        FieldSupport.from(String.class.getDeclaredField("value")).getValue("text");
    }

    @Test(expected = InaccessibleObjectException.class)
    public void rejectsWritingPrivateFieldOfJdkClass() throws NoSuchFieldException {
        FieldSupport.from(String.class.getDeclaredField("value")).setValue("text", new byte[0]);
    }

    private static class FieldHolder {
        private String value = "before";
    }
}
