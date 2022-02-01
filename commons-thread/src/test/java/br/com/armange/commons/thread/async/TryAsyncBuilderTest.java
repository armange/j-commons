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

import br.com.armange.commons.thread.util.ByteBuddyUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Closeable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class TryAsyncBuilderTest {

    private static final RunnableTryAsyncBuilder MOCKED_RUNNABLE_TRY_ASYNC_BUILDER = Mockito
            .mock(RunnableTryAsyncBuilder.class);
    private static final CallableTryAsyncBuilder MOCKED_CALLABLE_TRY_ASYNC_BUILDER = Mockito
            .mock(CallableTryAsyncBuilder.class);
    private static final ResourceTryAsyncBuilder MOCKED_RESOURCE_TRY_ASYNC_BUILDER = Mockito
            .mock(ResourceTryAsyncBuilder.class);
    private static final ResourcesTryAsyncBuilder MOCKED_RESOURCES_TRY_ASYNC_BUILDER = Mockito
            .mock(ResourcesTryAsyncBuilder.class);
    private static final MappedResourcesTryAsyncBuilder MOCKED_MAPPED_RESOURCES_TRY_ASYNC_BUILDER = Mockito
            .mock(MappedResourcesTryAsyncBuilder.class);

    @Test
    public void shouldCallRunnableTryAsyncBuilder() {
        mockStatic(RunnableTryAsyncBuilder.class, RunnableTryAsyncBuilderInterceptor.class);

        final RunnableTryAsyncBuilder runnableTryAsyncBuilder = TryAsyncBuilder.tryAsync(() -> {
        });

        Assert.assertEquals(MOCKED_RUNNABLE_TRY_ASYNC_BUILDER, runnableTryAsyncBuilder);
        ByteBuddyUtil.resetClassDefinition(RunnableTryAsyncBuilder.class);
    }

    @Test
    public void shouldCallCallableTryAsyncBuilder() {
        mockStatic(CallableTryAsyncBuilder.class, CallableTryAsyncBuilderInterceptor.class);

        final CallableTryAsyncBuilder<?> callableTryAsyncBuilder = TryAsyncBuilder.tryAsync(() -> null, result -> {
        });

        Assert.assertEquals(MOCKED_CALLABLE_TRY_ASYNC_BUILDER, callableTryAsyncBuilder);
        ByteBuddyUtil.resetClassDefinition(CallableTryAsyncBuilder.class);
    }

    @Test
    public void shouldCallResourceTryAsyncBuilder() {
        mockStatic(ResourceTryAsyncBuilder.class, ResourceTryAsyncBuilderInterceptor.class);

        final InputStream inputStream = mock(InputStream.class);
        final ResourceTryAsyncBuilder callableTryAsyncBuilder = TryAsyncBuilder.tryAsync(inputStream, closeable -> {
        });

        Assert.assertEquals(MOCKED_RESOURCE_TRY_ASYNC_BUILDER, callableTryAsyncBuilder);
        ByteBuddyUtil.resetClassDefinition(ResourceTryAsyncBuilder.class);
    }

    @Test
    public void shouldCallResourcesTryAsyncBuilder() {
        mockStatic(ResourcesTryAsyncBuilder.class, ResourcesTryAsyncBuilderInterceptor.class);

        final InputStream inputStream = mock(InputStream.class);
        final ResourcesTryAsyncBuilder resourcesTryAsyncBuilder = TryAsyncBuilder.tryAsync(closeable -> {
        }, inputStream);

        Assert.assertEquals(MOCKED_RESOURCES_TRY_ASYNC_BUILDER, resourcesTryAsyncBuilder);
        ByteBuddyUtil.resetClassDefinition(ResourcesTryAsyncBuilder.class);
    }

    @Test
    public void shouldCallMappedResourcesTryAsyncBuilder() {
        mockStatic(MappedResourcesTryAsyncBuilder.class, MappedResourcesTryAsyncBuilderInterceptor.class);

        final InputStream inputStream = mock(InputStream.class);
        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);

        final MappedResourcesTryAsyncBuilder mappedResourcesTryAsyncBuilder = TryAsyncBuilder
                .tryAsync(map, mapOfCloseable -> {
                });

        Assert.assertEquals(MOCKED_MAPPED_RESOURCES_TRY_ASYNC_BUILDER, mappedResourcesTryAsyncBuilder);
        ByteBuddyUtil.resetClassDefinition(MappedResourcesTryAsyncBuilder.class);
    }

    private void mockStatic(final Class<?> targetClass, final Class<?> interceptor) {
        ByteBuddyUtil
                .changeClass(targetClass)
                .whenCallMethod("tryAsync")
                .thenCall(interceptor);
    }

    public static class RunnableTryAsyncBuilderInterceptor {
        public static RunnableTryAsyncBuilder intercept() {
            return MOCKED_RUNNABLE_TRY_ASYNC_BUILDER;
        }
    }

    public static class CallableTryAsyncBuilderInterceptor {
        public static CallableTryAsyncBuilder intercept() {
            return MOCKED_CALLABLE_TRY_ASYNC_BUILDER;
        }
    }

    public static class ResourceTryAsyncBuilderInterceptor {
        public static ResourceTryAsyncBuilder intercept() {
            return MOCKED_RESOURCE_TRY_ASYNC_BUILDER;
        }
    }

    public static class ResourcesTryAsyncBuilderInterceptor {
        public static ResourcesTryAsyncBuilder intercept() {
            return MOCKED_RESOURCES_TRY_ASYNC_BUILDER;
        }
    }

    public static class MappedResourcesTryAsyncBuilderInterceptor {
        public static MappedResourcesTryAsyncBuilder intercept() {
            return MOCKED_MAPPED_RESOURCES_TRY_ASYNC_BUILDER;
        }
    }
}
