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
package br.com.armange.commons.object.impl.typeconverter.booleanvalue;

import br.com.armange.commons.object.api.typeconverter.TypeConverter;
import br.com.armange.commons.object.api.typeconverter.booleanvalue.BooleanToFloatConverter;

public class BooleanToFloatConverterImpl implements BooleanToFloatConverter {

    private Float result;
    
    @Override
    public TypeConverter<Boolean, Float> from(final Boolean sourceObject) {
        result = sourceObject != null ? toNumber(sourceObject) : null;
        
        return this; 
    }

    private float toNumber(final Boolean sourceObject) {
        return sourceObject ? (float)1 : (float)0;
    }
    
    @Override
    public Float to(final Class<Float> targetClass) {
        return result;
    }

    @Override
    public boolean matches(final Object sourceObject, final Class<?> targetClass) {
        return sourceObject.getClass().equals(Boolean.class) && targetClass.equals(Float.class);
    }
}
