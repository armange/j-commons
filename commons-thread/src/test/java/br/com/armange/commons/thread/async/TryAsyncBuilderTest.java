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
package br.com.armange.commons.thread.async;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class TryAsyncBuilderTest {

    @Test
    public void shouldCallRunnableTryAsyncBuilder() {
        final RunnableTryAsyncBuilder runnableTryAsyncBuilder = TryAsyncBuilder.tryAsync(() -> {
        });

        Assertions.assertEquals(RunnableTryAsyncBuilder.class, runnableTryAsyncBuilder.getClass());
    }

    @Test
    public void shouldCallCallableTryAsyncBuilder() {
        final CallableTryAsyncBuilder<?> callableTryAsyncBuilder = TryAsyncBuilder.tryAsync(() -> null, result -> {
        });

        Assertions.assertEquals(CallableTryAsyncBuilder.class, callableTryAsyncBuilder.getClass());
    }

    @Test
    public void shouldCallResourceTryAsyncBuilder() {
        final InputStream inputStream = mock(InputStream.class);
        final ResourceTryAsyncBuilder callableTryAsyncBuilder = TryAsyncBuilder.tryAsync(inputStream, closeable -> {
        });

        Assertions.assertEquals(ResourceTryAsyncBuilder.class, callableTryAsyncBuilder.getClass());
    }

    @Test
    public void shouldCallResourcesTryAsyncBuilder() {
        final InputStream inputStream = mock(InputStream.class);
        final ResourcesTryAsyncBuilder resourcesTryAsyncBuilder = TryAsyncBuilder.tryAsync(closeable -> {
        }, inputStream);

        Assertions.assertEquals(ResourcesTryAsyncBuilder.class, resourcesTryAsyncBuilder.getClass());
    }

    @Test
    public void shouldCallMappedResourcesTryAsyncBuilder() {
        final InputStream inputStream = mock(InputStream.class);
        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);

        final MappedResourcesTryAsyncBuilder mappedResourcesTryAsyncBuilder = TryAsyncBuilder
                .tryAsync(map, mapOfCloseable -> {
                });

        Assertions.assertEquals(MappedResourcesTryAsyncBuilder.class, mappedResourcesTryAsyncBuilder.getClass());
    }
}
