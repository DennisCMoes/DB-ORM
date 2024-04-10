package org.zenith.mapper;

import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.core.EntityManager;
import org.zenith.model.interfaces.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class used to map database result sets to Java objects.
 * It would contain methods for converting database records to
 * instances of entity classes based on entity mappings.
 */
public class EntityMapper {
    public static <T extends IModel> List<T> resultToList(ResultSet resultSet, Class<T> classObj) throws SQLException {
        List<T> objects = new ArrayList<>();

        while (resultSet.next())
            objects.add(resultToObject(resultSet, classObj, false));

        return objects;
    }

    public static <T extends IModel> T resultToObject(ResultSet resultSet, Class<T> classObj) throws SQLException {
        return resultToObject(resultSet, classObj, true);
    }

    private static <T extends IModel> T resultToObject(ResultSet resultSet, Class<T> classObj, boolean hasOneRow)
            throws SQLException {
        try {
            Field[] fields = classObj.getDeclaredFields();
            T object = classObj.getDeclaredConstructor().newInstance();

            if (hasOneRow)
                resultSet.next();

            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();
                // TODO: Make this work with multiple annotations
                Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];
                Object value;

                if (fieldAnnotation instanceof OneToOne || fieldAnnotation instanceof ManyToOne) {
                    fieldName += "_id";

                    value = resultSet.getObject(fieldName);
                    T model = (T) field.getType().getConstructor().newInstance();
                    model.getClass().getField("id").set(model, value);

                    EntityManager entityManager = new EntityManager();
                    Object linkedObj = entityManager.findEntity(model, model.getClass());

                    // Link the new linked object to the original object
                    field.set(object, linkedObj);
                } else {
                    value = resultSet.getObject(fieldName);
                    field.set(object, value);
                }
            }

            return object;
        } catch (ConnectException | NoSuchFieldException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
