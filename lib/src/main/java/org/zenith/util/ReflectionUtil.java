package org.zenith.util;

import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToMany;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.reflection.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReflectionUtil {
    private static final Set<Class<? extends Annotation>> RELATION_ANNOTATIONS = Set.of(OneToOne.class, ManyToOne.class);
    private static final Map<Class<? extends Annotation>, FieldMappingStrategy> MAPPING_STRATEGIES = Map.of(
            Id.class, new IdFieldMappingStrategy(),
            ManyToOne.class, new ManyToOneMappingStrategy(),
            OneToMany.class, new OneToManyFieldMappingStrategy(),
            Column.class, new ColumnFieldMappingStrategy());

    private ReflectionUtil() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    /**
     * Maps the values from a {@link ResultSet} to an instance of the specified model class.
     *
     * @param resultSet The {@link ResultSet} containing the data to map
     * @param modelClass The {@link Class} f the model to which the data should be mapped to
     * @return An instance of the model class with fields populated fom the {@link ResultSet}
     * @throws SQLException If there is an error accessing the {@link ResultSet}
     * @throws IllegalAccessException If the field in the model class cannot be accessed
     * @throws InstantiationException If the model class cannot be instantiated
     * @throws NoSuchMethodException If the no-argument constructor of the model class is not found
     * @throws InvocationTargetException If there is an exception thrown by the constructor
     */
    public static <T extends IModel> T mapToModel(ResultSet resultSet, Class<T> modelClass)
            throws SQLException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {

        T model = modelClass.getDeclaredConstructor().newInstance();
        Field[] fields = modelClass.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            // If the field is the constructor we continue to the next in the loop
            if (fieldName.equals("<init>")) {
                continue;
            }

            field.setAccessible(true);
            Annotation[] annotations = field.getDeclaredAnnotations();

            if (annotations.length == 0)
                continue;

            for (Annotation annotation : annotations) {
                FieldMappingStrategy strategy = MAPPING_STRATEGIES.get(annotation.annotationType());

                if (strategy != null) {
                    strategy.mapField(resultSet, model, field);
                }
            }
        }

        return model;
    }

    /**
     * Retrieves the fields of a given model class.
     *
     * @param model The class object representing the model class.
     * @return A list of {@link Field} objects representing the fields of the model class.
     */
    public static List<Field> getFieldsOfModel(Class<? extends IModel> model) {
        return List.of(model.getDeclaredFields());
    }

    /**
     * Retrieves the fields of a given model class without the specified annotations.
     *
     * @param model The object representing the model class.
     * @param annotations A list of annotations to ignore.
     * @return A list of {@link Field} objects representing the fields of the model class.
     */
    public static List<Field> getFieldsOfModelWithoutTypes(Class<? extends IModel> model, List<Class<? extends Annotation>> annotations) {
        return getFieldsOfModel(model)
                .stream()
                .filter(field -> annotations.stream().noneMatch(field::isAnnotationPresent))
                .toList();
    }

    /**
     * Retrieves the fields of a given model class with only the specified annotations.
     *
     * @param model The object representing the model class.
     * @param annotations A list of annotations to ignore.
     * @return A list of {@link Field} objects representing the fields of the model class.
     */
    public static List<Field> getFieldsOfModelWithTypes(Class<? extends IModel> model, List<Class<? extends Annotation>> annotations) {
        return getFieldsOfModel(model)
                .stream()
                .filter(field -> annotations.stream().anyMatch(field::isAnnotationPresent))
                .toList();
    }

    /**
     * Retrieves the value of a specified field from an instance of a model.
     *
     * @param model The instance of the {@link IModel} from which the field value will be retrieved
     * @param fieldName The name of the field to retrieve
     * @return The value of the specified field in the given model instance
     * @throws NoSuchFieldException If the field with the specified name is not found in the model
     * @throws IllegalAccessException if the field cannot be accessed (e.g., if it's private)
     */
    public static Object getValueOfField(IModel model, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = getFieldByName(model.getClass(), fieldName);
        field.trySetAccessible();
        return field.get(model);
    }

    /**
     * Retrieves a {@link Field} from the specified model class by its name
     *
     * @param model The {@link Class} of the model from which to retrieve the field. Must implement {@link IModel}
     * @param fieldName The name of the field to retrieve
     * @return The {@link Field} object corresponding to the specified name
     * @throws NoSuchFieldException If a field with the specified name is not found in the class
     */
    public static Field getFieldByName(Class<? extends IModel> model, String fieldName) throws NoSuchFieldException {
        Field field = model.getDeclaredField(fieldName);
        field.trySetAccessible();
        return field;
    }

    /**
     * Returns the database column name corresponding to the given {@link Field}
     *
     * @param field The {@link Field} for which the database column name s to be determined
     * @return The database column name as a {@link String}
     */
    public static String getFieldName(Field field) {
        return RELATION_ANNOTATIONS
                .stream()
                .anyMatch(field::isAnnotationPresent)
                    ? field.getName() + "_id"
                    : field.getName();
    }

    public static Class<?> getFieldType(Class<?> modelClass, String fieldName) throws NoSuchFieldException {
        Field field = modelClass.getDeclaredField(fieldName);
        field.setAccessible(true);

        if (List.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();

            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> classObj) {
                    return classObj;
                }
            }
        }

        return field.getType();
    }
}
