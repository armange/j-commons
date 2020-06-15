package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.CloseableArrayConsumer;
import br.com.armange.commons.thread.CloseableArrayConsumerWithException;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static br.com.armange.commons.thread.Constants.JDK_BEHAVIOR;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static org.mockito.Mockito.*;

public class ResourcesTryAsyncBuilderTest {

    @Test
    public void shouldTryWithResources() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final CloseableArrayConsumer closeableArrayConsumer = spy(new CloseableArrayConsumer());

        TryAsyncBuilder.tryAsync(closeableArrayConsumer, inputStream, inputStream2).execute();
        sleepUnchecked(500);
        verify(closeableArrayConsumer).accept(new Closeable[]{inputStream, inputStream2});
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void shouldTryWithResourcesWithException() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final InputStream inputStream2 = mock(InputStream.class);
        final CloseableArrayConsumerWithException closeableArrayConsumerWithException = spy(new CloseableArrayConsumerWithException());

        TryAsyncBuilder.tryAsync(closeableArrayConsumerWithException, inputStream, inputStream2).execute();
        sleepUnchecked(500);
        verify(closeableArrayConsumerWithException).accept(new Closeable[]{inputStream, inputStream2});
        verify(inputStream).close();
        verify(inputStream2).close();
    }

    @Test
    public void shouldThrowExceptionWhenClosingOneResourceFromArray() throws IOException {
        final InputStream inputStream = mock(InputStream.class);

        doThrow(IOException.class).when(inputStream).close();
        TryAsyncBuilder.tryAsync(e -> {
        }, inputStream).execute();
        sleepUnchecked(500);
        verify(inputStream, times(JDK_BEHAVIOR)).close();
    }
}
