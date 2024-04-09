package org.zenith.mapper;

import org.zenith.model.interfaces.IModel;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A utility class used to map database result sets to Java objects.
 * It would contain methods for converting database records to
 * instances of entity classes based on entity mappings.
 */
public class ResultSetMapper {
    public static <T extends IModel> List<T> resultToList(ResultSet resultSet, Class<T> classObj) {
        List<T> objects = new ArrayList<>();

        try {
            Field[] fields = classObj.getDeclaredFields();

            while (resultSet.next()) {
                T object = classObj.getDeclaredConstructor().newInstance();

                for (Field field : fields) {
                    field.setAccessible(true);

                    Object value = resultSet.getObject(field.getName());
                    field.set(object, value);
                }

                objects.add(object);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return objects;
    }

    public static <T extends IModel> T resultToObject(ResultSet resultSet, Class<T> classObj) {
        try {
            Field[] fields = classObj.getDeclaredFields();
            T object = classObj.getDeclaredConstructor().newInstance();

            resultSet.next();

            for (Field field : fields) {
                field.setAccessible(true);

                Object value = resultSet.getObject(field.getName());
                field.set(object, value);
            }

            return object;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
