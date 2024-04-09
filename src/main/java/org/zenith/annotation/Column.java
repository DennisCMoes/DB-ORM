package org.zenith.annotation;

import org.zenith.enumeration.ColumnType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to map a field to a specific column in a database table.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    ColumnType type() default ColumnType.VARCHAR;
    int size() default 64;
}
