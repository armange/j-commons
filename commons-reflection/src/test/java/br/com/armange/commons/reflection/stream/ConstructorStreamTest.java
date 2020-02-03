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
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Test;

import br.com.armange.commons.reflection.stream.artifact.ReflectionStreamBeanArtifact;

public class ConstructorStreamTest {

    private static final String PARAMETER_TYPES = "parameterTypes";

    @Test
    public void findDeclaredConstructors() {
        assertThat(
                ConstructorStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .declared()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                PARAMETER_TYPES, Matchers.is(new Class[] {long.class})))),
                        hasItem(
                                hasProperty(
                                        PARAMETER_TYPES, Matchers.is(new Class[] {})))));
    }
    
    @Test
    public void findNonDeclaredConstructor() {
        assertThat(
                ConstructorStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                PARAMETER_TYPES, Matchers.is(new Class[] {long.class})))),
                        not(
                                hasItem(
                                        hasProperty(
                                                PARAMETER_TYPES, Matchers.is(new Class[] {String.class, long.class})))),
                    hasItem(
                            hasProperty(
                                    PARAMETER_TYPES, Matchers.is(new Class[] {})))));
    }
    
    @Test
    public void findDeclaredNestedFields() {
        assertThat(
                ConstructorStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .declared()
                    .nested()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        hasItem(
                                hasProperty(
                                        PARAMETER_TYPES, Matchers.is(new Class[] {long.class}))),
                        hasItem(
                                hasProperty(
                                        PARAMETER_TYPES, Matchers.is(new Class[] {String.class, long.class})))));
    }
    
    @Test
    public void findNonDeclaredNestedFields() {
        assertThat(
                ConstructorStream
                    .of(ReflectionStreamBeanArtifact.class)
                    .nested()
                    .build()
                    .collect(Collectors.toList()), 
                allOf(
                        not(
                                hasItem(
                                        hasProperty(
                                                PARAMETER_TYPES, Matchers.is(new Class[] {String.class, long.class})))),
                        hasItem(
                                hasProperty(
                                        PARAMETER_TYPES, Matchers.is(new Class[] {long.class})))));
    }
}
