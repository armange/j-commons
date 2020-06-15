package br.com.armange.commons.object.api.typeconverter.bean;

import java.lang.reflect.Field;
import java.util.List;

public interface StrategicBeanConverterWriter<S, T> {
    void writeInto(T targetObject, List<Field> targetFields);
}
