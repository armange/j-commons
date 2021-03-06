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
package br.com.armange.commons.thread;

/**
 * Unchecked exceptions may be thrown instead of the RuntimeException option to allow 
 * differentiation between an unmapped fault and a controlled fault.
 * @author Diego Armange Costa
 * @since 2019-12-08 V1.1.0
 * @see java.lang.RuntimeException
 */
@SuppressWarnings("serial")
public class UncheckedException extends RuntimeException {

    /**
     * The builder will exclusively call SUPER ({@code RuntimeException#RuntimeException(Throwable)})
     * @param cause the cause
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public UncheckedException(final Throwable cause) {
        super(cause);
    }
}
