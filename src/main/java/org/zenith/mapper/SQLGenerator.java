package org.zenith.mapper;

import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * A utility class used by the EntityMapper to generate SQL queries based on entity mappings.
 * It would handle tasks such as generating INSERT, SELECT, UPDATE, and DELETE queries based on entity metadata.
 */
public class SQLGenerator {
    public static String generateCreateTable(String name, List<Field> fields) {
        StringBuilder stringBuilder = new StringBuilder();

        // Set the name of the table
        stringBuilder.append("CREATE TABLE ").append(name).append("(");

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            stringBuilder.append(field.getName());

            // TODO: Make this work with multiple annotations
            Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

            if (fieldAnnotation instanceof Id) {
                stringBuilder.append(" SERIAL PRIMARY KEY");
            } else if (fieldAnnotation instanceof Column) {
                stringBuilder
                        .append(" ")
                        .append(((Column) fieldAnnotation).type())
                        .append(String.format("(%d)", ((Column) fieldAnnotation).size()));
            }

            if (i + 1 < fields.size()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(");");

        return stringBuilder.toString();
    }

    public static String generateDropTable(List<String> tables) {
        return String.format("DROP TABLE IF EXISTS %s;", String.join(",", tables));
    }

    public static String generateInsert(IModel model) {
        ReflectionUtil reflectionUtil = new ReflectionUtil();

        StringBuilder stringBuilder = new StringBuilder();
        List<Field> fields = reflectionUtil.getFieldsOfModel(model.getClass())
                .stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .toList();

        stringBuilder.append(String.format("INSERT INTO %s (%s) VALUES (",
                model.getClass().getSimpleName().toLowerCase(),
                String.join(", ", fields.stream().map(Field::getName).toList())));

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            // Ensure we can read the value of the field
            field.setAccessible(true);

            try {
                // TODO: Make this work with multiple annotations
                Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

                if (fieldAnnotation instanceof Id) {
                    continue;
                } else if (fieldAnnotation instanceof Column) {
                    if (((Column) fieldAnnotation).type().equals(ColumnType.VARCHAR)) {
                        stringBuilder.append("'").append(field.get(model)).append("'");
                    }
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }

            if (i + 1 < fields.size()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(");");

        return stringBuilder.toString();
    }

    public static String generateSelect(String tableName) {
        return String.format("SELECT * FROM %s;", tableName);
    }

    public static String generateSelect(IModel model) {
        try {
            String tableName = model.getClass().getSimpleName().toLowerCase();
            Field idField = model.getClass().getDeclaredField("id");
            idField.setAccessible(true);

            Object idValue = idField.get(model);

            return String.format("SELECT * FROM %s WHERE id=%s;",
                    tableName,
                    idValue);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
