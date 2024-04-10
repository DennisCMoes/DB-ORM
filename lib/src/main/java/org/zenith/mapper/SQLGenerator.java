package org.zenith.mapper;

import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class used by the EntityMapper to generate SQL queries based on entity mappings.
 * It would handle tasks such as generating INSERT, SELECT, UPDATE, and DELETE queries based on entity metadata.
 */
public class SQLGenerator {
    private static Object getFieldValue(IModel model, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = model.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(model);
    }

    private static List<Field> getFieldsWithoutId(IModel model) {
        ReflectionUtil reflectionUtil = new ReflectionUtil();

        return reflectionUtil.getFieldsOfModel(model.getClass())
                .stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .toList();
    }

    private static String getFieldName(Field field) {
        String fieldName = field.getName();

        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class))
            fieldName += "_id";

        return fieldName;
    }

    public static String generateCreateTable(List<Class<? extends IModel>> classes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Class<? extends IModel> classObj : classes) {
            stringBuilder.append(generateCreateTable(classObj.getSimpleName(), Arrays.stream(classObj.getFields()).toList())).append("\n");
        }

        return stringBuilder.toString();
    }

    private static String generateCreateTable(String tableName, List<Field> fields) {
        StringBuilder queryBuilder = new StringBuilder();

        // Set the name of the table
        queryBuilder.append(String.format("CREATE TABLE %s (", tableName));

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String fieldName = field.getName();

            // TODO: Make this work with multiple annotations
            Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

            if (fieldAnnotation instanceof Id) {
                queryBuilder.append(String.format("%s SERIAL PRIMARY KEY", fieldName));
            } else if (fieldAnnotation instanceof Column column) {
                if (column.type().equals(ColumnType.TEXT)) {
                    queryBuilder.append(String.format("%s %s", fieldName, column.type()));
                } else {
                    queryBuilder.append(String.format("%s %s (%d)", fieldName, column.type(), column.size()));
                }
            } else if (fieldAnnotation instanceof OneToOne || fieldAnnotation instanceof ManyToOne) {
                String foreignKeyName = fieldName + "_id";
                String referencedClassName = field.getType().getSimpleName();

                queryBuilder.append(String.format("%s INT", foreignKeyName));
                // TODO: If OneToOne add unique field
                // TODO: Make (id) dynamic so that it will get the field with the @Id annotation instead of
            }

            if (i + 1 < fields.size())
                queryBuilder.append(", ");
        }

        queryBuilder.append(");");

        return queryBuilder.toString();
    }

    public static String generateAddForeignKey(List<Class<? extends IModel>> classes) {
        StringBuilder queryBuilder = new StringBuilder();

        for (Class<? extends IModel> classObj : classes) {
            queryBuilder.append(generateAddForeignKey(classObj.getSimpleName().toLowerCase(), Arrays.stream(classObj.getFields()).toList())).append("\n");
        }

        return queryBuilder.toString();
    }

    private static String generateAddForeignKey(String tableName, List<Field> fields) {
        StringBuilder queryBuilder = new StringBuilder();

        for (Field field : fields) {
            // TODO: Make this work with multiple annotations
            Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

            if (fieldAnnotation instanceof OneToOne || fieldAnnotation instanceof ManyToOne) {
                String fieldName = field.getName() + "_id";
                String referencedClassName = field.getType().getSimpleName().toLowerCase();

                // ALTER TABLE message ADD FOREIGN KEY (sender) REFERENCES users;
                queryBuilder.append(String.format("ALTER TABLE %s ADD FOREIGN KEY (%s) REFERENCES %s;", tableName, fieldName, referencedClassName));
            }
        }

        if (!queryBuilder.isEmpty())
            queryBuilder.append("\n");

        return queryBuilder.toString();
    }

    public static String generateDropTable(List<String> tables) {
        return String.format("DROP TABLE IF EXISTS %s CASCADE;", String.join(",", tables));
    }

    public static String generateInsert(IModel model) {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            List<Field> fields = SQLGenerator.getFieldsWithoutId(model);

            String tableName = model.getClass().getSimpleName().toLowerCase();
            String tableCols = String.join(", ", fields.stream().map(SQLGenerator::getFieldName).toList());

            queryBuilder.append(String.format("INSERT INTO %s (%s) VALUES (", tableName, tableCols));

            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);

                String fieldName = field.getName();
                Object fieldValue = SQLGenerator.getFieldValue(model, fieldName);

                // TODO: Make this work with multiple annotations
                Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

                if (fieldAnnotation instanceof Column column) {
                    switch (column.type()) {
                        case VARCHAR, TEXT -> queryBuilder.append(String.format("'%s'", fieldValue));
                        case INTEGER, BOOLEAN -> queryBuilder.append(String.format("%s", fieldValue));
                    }
                } else if (fieldAnnotation instanceof OneToOne || fieldAnnotation instanceof ManyToOne) {
                    IModel linkedObj = (IModel) field.get(model);
                    Object idValue = SQLGenerator.getFieldValue(linkedObj, "id");
                    queryBuilder.append(idValue);
                }

                if (i + 1 < fields.size()) {
                    queryBuilder.append(", ");
                }
            }

            queryBuilder.append(") RETURNING *;");

            return queryBuilder.toString();
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String generateSelect(String tableName) {
        return String.format("SELECT * FROM %s;", tableName);
    }

    public static String generateSelect(IModel model) {
        try {
            String tableName = model.getClass().getSimpleName().toLowerCase();
            Integer idValue = (Integer) SQLGenerator.getFieldValue(model, "id");

            return String.format("SELECT * FROM %s WHERE id=%s;", tableName, idValue);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String generateUpdate(IModel model) {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            String tableName = model.getClass().getSimpleName().toLowerCase();

            queryBuilder.append(String.format("UPDATE %s SET ", tableName));

            List<Field> fields = SQLGenerator.getFieldsWithoutId(model);

            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                Object fieldValue = SQLGenerator.getFieldValue(model, field.getName());

                Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

                if (fieldAnnotation instanceof Column column) {
                    switch (column.type()) {
                        case VARCHAR, TEXT -> queryBuilder.append(String.format("%s='%s'", field.getName(), fieldValue));
                        case INTEGER, BOOLEAN -> queryBuilder.append(String.format("%s=%s", field.getName(), fieldValue));
                    }
                } else if (fieldAnnotation instanceof OneToOne || fieldAnnotation instanceof ManyToOne) {
                    String fieldName = field.getName() + "_id";
                    queryBuilder.append(String.format("%s=%s", fieldName, fieldValue));
                }

                if (i + 1 < fields.size()) {
                    queryBuilder.append(", ");
                }

                Object idValue = SQLGenerator.getFieldValue(model, "id");
                queryBuilder.append(String.format(" WHERE id=%s RETURNING *;", idValue));
            }

            return queryBuilder.toString();
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String generateDelete(IModel model) {
        try {
            String tableName = model.getClass().getSimpleName().toLowerCase();
            Integer id = (Integer) SQLGenerator.getFieldValue(model, "id");

            return String.format("DELETE FROM %s WHERE id=%d RETURNING *;", tableName, id);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
