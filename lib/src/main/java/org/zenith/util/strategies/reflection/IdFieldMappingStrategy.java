package org.zenith.util.strategies.reflection;

import org.zenith.model.interfaces.IModel;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdFieldMappingStrategy extends MappingBase implements FieldMappingStrategy {
    @Override
    public void mapField(ResultSet resultSet, IModel model, Field field)
            throws SQLException, IllegalAccessException {

        if (resultSet == null || model == null || field == null) {
            throw new IllegalArgumentException("ResultSet, model, or field cannot be null");
        }

        if (!hasColumn(resultSet, "id")) {
            throw new SQLException("Column 'id' could not be found in the ResultSet");
        }

        int id = resultSet.getInt("id");
        field.setAccessible(true);
        field.set(model, id);
    }
}
