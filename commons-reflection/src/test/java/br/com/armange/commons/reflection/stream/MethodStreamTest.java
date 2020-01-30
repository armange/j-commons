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

public class MethodStreamTest {
    @Test
    public void findDeclaredMethods() {
        assertThat(
                MethodStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .declared()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("nestedMethod")))),
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("internalMethod")))));
    }
    
    @Test
    public void findNonDeclaredMethods() {
        assertThat(
                MethodStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("internalMethod")))),
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("nestedMethod")))),
                    hasItem(
                            hasProperty(
                                    "name", Matchers.is("getField2")))));
    }
    
    @Test
    public void findDeclaredNestedMethods() {
        assertThat(
                MethodStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .declared()
                    .nested()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("internalMethod"))),
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("nestedMethod")))));
    }
    
    @Test
    public void findNonDeclaredNestedMethods() {
        assertThat(
                MethodStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .nested()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                "name", Matchers.is("nestedMethod")))),
                        hasItem(
                                hasProperty(
                                        "name", Matchers.is("getNestedField3")))));
    }
}
