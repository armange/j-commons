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
    private static final String ANNOTATION_TEST_ONE = "AnnotationTestOne";
    private static final String ANNOTATION_TEST_TWO = "AnnotationTestTwoArtifact";

    @Test
    public void findDeclaredAnnotations() {
        final List<Annotation> list = AnnotationStream
            .of(ReflectionStreamBeanArtifact.class)
            .declared()
            .build()
            .collect(Collectors.toList());
        
        assertThat(list, hasSize(1));
        
        assertEquals(ANNOTATION_TEST_ONE, list.get(0).annotationType().getSimpleName());
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
                            hasItem(is(ANNOTATION_TEST_ONE)),
                            hasItem(is(ANNOTATION_TEST_TWO))));
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
                            hasItem(is(ANNOTATION_TEST_ONE)),
                            hasItem(is(ANNOTATION_TEST_TWO))));
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
                            hasItem(is(ANNOTATION_TEST_ONE)),
                            hasItem(is(ANNOTATION_TEST_TWO))));
            
            assertThat(names, hasSize(2));
    }
}
