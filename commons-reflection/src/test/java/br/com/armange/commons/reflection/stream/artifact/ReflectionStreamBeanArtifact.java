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
package br.com.armange.commons.reflection.stream.artifact;

@AnnotationTestOne
public class ReflectionStreamBeanArtifact extends AbstractReflectionStreamBeanArtifact {
    @SuppressWarnings("unused")
    private String field1;
    private Integer field2;
    private long field3;
    public long field4;
    
    public ReflectionStreamBeanArtifact() {}
    public ReflectionStreamBeanArtifact(final String field1) {}
    @SuppressWarnings("unused")
    private ReflectionStreamBeanArtifact(final String field1, final long field3) {}
    
    public Integer getField2() {
        return field2;
    }
    
    public void setField2(final Integer field2) {
        this.field2 = field2;
    }
    
    public long getField3() {
        return field3;
    }
    
    public void setField3(final long field3) {
        this.field3 = field3;
    }
    
    @SuppressWarnings("unused")
    private void internalMethod() {}
}