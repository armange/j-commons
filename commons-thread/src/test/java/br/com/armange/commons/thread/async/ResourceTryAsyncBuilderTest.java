package br.com.armange.commons.thread.async;

import br.com.armange.commons.thread.CloseableConsumer;
import br.com.armange.commons.thread.CloseableConsumerWithException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static br.com.armange.commons.thread.Constants.JDK_BEHAVIOR;
import static br.com.armange.commons.thread.util.ThreadUtil.sleepUnchecked;
import static org.mockito.Mockito.*;

public class ResourceTryAsyncBuilderTest {

    @Test
    public void shouldTryWithResource() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final CloseableConsumer closeableConsumer = spy(new CloseableConsumer());

        TryAsyncBuilder.tryAsync(inputStream, closeableConsumer).execute();
        sleepUnchecked(500);
        verify(closeableConsumer).accept(inputStream);
        verify(inputStream).close();
    }

    @Test
    public void shouldTryWithResourceWithException() throws IOException {
        final InputStream inputStream = mock(InputStream.class);
        final CloseableConsumerWithException closeableConsumerWithException = spy(new CloseableConsumerWithException());

        TryAsyncBuilder.tryAsync(inputStream, closeableConsumerWithException).execute();
        sleepUnchecked(500);
        verify(closeableConsumerWithException).accept(inputStream);
        verify(inputStream).close();
    }

    @Test
    public void shouldThrowExceptionWhenClosingOneResource() throws IOException {
        final InputStream inputStream = mock(InputStream.class);

        doThrow(IOException.class).when(inputStream).close();
        TryAsyncBuilder.tryAsync(inputStream, e -> {
        }).execute();
        sleepUnchecked(500);
        verify(inputStream, times(JDK_BEHAVIOR)).close();
    }
}
