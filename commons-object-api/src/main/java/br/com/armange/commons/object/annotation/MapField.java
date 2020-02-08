package br.com.armange.commons.object.annotation;

public @interface MapField {
    String sourceField() default "";
    Class<?> target() default Object.class;
    String targetField() default "";
}
