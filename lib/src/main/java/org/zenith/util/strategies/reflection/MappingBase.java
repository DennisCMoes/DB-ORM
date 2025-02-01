package org.zenith.util.strategies.reflection;

import org.zenith.model.interfaces.IModel;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class MappingBase {
    protected boolean hasColumn(ResultSet resultSet, String columnName)  {
        try {
            resultSet.findColumn(columnName);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    protected  <T extends IModel> Class<T> castToIModelClass(Class<?> modelClass) {
        if (!IModel.class.isAssignableFrom(modelClass)) {
            throw new IllegalArgumentException(String.format("Class %s does not extend IModel", modelClass.getName()));
        }

        return (Class<T>) modelClass;
    }
}
