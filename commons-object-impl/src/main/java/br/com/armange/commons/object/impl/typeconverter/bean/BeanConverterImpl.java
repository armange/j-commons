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
package br.com.armange.commons.object.impl.typeconverter.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import br.com.armange.commons.message.CommonMessages;
import br.com.armange.commons.object.api.typeconverter.BeanConverter;
import br.com.armange.commons.object.api.typeconverter.TypeConverter;
import br.com.armange.commons.object.api.typeconverter.annotation.ConvertibleBean;
import br.com.armange.commons.object.api.typeconverter.bean.BeanConverterStrategy;
import br.com.armange.commons.object.impl.exception.ObjectConverterException;
import br.com.armange.commons.object.impl.message.Messages;
import br.com.armange.commons.reflection.stream.AnnotationStream;
import br.com.armange.commons.reflection.stream.ConstructorStream;
import br.com.armange.commons.reflection.stream.FieldStream;
import br.com.armange.commons.reflection.support.ConstructorSupport;

public class BeanConverterImpl<S, T> implements BeanConverter<S, T> {

    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    
    private S sourceObject;
    private List<Field> sourceFields;
    private BeanConverterStrategy strategy;
    
    @Override
    public TypeConverter<S, T> from(final S sourceObject) {
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
                .of(targetClass)
                .build()
                .parallel()
                .filter(noParams())
                .findFirst()
                .map(newInstanceByConstructor())
                .orElseThrow(newObjectConverterException(targetClass));

        final List<Field> targetFields = FieldStream
                .of(targetClass)
                .nested()
                .declared()
                .build()
                .collect(Collectors.toList());
        
        doConversionByStrategy(targetObject, targetFields);
        
        return targetObject;
    }

    private Predicate<? super Constructor<T>> noParams() {
        return c -> c.getParameterCount() == 0;
    }

    private Function<? super Constructor<T>, ? extends T> newInstanceByConstructor() {
        return c -> ConstructorSupport.from(c).newInstance();
    }

    private Supplier<? extends ObjectConverterException> newObjectConverterException(final Class<T> targetClass) {
        return () -> new ObjectConverterException(Messages.DEFAULT_CONSTRUCTOR_NOT_FOUND, targetClass.getName());
    }
    
    private void doConversionByStrategy(final T targetObject, final List<Field> targetFields) {
        switch (Optional.ofNullable(strategy).orElse(BeanConverterStrategy.SAME_NAME)) {
        case ANNOTATED:
            break;
        case HYBRID:
            break;
        default:
            //SAME_NAME
            final SameFieldNameStrategyConverter<S, T> bc = new SameFieldNameStrategyConverter<>();
            bc
                .readSource(sourceObject, sourceFields)
                .writeInto(targetObject, targetFields);
            break;
        }
    }

    @Override
    public boolean matches(final Object source, final Class<?> targetClass) {
        return AnnotationStream.of(source).build().map(Annotation::annotationType).anyMatch(ConvertibleBean.class::equals);
    }
    
    @Override
    public void setStrategy(final BeanConverterStrategy strategy) {
        this.strategy = strategy;
    }
    
    public static void main(final String[] args) {
        System.out.println(int.class.getName());
        System.out.println(Integer.class.getName());
    }
}
