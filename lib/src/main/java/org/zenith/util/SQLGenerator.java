package org.zenith.util;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToMany;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToMany;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.model.interfaces.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

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
    private static List<String> generateCreateTable(Class<? extends IModel> model) throws IllegalArgumentException {
        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = ReflectionUtil.getFieldsOfModel(model);

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("The model must contain fields with annotations");
        }

        queryBuilder.append(String.format("CREATE TABLE %s (", model.getSimpleName().toLowerCase()));
        List<String> conditions = new ArrayList<>();
        List<String> manyToManyTables = new ArrayList<>();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String fieldName = field.getName();
            String fieldTableName = field.getType().getSimpleName().toLowerCase();

            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            switch (annotationOfField) {
                case Id ignored -> conditions.add(String.format("%s SERIAL PRIMARY KEY", fieldName));
                case OneToOne ignored -> {
                    conditions.add(String.format("%s INT", fieldName + "_id"));
                    conditions.add(String.format("FOREIGN KEY (%s_id) REFERENCES %s(id)", fieldName, fieldTableName));
                }
                case ManyToOne ignored -> { // Child
                    conditions.add(String.format("%s INT", fieldName + "_id"));
                    conditions.add(String.format("FOREIGN KEY (%s_id) REFERENCES %s(id)", fieldName, fieldTableName));
                }
                case OneToMany ignored -> { } // Parent
                case ManyToMany ignored when List.class.isAssignableFrom(field.getType()) -> {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType parameterizedType) {
                        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                        if (actualTypeArgument instanceof Class<?> relatedModelClass) {
                            String relatedTableName = relatedModelClass.getSimpleName().toLowerCase();
                            String joinTableName = model.getSimpleName().toLowerCase() + "_" + relatedTableName;

                            String joinTableQuery = String.format(
                                    "CREATE TABLE %s (%s_id INT, %s_id INT, " +
                                            "FOREIGN KEY (%s_id) REFERENCES %s(id), " +
                                            "FOREIGN KEY (%s_id) REFERENCES %s(id));",
                                    joinTableName,
                                    model.getSimpleName().toLowerCase(),
                                    relatedTableName,
                                    model.getSimpleName().toLowerCase(),
                                    model.getSimpleName().toLowerCase(),
                                    relatedTableName,
                                    relatedTableName
                            );

                            manyToManyTables.add(joinTableQuery);
                        }
                    }

                    conditions.add(String.format("%s INTEGER", fieldName));
                }
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

        if (!manyToManyTables.isEmpty()) {
            queries.addAll(manyToManyTables);
        }

        queries.forEach(Logger::query);
        return queries;
    }

    /**
     * Generates an SQL query to drop a table for a given model class
     *
     * @param model The class representing the model for which the table is dropped
     * @return A string containing the SQL query to drop the table
     */
    private static String generateDropTable(Class<? extends IModel> model) {
        return String.format("DROP TABLE IF EXISTS %s CASCADE;", model.getSimpleName().toLowerCase());
    }

    /**
     * Generates SQL queries to create tables for a list of model classes
     *
     * @param models The list of model classes for which the tables are created
     * @return A string containing the SQL queries to create the tables
     * @throws IllegalArgumentException If the list of classes is null or empty
     */
    public static List<String> generateCreateTable(List<Class<? extends IModel>> models) throws IllegalArgumentException {
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("The list of classes must cannot be null or empty.");
        }

        if (models.size() == 1)
            return SQLGenerator.generateCreateTable(models.getFirst());

        return models.stream()
                .map(SQLGenerator::generateCreateTable)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Generates SQL queries to drop tables for a list of model classes
     *
     * @param models The list of model classes for which tables are dropped
     * @return A string containing the SQL queries to drop the tables
     * @throws IllegalArgumentException If the list of classes is null or empty
     */
    public static String generateDropTable(List<Class<? extends IModel>> models) throws IllegalArgumentException {
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("The list of classes must not be null or empty.");
        }

        return models.stream()
                .map(SQLGenerator::generateDropTable)
                .collect(Collectors.joining("\n"));
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
    public static List<String> generateInsert(IModel model) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
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
        Map<String, List<Integer>> manyToManyFields = new HashMap<>();

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
                    case BOOLEAN -> fieldsToQueryString.add(String.format("%d", ((boolean) fieldValue) ? 1 : 0));
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
            } else if (annotationOfField instanceof ManyToMany) {
                if (List.class.isAssignableFrom(field.getType())) {
                    List<?> relatedItems = (List<?>) fieldValue;
                    List<Integer> relatedIds = new ArrayList<>();

                    for (Object relatedItem : relatedItems) {
                        Field relatedField = ReflectionUtil.getFieldByName((Class<? extends IModel>) relatedItem.getClass(), "id");
                        Object relatedIdValue = relatedField.get(relatedItem);
                        relatedIds.add((Integer) relatedIdValue);
                    }

                    manyToManyFields.put(fieldName, relatedIds);
                    fieldsToQueryString.add("NULL");
                }
            }
        }

        queryBuilder.append(String.join(", ", fieldsToQueryString));
        queryBuilder.append(") RETURNING *;");

        List<String> manyToManyQueries = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : manyToManyFields.entrySet()) {
            String fieldName = entry.getKey();
            List<Integer> relatedIds = entry.getValue();

            Class<?> relatedClass = ReflectionUtil.getFieldType(model.getClass(), fieldName);
            String relatedTableName = relatedClass.getSimpleName().toLowerCase();
            String joinTableName = tableName + "_" + relatedTableName;

            for (Integer relatedId : relatedIds) {
                // INSERT INTO todoitem_categorie (todoitem_id, categorie_id) VALUES (3, 1)
                manyToManyQueries
                        .add(String.format("INSERT INTO %s (%s_id, %s_id) VALUES (%d, %d);",
                                joinTableName,
                                model.getClass().getSimpleName().toLowerCase(),
                                relatedTableName,
                                ReflectionUtil.getValueOfField(model, "id"),
                                relatedId));
            }
        }

        List<String> result = new ArrayList<>(List.of(queryBuilder.toString()));
        if (!manyToManyFields.isEmpty())
            result.addAll(manyToManyQueries);

        result.forEach(Logger::query);

        return result;
    }

    public static <T extends IModel> String generateManyToManySelect(T parentClass, Class<? extends IModel> childClass, List<String> fieldsToReturn)
            throws NoSuchFieldException, IllegalAccessException {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");

        if (fieldsToReturn == null || fieldsToReturn.isEmpty()) {
            queryBuilder.append("*");
        } else {
            queryBuilder.append(String.join(", ", fieldsToReturn));
        }

        String tableName = parentClass.getClass().getSimpleName().toLowerCase() + "_" + childClass.getSimpleName().toLowerCase();

        // FROM clause
        queryBuilder.append(String.format(" FROM %s", tableName));

        // WHERE clause
        // 2025-01-27 17:05:04 [QUERY] INSERT INTO todoitem_category (todoitem_id, category_id) VALUES (3, 1);
        // String countQuery = "SELECT * FROM todoitem_category WHERE todoitem_id = 1;";
        queryBuilder.append(String.format(" WHERE %s=%d", parentClass.getClass().getSimpleName().toLowerCase() + "_id", ReflectionUtil.getValueOfField(parentClass, "id")));
        return queryBuilder.toString();
    }

    /**
     * Generates an SQL SELECT query for the given model instance with specified fields to return and query
     *
     * @param modelClass The model instance for which the SELECT query is generated
     * @param fieldsToReturn The list of fields to include in the SELECT clause. If null or empty, all fields are included
     * @param fieldsToQuery The list of fields to include in the WHERE clause
     * @return A string containing the SQL SELECT query
     * @throws NoSuchFieldException If a specified field is not found
     * @throws IllegalAccessException If a field cannot be accessed
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

    // TODO: Make a generateManyToManyCountSelect just like the generateManyToManySelect
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
    public static String generateUpdate(IModel model) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        String tableName = model.getClass().getSimpleName().toLowerCase();

        queryBuilder.append(String.format("UPDATE %s SET ", tableName));
        List<Field> fields = ReflectionUtil.getFieldsOfModelWithoutTypes(model.getClass(), List.of(Id.class));
        List<String> fieldsToUpdate = new ArrayList<>();

        for (Field field : fields) {
            Object fieldValue = ReflectionUtil.getValueOfField(model, field.getName());
            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            if (annotationOfField instanceof Column column) {
                switch (column.type()) {
                    case VARCHAR, TEXT -> fieldsToUpdate.add(String.format("%s='%s'", field.getName(), fieldValue));
                    case INTEGER, BOOLEAN -> fieldsToUpdate.add(String.format("%s=%s", field.getName(), fieldValue));
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
    public static List<String> generateDelete(IModel model) throws NoSuchFieldException, IllegalAccessException {
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
