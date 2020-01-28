package br.com.armange.commons.reflection.stream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Test;

import br.com.armange.commons.reflection.stream.artifact.ReflectionStreamBeanArtifact;

public class FieldStreamTest {

    @Test
    public void findDeclaredFields() {
        assertThat(
                FieldStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .declared()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("nestedField1")))),
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("field1")))));
    }
    
    @Test
    public void findNonDeclaredFields() {
        assertThat(
                FieldStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("field1")))),
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("nestedField1")))),
                    hasItem(
                            hasProperty(
                                    "name", Matchers.is("field4")))));
    }
    
    @Test
    public void findDeclaredNestedFields() {
        assertThat(
                FieldStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .declared()
                    .nested()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("nestedField1"))),
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("field1")))));
    }
    
    @Test
    public void findNonDeclaredNestedFields() {
        assertThat(
                FieldStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .nested()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("nestedField1")))),
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("nestedField4")))));
    }
}
