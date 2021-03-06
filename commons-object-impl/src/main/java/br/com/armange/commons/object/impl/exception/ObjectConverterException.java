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
package br.com.armange.commons.object.impl.exception;

import br.com.armange.commons.object.impl.message.Messages;

public class ObjectConverterException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ObjectConverterException() {
        super();
    }

    public ObjectConverterException(final String message) {
        super(message);
    }

    public ObjectConverterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ObjectConverterException(final Throwable cause) {
        super(cause);
    }
    
    public ObjectConverterException(final Messages message, final Object... messageParameters) {
        super(message.format(messageParameters));
    }
}
