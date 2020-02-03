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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class ReflectionStreamSupport {
    private ReflectionStreamSupport() {
        throw new IllegalStateException("Utility class");
    }
    
    private abstract static class AbstractReflectionMemberSupport<T> {
        protected final List<T> memberList = new ArrayList<>();
        protected final Class<?> sourceClass;
        protected boolean declared;
        protected boolean nested;
        
        protected AbstractReflectionMemberSupport(final Class<?> sourceClass) {
            this.sourceClass = sourceClass;
        }
        
        protected abstract Class<?> getMemberClass();
        
        protected void findMembers(final Class<?> sourceClass, final boolean declared, final boolean nested) {
            if (!sourceClass.equals(Object.class)) {
                memberList.addAll(Arrays.asList(getMembers(sourceClass, declared)));
                
                if (nested) {
                    findMembers(sourceClass.getSuperclass(), declared, nested);
                }
            } 
        }

        @SuppressWarnings("unchecked")
        private T[] getMembers(final Class<?> sourceClass, final boolean declared) {
            if (getMemberClass().equals(Field.class)) {
                return (T[]) (declared ? sourceClass.getDeclaredFields() : sourceClass.getFields());
            } else if (getMemberClass().equals(Method.class)) {
                return (T[]) (declared ? sourceClass.getDeclaredMethods() : sourceClass.getMethods());
            } else if (getMemberClass().equals(Constructor.class)) {
                return (T[]) (declared ? sourceClass.getDeclaredConstructors() : sourceClass.getConstructors());
            } else {
                return (T[]) (declared ? sourceClass.getDeclaredAnnotations() : sourceClass.getAnnotations());
            }
        }
        
        public Stream<T> build() {
            findMembers(sourceClass, declared, nested);
            
            return memberList.stream();
        }
    }
    
    static final class FieldStreamSupport extends AbstractReflectionMemberSupport<Field> implements FieldStream{
        protected FieldStreamSupport(final Class<?> sourceClass) {
            super(sourceClass);
        }

        static FieldStreamSupport from(final Class<?> sourceClass) {
            return new FieldStreamSupport(sourceClass);
        }

        @Override
        public FieldStreamSupport declared() {
            declared = true;
            
            return this;
        }

        @Override
        public FieldStreamSupport nested() {
            nested = true;
            
            return this;
        }

        @Override
        protected Class<Field> getMemberClass() {
            return Field.class;
        }
    }
    
    static final class MethodStreamSupport  extends AbstractReflectionMemberSupport<Method> implements MethodStream {
        protected MethodStreamSupport(final Class<?> sourceClass) {
            super(sourceClass);
        }

        static MethodStreamSupport from(final Class<?> sourceClass) {
            return new MethodStreamSupport(sourceClass);
        }

        @Override
        public MethodStreamSupport declared() {
            declared = true;
            
            return this;
        }

        @Override
        public MethodStreamSupport nested() {
            nested = true;
            
            return this;
        }

        @Override
        protected Class<Method> getMemberClass() {
            return Method.class;
        }
    }
    
    @SuppressWarnings("rawtypes")
    static final class ConstructorStreamSupport<T>  extends AbstractReflectionMemberSupport<Constructor<T>> implements ConstructorStream<T> {
        protected ConstructorStreamSupport(final Class<T> sourceClass) {
            super(sourceClass);
        }

        static <T> ConstructorStreamSupport<T> from(final Class<T> sourceClass) {
            return new ConstructorStreamSupport<T>(sourceClass);
        }

        @Override
        public ConstructorStreamSupport declared() {
            declared = true;
            
            return this;
        }

        @Override
        public ConstructorStreamSupport nested() {
            nested = true;
            
            return this;
        }

        @Override
        protected Class<Constructor> getMemberClass() {
            return Constructor.class;
        }
    }
    
    static final class AnnotationStreamSupport  extends AbstractReflectionMemberSupport<Annotation> implements AnnotationStream {
        protected AnnotationStreamSupport(final Class<?> sourceClass) {
            super(sourceClass);
        }

        static AnnotationStreamSupport from(final Class<?> sourceClass) {
            return new AnnotationStreamSupport(sourceClass);
        }

        @Override
        public AnnotationStreamSupport declared() {
            declared = true;
            
            return this;
        }

        @Override
        public AnnotationStreamSupport nested() {
            nested = true;
            
            return this;
        }

        @Override
        protected Class<Annotation> getMemberClass() {
            return Annotation.class;
        }
    }
}
