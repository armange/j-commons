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

import java.lang.reflect.Field;

import br.com.armange.commons.reflection.exception.ReflectionException;

public class FieldSupport {
    private final Field field;
    
    private FieldSupport(final Field field) {
        this.field = field;
    }
    
    public static FieldSupport from(final Field field) {
        return new FieldSupport(field);
    }
    
    public Object getValue(final Object declaringInstance) {
        try {
            field.setAccessible(true);
            
            return field.get(declaringInstance);
        } catch (final IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }
    
    public void setValue(final Object declaringInstance, final Object value) {
        try {
            field.setAccessible(true);
            
            field.set(declaringInstance, value);
        } catch (final IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }
}
