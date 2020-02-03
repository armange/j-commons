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

@AnnotationTestTwo
public class AbstractReflectionStreamBeanArtifact {
    @SuppressWarnings("unused")
    private String nestedField1;
    protected Integer nestedField2;
    private long nestedField3;
    public long nestedField4;
    
    protected AbstractReflectionStreamBeanArtifact() {}
    public AbstractReflectionStreamBeanArtifact(final long nestedField3) {}
    
    public long getNestedField3() {
        return nestedField3;
    }
    
    public void setNestedField3(final long nestedField3) {
        this.nestedField3 = nestedField3;
    }
    
    @SuppressWarnings("unused")
    private void nestedMethod() {}
}
