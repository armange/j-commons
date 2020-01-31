package br.com.armange.commons.reflection.stream;

import java.lang.reflect.Constructor;

@SuppressWarnings("rawtypes")
public interface ConstructorStream extends ReflectionMemberStream<Constructor, ConstructorStream>{
    public static ConstructorStream of(final Class<?> sourceClass) {
        return ReflectionStreamSupport.ConstructorStreamSupport.from(sourceClass);
    }
}
