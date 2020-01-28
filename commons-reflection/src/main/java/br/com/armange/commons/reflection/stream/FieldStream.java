package br.com.armange.commons.reflection.stream;

import java.lang.reflect.Field;

public interface FieldStream extends ReflectionMemberStream<Field, FieldStream> {
    public static FieldStream of(final Class<?> sourceClass) {
        return ReflectionStreamSupport.FieldStreamSupport.from(sourceClass);
    }
}
