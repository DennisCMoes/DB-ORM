package org.zenith.util.reflection;

import org.zenith.model.interfaces.IModel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface FieldMappingStrategy {
    void mapField(ResultSet resultSet, IModel model, Field field)
            throws SQLException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException;
}
