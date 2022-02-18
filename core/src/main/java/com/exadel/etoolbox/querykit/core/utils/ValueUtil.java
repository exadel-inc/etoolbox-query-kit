package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.Calendar;
import java.util.Collection;

@UtilityClass
public class ValueUtil {

    public static Object extractValue(Property source) throws RepositoryException {
        if (source == null) {
            return null;
        }
        if (!source.isMultiple()) {
            return extractValue(source.getValue());
        }
        Value[] rawValues = source.getValues();
        Object[] result = new Object[rawValues.length];
        for (int i = 0; i < rawValues.length; i++) {
            result[i] = extractValue(rawValues[i]);
        }
        return result;
    }

    public static Object extractValue(Value source) throws RepositoryException {
        if (source == null) {
            return null;
        }
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

    public static int detectType(Object value) {
        if (value == null) {
            return PropertyType.UNDEFINED;
        }
        if (ClassUtils.isAssignable(value.getClass(), Boolean.class)) {
            return PropertyType.BOOLEAN;
        }
        if (ClassUtils.isAssignable(value.getClass(), Calendar.class)) {
            return PropertyType.DATE;
        }
        if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
            return PropertyType.LONG;
        }
        if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
            return PropertyType.DOUBLE;
        }
        if (ClassUtils.isAssignable(value.getClass(), String.class)) {
            return PropertyType.STRING;
        }
        return PropertyType.UNDEFINED;
    }

    public static boolean detectMultivalue(Object value) {
        if (value == null) {
            return false;
        }
        return ClassUtils.isAssignable(value.getClass(), Collection.class)
                || value.getClass().isArray();
    }

    public static Value createValue(Object source, ValueFactory valueFactory) {
        if (source == null) {
            return valueFactory.createValue(StringUtils.EMPTY);
        }
        if (ClassUtils.isAssignable(source.getClass(), Long.class)) {
            return valueFactory.createValue((long) source);
        }
        if (ClassUtils.isAssignable(source.getClass(), Double.class)) {
            return valueFactory.createValue((double) source);
        }
        if (ClassUtils.isAssignable(source.getClass(), Boolean.class)) {
            return valueFactory.createValue((boolean) source);
        }
        if (ClassUtils.isAssignable(source.getClass(), Calendar.class)) {
            return valueFactory.createValue((Calendar) source);
        }
        return valueFactory.createValue(source.toString());
    }
}
