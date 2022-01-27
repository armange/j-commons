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
package br.com.armange.commons.object.impl.typeconverter.character;

import br.com.armange.commons.object.api.typeconverter.TypeConverter;
import br.com.armange.commons.object.api.typeconverter.charactere.CharacterToByteConverter;

public class CharacterToByteConverterImpl implements CharacterToByteConverter {

    private Byte result;
    
    @Override
    public TypeConverter<Character, Byte> from(final Character sourceObject) {
        result = sourceObject != null ? (byte)sourceObject.charValue() : null;
        
        return this; 
    }

    @Override
    public Byte to(final Class<Byte> targetClass) {
        return result;
    }

    @Override
    public boolean matches(final Object sourceObject, final Class<?> targetClass) {
        return sourceObject.getClass().equals(Character.class) && targetClass.equals(Byte.class);
    }
}
