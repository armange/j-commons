package br.com.armange.commons.reflection.stream;

import java.lang.annotation.Annotation;

public interface AnnotationStream extends ReflectionMemberStream<Annotation, AnnotationStream>{
    public static AnnotationStream of(final Class<?> sourceClass) {
        return ReflectionStreamSupport.AnnotationStreamSupport.from(sourceClass);
    }
}
