 package br.com.armange.commons.reflection.stream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import br.com.armange.commons.reflection.stream.artifact.ReflectionStreamBeanArtifact;

public class AnnotationStreamTest {
    @Test
    public void findDeclaredAnnotations() {
        final List<Annotation> list = AnnotationStream
            .of(ReflectionStreamBeanArtifact.class)
            .declared()
            .build()
            .collect(Collectors.toList());
        
        assertThat(list, hasSize(1));
        
        assertEquals("AnnotationTestOne", list.get(0).annotationType().getSimpleName());
    }
    
    @Test
    public void findNonDeclaredAnnotations() {
        final List<Annotation> list = AnnotationStream
                .of(ReflectionStreamBeanArtifact.class)
                .build()
                .collect(Collectors.toList());
            
            assertThat(list, hasSize(2));
            
            assertThat(list.stream()
                .map(Annotation::annotationType)
                .map(Class::getSimpleName)
                .collect(Collectors.toList()), 
                    allOf(
                            hasItem(is("AnnotationTestOne")),
                            hasItem(is("AnnotationTestTwo"))));
    }
    
    @Test
    public void findDeclaredNestedAnnotations() {
        final List<Annotation> list = AnnotationStream
                .of(ReflectionStreamBeanArtifact.class)
                .declared()
                .nested()
                .build()
                .collect(Collectors.toList());
            
            assertThat(list, hasSize(2));
            
            assertThat(list.stream()
                .map(Annotation::annotationType)
                .map(Class::getSimpleName)
                .collect(Collectors.toList()), 
                    allOf(
                            hasItem(is("AnnotationTestOne")),
                            hasItem(is("AnnotationTestTwo"))));
    }
    
    @Test
    public void findNonDeclaredNestedAnnotations() {
        final List<Annotation> list = AnnotationStream
                .of(ReflectionStreamBeanArtifact.class)
                .nested()
                .build()
                .collect(Collectors.toList());
            
            assertThat(list, hasSize(3));
            
            final List<String> names = list.stream()
                .map(Annotation::annotationType)
                .map(Class::getSimpleName)
                .distinct()
                .collect(Collectors.toList());
            
            assertThat(names, 
                    allOf(
                            hasItem(is("AnnotationTestOne")),
                            hasItem(is("AnnotationTestTwo"))));
            
            assertThat(names, hasSize(2));
            
    }
}
