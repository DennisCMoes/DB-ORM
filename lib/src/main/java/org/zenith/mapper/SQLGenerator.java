package org.zenith.mapper;

import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToMany;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class used by the EntityMapper to generate SQL queries based on entity mappings.
 * It would handle tasks such as generating INSERT, SELECT, UPDATE, and DELETE queries based on entity metadata.
 */
public class SQLGenerator {
    private static final ReflectionUtil reflectionUtil = new ReflectionUtil();

    /**
     * Generates an SQL query to add a foreign key constraint for a given model class.
     *
     * @param model The class representing the model for which foreign keys are generated.
     * @return A string containing the SQL query to add a foreign key constraint.
     */
    private static String generateAddForeignKey(Class<? extends IModel> model) {
        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = reflectionUtil.getFieldsOfModelWithTypes(model, List.of(OneToOne.class, ManyToOne.class));

        for (Field field : fields) {
            String fieldName = field.getName() + "_id";
            String referencedClassName = field.getType().getSimpleName().toLowerCase();

            queryBuilder.append(String.format("ALTER TABLE %s ADD FOREIGN KEY (%s) REFERENCES %s;", model.getSimpleName().toLowerCase(), fieldName, referencedClassName));
        }

        return queryBuilder.toString();
    }

    /**
     * Generates an SQL query to create a table for a given model class
     *
     * @param model The class representing the model for which the table is generated
     * @return A string containing the SQL query to create the table
     * @throws IllegalArgumentException If the model does not contain the annotated field
     */
    private static String generateCreateTable(Class<? extends IModel> model) throws IllegalArgumentException {
        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = reflectionUtil.getFieldsOfModelWithoutTypes(model, List.of(OneToMany.class));

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("The model must contain fields with annotations");
        }

        queryBuilder.append(String.format("CREATE TABLE %s (", model.getSimpleName().toLowerCase()));

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String fieldName = field.getName();

            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            switch (annotationOfField) {
                case Id ignored -> queryBuilder.append(String.format("%s SERIAL PRIMARY KEY", fieldName));
                case OneToOne ignored -> queryBuilder.append(String.format("%s INT", fieldName + "_id"));
                case ManyToOne ignored -> queryBuilder.append(String.format("%s INT", fieldName + "_id"));
                case Column column -> {
                    // TODO: Check if this should be annotated like this and we can't make one size fits all
                    if (column.type().equals(ColumnType.TEXT)) {
                        queryBuilder.append(String.format("%s %s", fieldName, column.type()));
                    } else {
                        queryBuilder.append(String.format("%s %s (%d)", fieldName, column.type(), column.size()));
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + annotationOfField);
            }

            if (i + 1 < fields.size()) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(");");
        return queryBuilder.toString();
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
    public static String generateCreateTable(List<Class<? extends IModel>> models) throws IllegalArgumentException {
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("The list of classes must cannot be null or empty.");
        }

        return models.stream()
                .map(SQLGenerator::generateCreateTable)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Generates SQL queries to add foreign key constraints for a list of model classes
     *
     * @param models The list of model classes for which foreign keys are generated
     * @return A string containing the SQL queries to add foreign key constraints
     * @throws IllegalArgumentException If the list of classes is null or empty
     */
    public static String generateAddForeignKey(List<Class<? extends IModel>> models) throws IllegalArgumentException {
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("The list of classes must cannot be null or empty");
        }

        return models.stream()
                .map(SQLGenerator::generateAddForeignKey)
                .collect(Collectors.joining("\n"));
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
    public static String generateInsert(IModel model) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = reflectionUtil.getFieldsOfModel(model.getClass());

        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("The model must contain annotated fields");
        }

        String tableName = model.getClass().getSimpleName().toLowerCase();
        String tableCols = String.join(", ", fields.stream().map(reflectionUtil::getFieldName).toList());

        queryBuilder.append(String.format("INSERT INTO %s (%s) VALUES (", tableName, tableCols));
        List<String> fieldsToQueryString = new ArrayList<>();

        for (Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue = reflectionUtil.getValueOfField(model, fieldName);

            if (fieldValue == null) {
                fieldsToQueryString.add("NULL");
                continue;
            }

            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            if (annotationOfField instanceof Column column) {
                switch (column.type()) {
                    case VARCHAR, TEXT -> fieldsToQueryString.add(String.format("'%s'", fieldValue));
                    case INTEGER, BOOLEAN -> fieldsToQueryString.add(String.format("%s", fieldValue));
                }
            } else if (annotationOfField instanceof Id) {
                fieldsToQueryString.add(String.format("%d", (int)fieldValue));
            } else if (annotationOfField instanceof OneToOne || annotationOfField instanceof ManyToOne) {
                Field relatedField = reflectionUtil.getFieldByName((Class<? extends IModel>) field.getType(), "id");
                Object relatedIdValue = relatedField.get(fieldValue);

                fieldsToQueryString.add(String.format("%d", (int)relatedIdValue));
            }
        }

        queryBuilder.append(String.join(", ", fieldsToQueryString));
        queryBuilder.append(") RETURNING *;");
        return queryBuilder.toString();
    }

    /**
     * Generates an SQL SELECT query for the given model instance with specified fields to return and query
     *
     * @param model The model instance for which the SELECT query is generated
     * @param fieldsToReturn The list of fields to include in the SELECT clause. If null or empty, all fields are included
     * @param fieldsToQuery The list of fields to include in the WHERE clause
     * @return A string containing the SQL SELECT query
     * @throws NoSuchFieldException If a specified field is not found
     * @throws IllegalAccessException If a field cannot be accessed
     */
    public static String generateSelect(IModel model, List<String> fieldsToReturn, List<String> fieldsToQuery) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");

        if (fieldsToReturn == null || fieldsToReturn.isEmpty()) {
            queryBuilder.append("*");
        } else {
            queryBuilder.append(fieldsToReturn.stream()
                    .collect(Collectors.joining(", ")));
        }

        // FROM clause
        queryBuilder.append(String.format(" FROM %s", model.getClass().getSimpleName().toLowerCase()));

        // Add WHERE clause
        if (fieldsToQuery != null && !fieldsToQuery.isEmpty()) {
            queryBuilder.append(" WHERE ");
            List<Field> fields = new ArrayList<>();

            for (String fieldName : fieldsToQuery) {
                fields.add(reflectionUtil.getFieldByName(model.getClass(), fieldName));
            }

            List<String> fieldsToQueryString = new ArrayList<>();

            for (Field field : fields) {
                Object fieldValue = reflectionUtil.getValueOfField(model, field.getName());
                Annotation annotationOfField = field.getDeclaredAnnotations()[0];

                if (annotationOfField instanceof Column column) {
                    switch (column.type()) {
                        case VARCHAR, TEXT -> fieldsToQueryString.add(String.format("%s='%s'", field.getName(), fieldValue));
                        case INTEGER, BOOLEAN -> fieldsToQueryString.add(String.format("%s=%s", field.getName(), fieldValue));
                    }
                } else if (annotationOfField instanceof Id id) {
                    fieldsToQueryString.add(String.format("%s=%d", field.getName(), (int)fieldValue));
                } else if (annotationOfField instanceof OneToOne || annotationOfField instanceof ManyToOne) {
                    fieldsToQueryString.add(String.format("%s=%d", field.getName() + "_id", (int)fieldValue));
                }
            }

            queryBuilder.append(String.join(" AND ", fieldsToQueryString));
        }

        queryBuilder.append(";");
        return queryBuilder.toString();
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
        List<Field> fields = reflectionUtil.getFieldsOfModelWithoutTypes(model.getClass(), List.of(Id.class));
        List<String> fieldsToUpdate = new ArrayList<>();

        for (Field field : fields) {
            Object fieldValue = reflectionUtil.getValueOfField(model, field.getName());
            Annotation annotationOfField = field.getDeclaredAnnotations()[0];

            if (annotationOfField instanceof Column column) {
                switch (column.type()) {
                    case VARCHAR, TEXT -> fieldsToUpdate.add(String.format("%s='%s'", field.getName(), fieldValue));
                    case INTEGER, BOOLEAN -> fieldsToUpdate.add(String.format("%s=%s", field.getName(), fieldValue));
                }
            } else if (annotationOfField instanceof OneToOne || annotationOfField instanceof ManyToOne) {
                Field relatedField = reflectionUtil.getFieldByName((Class<? extends IModel>) field.getType(), "id");
                Object relatedIdValue = relatedField.get(fieldValue);

                fieldsToUpdate.add(String.format("%s=%s", field.getName() + "_id", relatedIdValue));
            }
        }

        queryBuilder.append(String.join(", ", fieldsToUpdate));

        Object idValue = reflectionUtil.getValueOfField(model, "id");
        queryBuilder.append(String.format(" WHERE id=%s RETURNING *;", idValue));

        return queryBuilder.toString();
    }

    /**
     * Generates an SQL DELETE query for the given model instance
     *
     * @param model The instance of the model to be deleted
     * @return A string containing the SQL DELETE query
     * @throws NoSuchFieldException If the "id" field is not found
     * @throws IllegalAccessException If the "id" field cannot be accessed
     */
    public static String generateDelete(IModel model) throws NoSuchFieldException, IllegalAccessException {
        String tableName = model.getClass().getSimpleName().toLowerCase();
        Integer id = (int)reflectionUtil.getValueOfField(model, "id");
        return String.format("DELETE FROM %s WHERE id=%d RETURNING *;", tableName, id);
    }
}
