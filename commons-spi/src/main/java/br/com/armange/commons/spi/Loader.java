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
package br.com.armange.commons.spi;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import br.com.armange.commons.spi.exception.NoImplementationFoundException;

/**
 * A simple service-provider loading facility.
 * 
 * @author Diego Armange Costa
 * @since 2020-02-22 V1.0.0 (JDK 1.8)
 * 
 * @see java.util.ServiceLoader
 */
public class Loader {
    private Loader() {}
    
    /**
     * Loads a single service implementation according the given service interface(class).
     * @param <T> The type of service whose implementation will be loaded
     * @param service The interface(class) of service whose implementation will be loaded
     * @return A single service implementation
     * @throws NoImplementationFoundException if the service has no implementations.
     * @see java.util.ServiceLoader#load(Class)
     */
    public static <T> T loadService(final Class<T> service) {
        final ServiceLoader<T> loader = ServiceLoader.load(service);
        
        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            throw new NoImplementationFoundException(service);
        }
    }
    
    /**
     * Loads a stream of service implementations according the given service interface(class).
     * @param <T> The type of service whose implementations will be loaded
     * @param service The interface(class) of service whose implementations will be loaded
     * @param parallel if {@code true} then the returned stream is a parallel stream; 
     * if {@code false} the returned stream is a sequential stream
     * @return A stream of implementations
     * @throws NoImplementationFoundException if the service has no implementations.
     * @see java.util.ServiceLoader#load(Class)
     * @see java.util.stream.StreamSupport#stream(java.util.function.Supplier, int, boolean)
     */
    public static <T> Stream<T> loadServices(final Class<T> service, final boolean parallel) {
        final ServiceLoader<T> loader = ServiceLoader.load(service);
        
        if (loader.iterator().hasNext()) {
            return StreamSupport.stream(loader.spliterator(), parallel);
        } else {
            throw new NoImplementationFoundException(service);
        }
    }

    /**
     * Loads a stream of service implementations according the given service interface(class). 
     * Delegates to {@link #loadServices(Class, boolean)}
     * @param <T> The type of service whose implementations will be loaded
     * @param service The interface(class) of service whose implementations will be loaded
     * @return A list of implementations
     * @throws NoImplementationFoundException if the service has no implementations.
     * @see #loadServices(Class, boolean)
     */
    public static <T> List<T> loadServices(final Class<T> service) {
        return loadServices(service, false).collect(Collectors.toList());
    }
}
