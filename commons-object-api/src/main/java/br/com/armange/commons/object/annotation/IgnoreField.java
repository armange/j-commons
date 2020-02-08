package br.com.armange.commons.object.annotation;

public @interface IgnoreField {
    //Subfields
    String[] value() default "";
}
