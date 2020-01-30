package br.com.armange.commons.reflection.stream;

import java.lang.reflect.Method;

public interface MethodStream extends ReflectionMemberStream<Method, MethodStream>{
    public static MethodStream of(final Class<?> sourceClass) {
        return ReflectionStreamSupport.MethodStreamSupport.from(sourceClass);
    }
}
