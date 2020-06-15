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

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

import br.com.armange.commons.object.api.typeconverter.TypeConverter;
import br.com.armange.commons.object.api.typeconverter.bean.StrategicBeanConverter;
import br.com.armange.commons.object.api.typeconverter.bean.StrategicBeanConverterWriter;
import br.com.armange.commons.reflection.support.FieldSupport;
import br.com.armange.commons.spi.Loader;

public class SameFieldNameStrategyConverter<S, T> implements StrategicBeanConverter<S, T> {

    private S sourceObject;
    private List<Field> sourceFields;
    
    public SameFieldNameStrategyConverter() {}
    
    @Override
    public StrategicBeanConverterWriter<S, T> readSource(final S sourceObject, final List<Field> sourceFields) {
        this.sourceObject = sourceObject;
        this.sourceFields = sourceFields;
        
        return this;
    }
    
    @Override
    public void writeInto(final T targetObject, final List<Field> targetFields) {
        targetFields
            .parallelStream()
            .forEach(forEachSourceFieldDoSameFieldConsumer(targetObject));
    }

    private Consumer<? super Field> forEachSourceFieldDoSameFieldConsumer(final T targetObject) {
        return targetField -> {
            sourceFields.parallelStream().forEach(sameFieldConsumer(targetObject, targetField));
        };
    }
    
    private Consumer<? super Field> sameFieldConsumer(final Object targetObject, final Field targetField) {
        return sourceField -> { 
            if (sourceField.getName().equals(targetField.getName())) {
                copyFieldValue(targetObject, targetField, sourceField);
            }
        };
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void copyFieldValue(final Object targetObject, final Field targetField, final Field sourceField) {
        final Object sourceValue = FieldSupport.from(sourceField).getValue(sourceObject);
        final Class sourceClass = sourceField.getType();
        final Class targetClass = targetField.getType();
        
        if (sourceClass.equals(targetClass)) {
            FieldSupport
                .from(targetField)
                .setValue(targetObject, sourceValue);
        } else {
            final TypeConverter<Object, Object> typeConverter = Loader
                    .loadServices(TypeConverter.class, true)
                    .filter(t -> t.matches(sourceValue, targetField.getType()))
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
            
            FieldSupport
                .from(targetField)
                .setValue(targetObject, typeConverter
                    .from(sourceValue)
                    .to(targetClass));
        }
        
    }
}
