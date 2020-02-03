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

public class MethodStreamTest {
    private static final String GET_FIELD2 = "getField2";
    private static final String INTERNAL_METHOD = "internalMethod";
    private static final String NESTED_METHOD = "nestedMethod";
    private static final String GET_NESTED_FIELD3 = "getNestedField3";
    private static final String NAME = "name";

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
                                                NAME, Matchers.is(NESTED_METHOD)))),
                        hasItem(
                                hasProperty(
                                        NAME, Matchers.is(INTERNAL_METHOD)))));
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
                                                NAME, Matchers.is(INTERNAL_METHOD)))),
                        not(
                                hasItem(
                                        hasProperty(
                                                NAME, Matchers.is(NESTED_METHOD)))),
                    hasItem(
                            hasProperty(
                                    NAME, Matchers.is(GET_FIELD2)))));
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
                                        NAME, Matchers.is(INTERNAL_METHOD))),
                        hasItem(
                                hasProperty(
                                        NAME, Matchers.is(NESTED_METHOD)))));
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
                                                NAME, Matchers.is(NESTED_METHOD)))),
                        hasItem(
                                hasProperty(
                                        NAME, Matchers.is(GET_NESTED_FIELD3)))));
    }
}
