package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

@UtilityClass
public class ValueUtil {

    public static Object getValue(Property source) throws RepositoryException {
        if (source == null) {
            return null;
        }
        if (!source.isMultiple()) {
            return getValue(source.getValue());
        }
        Value[] rawValues = source.getValues();
        Object[] result = new Object[rawValues.length];
        for (int i = 0; i < rawValues.length; i++) {
            result[i] = getValue(rawValues[i]);
        }
        return result;
    }

    public static Object getValue(Value source) throws RepositoryException {
        int type = source.getType();
        switch (type) {
            case PropertyType.BINARY: return source.getBinary();
            case PropertyType.BOOLEAN: return source.getBoolean();
            case PropertyType.DATE: return source.getDate();
            case PropertyType.DECIMAL: return source.getDecimal();
            case PropertyType.DOUBLE: return source.getDouble();
            case PropertyType.LONG: return source.getLong();
            case PropertyType.STRING:
            case PropertyType.NAME: return source.getString();
            default: return null;
        }

    }
}
