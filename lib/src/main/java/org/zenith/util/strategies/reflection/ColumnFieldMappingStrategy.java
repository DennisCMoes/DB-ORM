package org.zenith.util.strategies.reflection;

import org.zenith.annotation.Column;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ColumnFieldMappingStrategy implements FieldMappingStrategy {
    @Override
    public void mapField(ResultSet resultSet, IModel model, Field field)
            throws SQLException, IllegalAccessException {

        String columnName = field.getName();
        Object fieldValue = resultSet.getObject(columnName);
        Column annotation = field.getAnnotation(Column.class);

        try {
            switch (annotation.type()) {
                case ColumnType.DATETIME -> fieldValue = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse((String) fieldValue);
                case ColumnType.BOOLEAN -> fieldValue = (int) fieldValue != 0;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        field.set(model, fieldValue);
    }
}
