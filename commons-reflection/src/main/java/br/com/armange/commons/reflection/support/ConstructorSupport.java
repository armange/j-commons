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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import br.com.armange.commons.reflection.exception.ReflectionException;

public class ConstructorSupport<T> {
    private final Constructor<T> constructor;
    
    private ConstructorSupport(final Constructor<T> constructor) {
        this.constructor = constructor;
    }
    
    public static <T> ConstructorSupport<T> from(final Constructor<T> constructor) {
        return new ConstructorSupport<T>(constructor);
    }
    
    public T newInstance() {
        try {
            return constructor.newInstance(new Object[] {});
        } catch (final InstantiationException 
                | IllegalAccessException 
                | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }
}
