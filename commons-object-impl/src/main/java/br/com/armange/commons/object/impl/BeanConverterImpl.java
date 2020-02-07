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
package br.com.armange.commons.object.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import br.com.armange.commons.message.CommonMessages;
import br.com.armange.commons.object.annotation.ConvertibleBean;
import br.com.armange.commons.object.api.BeanConverter;
import br.com.armange.commons.object.api.ObjectConverter;
import br.com.armange.commons.object.impl.exception.ObjectConverterException;
import br.com.armange.commons.object.impl.message.Messages;
import br.com.armange.commons.reflection.stream.AnnotationStream;
import br.com.armange.commons.reflection.stream.ConstructorStream;
import br.com.armange.commons.reflection.stream.FieldStream;
import br.com.armange.commons.reflection.support.ConstructorSupport;
import br.com.armange.commons.reflection.support.FieldSupport;

public class BeanConverterImpl<S, T> implements BeanConverter<S, T> {

    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    
    private S sourceObject;
    private List<Field> sourceFields;
    
    @Override
    public ObjectConverter<S, T> from(final S sourceObject) {
        Objects.requireNonNull(sourceObject, CommonMessages.REQUIRED_PARAMETER.format(SOURCE));
        
        this.sourceObject = sourceObject;
        
        sourceFields = FieldStream
                .of(sourceObject.getClass())
                .nested()
                .declared()
                .build()
                .collect(Collectors.toList());
        
        return this;
    }

    @Override
    public T to(final Class<T> targetClass) {
        Objects.requireNonNull(targetClass, CommonMessages.REQUIRED_PARAMETER.format(TARGET));
        
        final T targetObject = ConstructorStream
                .<T>of(targetClass)
                .build()
                .parallel()
                .filter(c -> c.getParameterCount() == 0)
                .findFirst()
                .map(c -> ConstructorSupport.<T>from(c).newInstance())
                .orElseThrow(() -> new ObjectConverterException(Messages.DEFAULT_CONSTRUCTOR_NOT_FOUND, targetClass.getName()));

        final List<Field> targetFields = FieldStream
                .of(targetClass)
                .nested()
                .declared()
                .build()
                .collect(Collectors.toList());
        
        targetFields
            .parallelStream()
            .forEach(targetField -> {
                sourceFields.parallelStream().forEach(sameFieldConsumer(targetObject, targetField));
            });
        
        return targetObject;
    }

    private Consumer<? super Field> sameFieldConsumer(final Object targetObject, final Field targetField) {
        return sourceField -> { 
            if (sourceField.getName().equals(targetField.getName())) {
                copyFieldValue(targetObject, targetField, sourceField);
            }
        };
    }

    private void copyFieldValue(final Object targetObject, final Field targetField, final Field sourceField) {
        FieldSupport.from(targetField).setValue(targetObject, FieldSupport.from(sourceField).getValue(sourceObject));
    }

    @Override
    public boolean matches(final Object source) {
        return AnnotationStream.of(source).build().map(Annotation::annotationType).anyMatch(ConvertibleBean.class::equals);
    }
}
