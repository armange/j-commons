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

import java.util.Iterator;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.armange.commons.spi.artifact.MultipleServices;
import br.com.armange.commons.spi.artifact.Service;
import br.com.armange.commons.spi.exception.NoImplementationFoundException;

public class LoaderTest {

    @Test
    public void loadSingleService() {
        Assertions.assertNotNull(Loader.loadService(Service.class));
        
        final List<Service> services = Loader.loadServices(Service.class);
        
        MatcherAssert.assertThat(services, Matchers.hasSize(1));
    }
    
    @Test
    public void loadMultipleServices() {
        Assertions.assertNotNull(Loader.loadService(MultipleServices.class));
        
        final List<MultipleServices> services = Loader.loadServices(MultipleServices.class);
        
        MatcherAssert.assertThat(services, Matchers.hasSize(2));
    }
    
    @Test
    public void throwNoImplExceptionForSingleServiceLoading() {
        Assertions.assertThrows(NoImplementationFoundException.class,
                () -> Loader.loadService(Iterator.class));
    }
    
    @Test
    public void throwNoImplExceptionForMultipleServiceLoading() {
        Assertions.assertThrows(NoImplementationFoundException.class,
                () -> Loader.loadServices(Iterator.class));
    }
}
