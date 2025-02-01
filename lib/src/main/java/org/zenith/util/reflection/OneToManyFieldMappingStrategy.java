package org.zenith.util.reflection;

import org.zenith.model.interfaces.IModel;
import org.zenith.util.ReflectionUtil;
import org.zenith.util.SQLGenerator;
import org.zenith.util.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OneToManyFieldMappingStrategy extends MappingBase implements FieldMappingStrategy {
    @Override
    public void mapField(ResultSet resultSet, IModel model, Field field)
            throws SQLException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {

        try {
            if (!List.class.isAssignableFrom(field.getType()))
                return;

            Type genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType parameterizedType))
                return;

            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            if (!(actualTypeArgument instanceof Class<?> actualClass))
                return;

            ReflectionUtil reflectionUtil = new ReflectionUtil();
            int id = (int)reflectionUtil.getValueOfField(model, "id");
            String relatedFieldName = getRelatedFieldName(model);

            List<IModel> linkedSubClasses = fetchRelatedModels(actualClass, relatedFieldName, id);
            field.set(model, linkedSubClasses);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
    }

    private String getRelatedFieldName(IModel model) {
        String className = model.getClass().getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private List<IModel> fetchRelatedModels(Class<?> actualClass, String relatedFieldName, int id)
            throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, InstantiationException {

        String countQuery = SQLGenerator.generateCountSelect(castToIModelClass(actualClass), Map.of(relatedFieldName, id));

        try (ResultSet countResultSet = SQLiteDatabase.getInstance().executeQueryWithResult(countQuery)) {
            if (!countResultSet.next() || countResultSet.getInt("COUNT(*)") == 0)
                return new ArrayList<>();

            String selectQuery = SQLGenerator.generateSelect(castToIModelClass(actualClass), null, Map.of(relatedFieldName, id));

            try (ResultSet relatedModelsResultSet = SQLiteDatabase.getInstance().executeQueryWithResult(selectQuery)) {
                return extractModelsFromResultSet(relatedModelsResultSet, actualClass);
            }
        }
    }

    private List<IModel> extractModelsFromResultSet(ResultSet resultSet, Class<?> modelClass)
            throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, InstantiationException {

        List<IModel> models = new ArrayList<>();

        while (resultSet.next()) {
            models.add(ReflectionUtil.mapToModel(resultSet, castToIModelClass(modelClass)));
        }

        return models;
    }
}
