package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.MappedCloseableConsumer;
import br.com.armange.commons.thread.MappedCloseableConsumerWithException;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static br.com.armange.commons.thread.Constants.JDK_BEHAVIOR;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static org.mockito.Mockito.*;

public class MappedResourcesTryAsyncBuilderTest {

    @Test
    public void shouldTryWithMappedResource() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final MappedCloseableConsumer mappedCloseableConsumer = spy(new MappedCloseableConsumer());
        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);
        map.put(2, inputStream2);
        TryAsyncBuilder.tryAsync(map, mappedCloseableConsumer).execute();
        sleepUnchecked(500);
        verify(mappedCloseableConsumer).accept(map);
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void shouldTryWithMappedResourceWithException() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final MappedCloseableConsumerWithException mappedCloseableConsumerWithException = spy(
                new MappedCloseableConsumerWithException());
        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);
        map.put(2, inputStream2);
        TryAsyncBuilder.tryAsync(map, mappedCloseableConsumerWithException).execute();
        sleepUnchecked(500);
        verify(mappedCloseableConsumerWithException).accept(map);
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void shouldThrowExceptionWhenClosingOneResourceFromMap() throws IOException {
        final InputStream inputStream = mock(InputStream.class);

        doThrow(IOException.class).when(inputStream).close();

        final Map<Object, Closeable> map = new HashMap<>();

        map.put(0, inputStream);
        TryAsyncBuilder.tryAsync(map, e -> {
        }).execute();
        sleepUnchecked(500);
        verify(inputStream, times(JDK_BEHAVIOR)).close();
    }
}
