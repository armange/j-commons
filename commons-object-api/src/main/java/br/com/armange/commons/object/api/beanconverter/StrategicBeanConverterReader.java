package br.com.armange.commons.object.api.beanconverter;

import java.lang.reflect.Field;
import java.util.List;

public interface StrategicBeanConverterReader<S, T> {
    StrategicBeanConverterWriter<S, T> readSource(S sourceObject, List<Field> sourceFields);
}
