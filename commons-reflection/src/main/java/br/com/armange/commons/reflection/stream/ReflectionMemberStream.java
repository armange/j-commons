package br.com.armange.commons.reflection.stream;

import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public interface ReflectionMemberStream<T, U extends ReflectionMemberStream> {
    U declared();
    U nested();
    Stream<T> build();
}
