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
package br.com.armange.commons.spi.exception;

import br.com.armange.commons.spi.Loader;

/**
 * Thrown to indicate that no implementations was found for the given service type.
 * @author Diego Armange Costa
 * @since 2020-02-22 V1.0.0 (JDK 1.8)
 * @see Loader
 * @see Loader#loadService(Class)
 * @see Loader#loadServices(Class)
 */
public class NoImplementationFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Delegates to the super constructor, passing a string message
     * ("The service implementation [%s] was not found"). 
     * @param service the given service that no implementations was found.
     */
    public NoImplementationFoundException(final Class<?> service) {
        super(String.format("The service implementation [%s] was not found", service.getName()));
    }
}
