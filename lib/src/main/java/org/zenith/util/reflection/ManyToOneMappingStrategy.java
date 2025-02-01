package org.zenith.util.reflection;

import org.zenith.model.interfaces.IModel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ManyToOneMappingStrategy implements FieldMappingStrategy {
    @Override
    public void mapField(ResultSet resultSet, IModel model, Field field)
            throws SQLException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {

        // get the value of todoItem_id
        // Get the type and use that type to get the name + "_id"
        // Then use it to get the related field and then we can input it.
        if (!List.class.isAssignableFrom(field.getType()))
            return;

        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType))
            return;

        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        if (!(actualTypeArgument instanceof Class<?> actualClass))
            return;

        // Get the parent of the field
        field.set(model, model);
    }
}
