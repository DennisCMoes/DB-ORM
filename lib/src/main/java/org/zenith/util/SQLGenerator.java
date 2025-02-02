package org.zenith.util;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToMany;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.model.interfaces.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A utility class used by the EntityMapper to generate SQL queries based on entity mappings.
 * It would handle tasks such as generating INSERT, SELECT, UPDATE, and DELETE queries based on entity metadata.
 */
public class SQLGenerator {
    private SQLGenerator() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    /**
     * Generates an SQL query to create a table for a given model class
     *
     * @param model The class representing the model for which the table is generated
     * @return A string containing the SQL query to create the table
     * @throws IllegalArgumentException If the model does not contain the annotated field
     */
    private static List<String> generateCreateTable(Class<? extends IModel> model)
            throws IllegalArgumentException {

        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = ReflectionUtil.getFieldsOfModel(model);

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("The model must contain fields with annotations");
        }

        queryBuilder.append(String.format("CREATE TABLE %s (", model.getSimpleName().toLowerCase()));
        List<String> conditions = new ArrayList<>();

        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldTableName = field.getType().getSimpleName().toLowerCase();

            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            switch (annotationOfField) {
                case Id ignored -> conditions.add(String.format("%s INTEGER PRIMARY KEY AUTOINCREMENT", fieldName));
                case OneToOne ignored -> {
                    conditions.add(String.format("%s INT", fieldName + "_id"));
                    conditions.add(String.format("FOREIGN KEY (%s_id) REFERENCES %s(id)", fieldName, fieldTableName));
                }
                case ManyToOne ignored -> { // Child
                    conditions.add(String.format("%s INT", fieldName + "_id"));
                    conditions.add(String.format("FOREIGN KEY (%s_id) REFERENCES %s(id)", fieldName, fieldTableName));
                }
                case OneToMany ignored -> { } // Parent
                case Column column -> {
                    switch (column.type()) {
                        case BOOLEAN, INTEGER -> conditions.add(String.format("%s INTEGER", fieldName));
                        case TEXT, DATETIME -> conditions.add(String.format("%s TEXT", fieldName));
                        case VARCHAR -> conditions.add(String.format("%s %s (%d)", fieldName, column.type(), column.size()));
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + annotationOfField);
            }
        }

        queryBuilder.append(String.join(", ", conditions)).append(");");

        List<String> queries = new ArrayList<>();
        queries.add(queryBuilder.toString());

        queries.forEach(Logger::query);
        return queries;
    }

    /**
     * Generates SQL queries to create tables for a list of model classes
     *
     * @param models The list of model classes for which the tables are created
     * @return A string containing the SQL queries to create the tables
     * @throws IllegalArgumentException If the list of classes is null or empty
     */
    public static List<String> generateCreateTable(List<Class<? extends IModel>> models)
            throws IllegalArgumentException {

        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("The list of classes must cannot be null or empty.");
        }

        return models.stream()
                .map(SQLGenerator::generateCreateTable)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Generates an SQL INSERT query for the given model instance
     *
     * @param model The instance of the model to be inserted into the database
     * @return A string containing the SQL INSERT query
     * @throws IllegalArgumentException If the model does not contain any annotated field
     * @throws NoSuchFieldException If a required field is not found
     * @throws IllegalAccessException If a field cannot be accessed
     */
    public static List<String> generateInsert(IModel model)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = ReflectionUtil.getFieldsOfModelWithoutTypes(model.getClass(), List.of(OneToMany.class));

        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("The model must contain annotated fields");
        }

        String tableName = model.getClass().getSimpleName().toLowerCase();
        String tableCols = String.join(", ", fields.stream()
                .filter(field -> {
                    try {
                        Annotation annotation = field.getDeclaredAnnotations()[0];
                        if (annotation instanceof Id) {
                            Object idValue = ReflectionUtil.getValueOfField(model, field.getName());
                            return idValue != null && !(idValue instanceof Integer && (int) idValue == 0);
                        }
                        return true;
                    } catch (NoSuchFieldException | IllegalAccessException ex) {
                        ex.printStackTrace();
                        return true;
                    }
                })
                .map(ReflectionUtil::getFieldName)
                .toList());

        queryBuilder.append(String.format("INSERT INTO %s (%s) VALUES (", tableName, tableCols));
        List<String> fieldsToQueryString = new ArrayList<>();

        for (Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue = ReflectionUtil.getValueOfField(model, fieldName);

            if (fieldValue == null) {
                fieldsToQueryString.add("NULL");
                continue;
            }

            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            if (annotationOfField instanceof Column column) {
                switch (column.type()) {
                    case VARCHAR, TEXT -> fieldsToQueryString.add(String.format("'%s'", fieldValue));
                    case INTEGER -> fieldsToQueryString.add(String.format("%s", fieldValue));
                    case BOOLEAN -> fieldsToQueryString.add(String.format("%d", !(fieldValue instanceof Boolean) ? 0 : ((boolean) fieldValue) ? 1 : 0));
                    case DATETIME -> fieldsToQueryString.add(String.format("datetime(%d, 'unixepoch')", ((Date)fieldValue).getTime() / 1000));
                }
            } else if (annotationOfField instanceof Id) {
                if ((int)fieldValue == 0)
                    continue;

                fieldsToQueryString.add(String.format("%d", (int)fieldValue));
            } else if (annotationOfField instanceof OneToOne || annotationOfField instanceof ManyToOne) { // ManyToOne -> Child
                Field relatedField = ReflectionUtil.getFieldByName((Class<? extends IModel>) field.getType(), "id");
                Object relatedIdValue = relatedField.get(fieldValue);

                fieldsToQueryString.add(String.format("%d", (int)relatedIdValue));
            }
        }

        queryBuilder.append(String.join(", ", fieldsToQueryString));
        queryBuilder.append(") RETURNING *;");

        List<String> result = new ArrayList<>(List.of(queryBuilder.toString()));
        result.forEach(Logger::query);

        return result;
    }

    /**
     * Generates an SQL SELECT query for the given model instance with specified fields to return and query
     *
     * @param modelClass The model instance for which the SELECT query is generated
     * @param fieldsToReturn The list of fields to include in the SELECT clause. If null or empty, all fields are included
     * @param fieldsToQuery The list of fields to include in the WHERE clause
     * @return A string containing the SQL SELECT query
     * @throws NoSuchFieldException If a specified field is not found
     * @throws IllegalArgumentException If provided model does not have the {@link Entity} annotation
     */
    public static String generateSelect(Class<? extends IModel> modelClass, List<String> fieldsToReturn, Map<String, Object> fieldsToQuery)
            throws NoSuchFieldException, IllegalArgumentException {

        if (!modelClass.isAnnotationPresent(Entity.class))
            throw new IllegalArgumentException(String.format("%s does not have the @Entity annotation", modelClass.getSimpleName()));

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");

        if (fieldsToReturn == null || fieldsToReturn.isEmpty()) {
            queryBuilder.append("*");
        } else {
            queryBuilder.append(String.join(", ", fieldsToReturn));
        }

        // FROM clause
        queryBuilder.append(String.format(" FROM %s", modelClass.getSimpleName().toLowerCase()));

        // Add WHERE clause
        if (fieldsToQuery != null && !fieldsToQuery.isEmpty()) {
            queryBuilder.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            for (Map.Entry<String, Object> entry : fieldsToQuery.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();

                Field field = modelClass.getDeclaredField(fieldName);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);

                    switch (column.type()) {
                        case VARCHAR, TEXT -> conditions.add(String.format("%s='%s'", fieldName, fieldValue));
                        case INTEGER, BOOLEAN -> conditions.add(String.format("%s=%s", fieldName, fieldValue));
                        case DATETIME -> conditions.add(String.format("%s=datetime('%s')", fieldName, ((Date)fieldValue).getTime()));
                    }
                } else if (field.isAnnotationPresent(Id.class)) {
                    conditions.add(String.format("%s=%s", fieldName, (int) fieldValue));
                } else if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
                    conditions.add(String.format("%s=%d", fieldName + "_id", (int)fieldValue));
                }
            }

            queryBuilder.append(String.join(" AND ", conditions));
        }

        queryBuilder.append(";");

        String query = queryBuilder.toString();
        Logger.query(query);

        return query;
    }

    /**
     * Generates a SQL SELECT COUNT(*) query for a given model class, with optional filtering conditions
     * The generated query counts the number of rows in the table corresponding to the given model class
     *
     * @param modelClass The class of the model, which extends {@link IModel}. The table name is derived from the clas name
     * @param fieldsToQuery A map of field names and their corresponding values to be used as filtering conditions in the WHERE clause
     * @return A valid SQL query string that counts the number of records based on the provided conditions
     * @throws NoSuchFieldException If a field in {@code fieldsToQuery} does not exist in the model class
     */
    public static String generateCountSelect(Class<? extends IModel> modelClass, Map<String, Object> fieldsToQuery)
            throws NoSuchFieldException {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("SELECT COUNT(*) FROM %s", modelClass.getSimpleName().toLowerCase()));

        // WHERE clause
        if (fieldsToQuery != null && !fieldsToQuery.isEmpty()) {
            queryBuilder.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            for (Map.Entry<String, Object> entry : fieldsToQuery.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();

                Field field = modelClass.getDeclaredField(fieldName);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);

                    switch (column.type()) {
                        case VARCHAR, TEXT -> conditions.add(String.format("%s='%s'", fieldName, fieldValue));
                        case INTEGER, BOOLEAN -> conditions.add(String.format("%s=%s", fieldName, fieldValue));
                        case DATETIME -> conditions.add(String.format("%s=datetime('%s')", fieldName, ((Date)fieldValue).getTime()));
                    }
                } else if (field.isAnnotationPresent(Id.class)) {
                    conditions.add(String.format("%s=%s", fieldName, (int) fieldValue));
                } else if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
                    conditions.add(String.format("%s=%d", fieldName + "_id", (int)fieldValue));
                }
            }

            queryBuilder.append(String.join(" AND ", conditions));
        }

        queryBuilder.append(";");

        String query = queryBuilder.toString();
        Logger.query(query);

        return query;
    }

    /**
     * Generates an SQL UPDATE query for the given model instance
     *
     * @param model The instance of the model to be updated
     * @return A string containing the SQL UPDATE query
     * @throws NoSuchFieldException If a required field is not found
     * @throws IllegalAccessException If a field cannot be accessed
     */
    public static String generateUpdate(IModel model)
            throws NoSuchFieldException, IllegalAccessException {

        StringBuilder queryBuilder = new StringBuilder();
        String tableName = model.getClass().getSimpleName().toLowerCase();

        queryBuilder.append(String.format("UPDATE %s SET ", tableName));
        List<Field> fields = ReflectionUtil.getFieldsOfModelWithoutTypes(model.getClass(), List.of(Id.class));
        List<String> fieldsToUpdate = new ArrayList<>();

        for (Field field : fields) {
            Object fieldValue = ReflectionUtil.getValueOfField(model, field.getName());
            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            if (fieldValue == null)
                continue;

            if (annotationOfField instanceof Column column) {
                switch (column.type()) {
                    case VARCHAR, TEXT -> fieldsToUpdate.add(String.format("%s='%s'", field.getName(), fieldValue));
                    case INTEGER -> fieldsToUpdate.add(String.format("%s=%s", field.getName(), fieldValue));
                    case BOOLEAN -> fieldsToUpdate.add(String.format("%s=%d", field.getName(), !(fieldValue instanceof Boolean) ? 0 : ((boolean) fieldValue) ? 1 : 0));
                    case DATETIME -> fieldsToUpdate.add(String.format("%s=datetime('%s')", field.getName(), ((Date)fieldValue).getTime()));
                }
            } else if (annotationOfField instanceof OneToOne || annotationOfField instanceof ManyToOne) {
                Field relatedField = ReflectionUtil.getFieldByName((Class<? extends IModel>) field.getType(), "id");
                Object relatedIdValue = relatedField.get(fieldValue);

                fieldsToUpdate.add(String.format("%s=%s", field.getName() + "_id", relatedIdValue));
            }
        }

        queryBuilder.append(String.join(", ", fieldsToUpdate));

        Object idValue = ReflectionUtil.getValueOfField(model, "id");
        queryBuilder.append(String.format(" WHERE id=%s RETURNING *;", idValue));

        String query = queryBuilder.toString();
        Logger.query(query);

        return query;
    }

    /**
     * Generates an SQL DELETE query for the given model instance
     *
     * @param model The instance of the model to be deleted
     * @return A string containing the SQL DELETE query
     * @throws NoSuchFieldException If the "id" field is not found
     * @throws IllegalAccessException If the "id" field cannot be accessed
     */
    public static List<String> generateDelete(IModel model)
            throws NoSuchFieldException, IllegalAccessException {

        String deleteQueryTemplate = "DELETE FROM %s WHERE %s=%d RETURNING *;";
        String tableName = model.getClass().getSimpleName().toLowerCase();
        Object idValue = ReflectionUtil.getValueOfField(model, "id");

        if ((!(idValue instanceof Integer id))) {
            throw new IllegalArgumentException("The model must have a valid integer ID");
        }

        List<Field> fields = ReflectionUtil.getFieldsOfModelWithTypes(model.getClass(), List.of(OneToMany.class));
        List<String> queries = new ArrayList<>();

        queries.add(String.format(deleteQueryTemplate, tableName, "id", id));

        for (Field field : fields) {
            if (!List.class.isAssignableFrom(field.getType()))
                continue;

            Object fieldValue = ReflectionUtil.getValueOfField(model, field.getName());
            if (!(fieldValue instanceof List<?> list) || list.isEmpty())
                continue;

            Type genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType parameterizedType))
                continue;

            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            if (!(actualTypeArgument instanceof Class<?> actualClass))
                continue;

            String simpleName = ((Class<? extends IModel>) actualTypeArgument).getSimpleName();
            String subTableName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
            String parentTableColumn = Character.toLowerCase(tableName.charAt(0)) + tableName.substring(1) + "_id";

            queries.add(String.format(deleteQueryTemplate, subTableName, parentTableColumn, id));
        }

        queries.forEach(Logger::query);
        return queries;
    }
}
