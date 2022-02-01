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

import br.com.armange.commons.reflection.exception.ReflectionException;

public class ClassSupport<T> {

    private final Class<T> sourceClass;
    
    private ClassSupport(final Class<T> sourceClass) {
        this.sourceClass = sourceClass;
    }
    
    public static <T> ClassSupport<T> from(final Class<T> sourceClass) {
        return new ClassSupport<T>(sourceClass);
    }
    
    public T newInstance() {
        try {
            return sourceClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }
}
