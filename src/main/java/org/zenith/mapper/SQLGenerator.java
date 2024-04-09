package org.zenith.mapper;

import org.zenith.annotation.Column;
import org.zenith.annotation.Id;

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
                stringBuilder
                        .append(" INTEGER ")
                        .append("PRIMARY KEY");
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
}
