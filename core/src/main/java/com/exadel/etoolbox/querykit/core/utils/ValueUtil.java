/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.Calendar;
import java.util.Collection;

/**
 * Contains utility methods for manipulating JCR values
 */
@UtilityClass
public class ValueUtil {

    /**
     * Retrieves an arbitrary value out of the given JCR {@link Property}
     * @param source {@code Property} instance
     * @return Nullable object
     * @throws RepositoryException If the value retrieval failed
     */
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

    /**
     * Retrieves an arbitrary value out of the given JCR {@link Value}
     * @param source {@code Value} instance
     * @return Nullable object
     * @throws RepositoryException If the value retrieval failed
     */
    public static Object extractValue(Value source) throws RepositoryException {
        if (source == null) {
            return null;
        }
        int type = source.getType();
        switch (type) {
            case PropertyType.BINARY:
                return source.getBinary();
            case PropertyType.BOOLEAN:
                return source.getBoolean();
            case PropertyType.DATE:
                return source.getDate();
            case PropertyType.DECIMAL:
                return source.getDecimal();
            case PropertyType.DOUBLE:
                return source.getDouble();
            case PropertyType.LONG:
                return source.getLong();
            case PropertyType.STRING:
            case PropertyType.NAME:
                return source.getString();
            default:
                return null;
        }
    }

    /**
     * Retrieves the type of the provided object. The type is selected among the managed {@link PropertyType}-s
     * @param value Arbitrary value
     * @return Int value; one og the {@code PropertyType}-s
     * @see PropertyType
     */
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

    /**
     * Gets whether the provided object is a multi-value, i.e., an array-like type
     * @param value Arbitrary value
     * @return True or false
     */
    public static boolean detectMultivalue(Object value) {
        if (value == null) {
            return false;
        }
        return ClassUtils.isAssignable(value.getClass(), Collection.class)
                || value.getClass().isArray();
    }

    /**
     * Creates a new <u>not</u> array-like {@link Value} out of the provided arbitrary object
     * @param source       Arbitrary value
     * @param valueFactory {@link ValueFactory} instance
     * @return Non-null {@code Value} object
     */
    public static Value createFlatValue(Object source, ValueFactory valueFactory) {
        if (source == null) {
            return valueFactory.createValue(StringUtils.EMPTY);
        }
        if (!source.getClass().isArray()) {
            return createValue(source, valueFactory);
        }
        if (ArrayUtils.getLength(source) > 0) {
            return createValue(((Object[]) source)[0], valueFactory);
        }
        return valueFactory.createValue(StringUtils.EMPTY);
    }

    /**
     * Creates a new {@link Value} out of the provided arbitrary object
     * @param source       Arbitrary value
     * @param valueFactory {@link ValueFactory} instance
     * @return Non-null {@code Value} object
     */
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
