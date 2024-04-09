package org.zenith.mapper;

import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
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
    public static String generateCreateTable(List<Class<? extends IModel>> classes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Class<? extends IModel> classObj : classes) {
            String createQuery = SQLGenerator.generateCreateTable(classObj.getSimpleName(), Arrays.stream(classObj.getFields()).toList());
            stringBuilder.append(createQuery).append("\n");
        }

        return stringBuilder.toString();
    }

    public static String generateCreateTable(String name, List<Field> fields) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder alterTableBuilder = new StringBuilder();

        // Set the name of the table
        stringBuilder.append("CREATE TABLE ").append(name).append("(");

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            stringBuilder.append(field.getName());

            // TODO: Make this work with multiple annotations
            Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

            if (fieldAnnotation instanceof Id) {
                stringBuilder.append(" SERIAL PRIMARY KEY");
            } else if (fieldAnnotation instanceof Column column) {
                stringBuilder.append(String.format(" %s (%d)", column.type(), column.size()));
            } else if (fieldAnnotation instanceof OneToOne) {
                stringBuilder.append("_id INT");

                String referencedClassName = field.getType().getSimpleName();

                alterTableBuilder
                        .append(String.format("ALTER TABLE %s", name)).append("\n")
                        .append(String.format("ADD CONSTRAINT fk_%s_%s", referencedClassName, name)).append("\n")
                        .append(String.format("FOREIGN KEY (%s_id)", field.getName())).append("\n")
                        .append(String.format("REFERENCES %s(id)", referencedClassName)).append("\n")
                        .append(String.format("ON DELETE %s", "CASCADE"))
                        .append(";");
            }

            if (i + 1 < fields.size()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder
                .append(");")
                .append("\n\n")
                .append(alterTableBuilder);

        return stringBuilder.toString();
    }

    public static String generateDropTable(List<String> tables) {
        return String.format("DROP TABLE IF EXISTS %s CASCADE;", String.join(",", tables));
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
                // TODO: If OneToOne present, add _id behind it
                String.join(", ",
                        fields.stream().map(field -> {
                            String name = field.getName();

                            if (field.isAnnotationPresent(OneToOne.class))
                                name += "_id";

                            return name;
                        }).toList())));

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            // Ensure we can read the value of the field
            field.setAccessible(true);

            try {
                // TODO: Make this work with multiple annotations
                Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

                if (fieldAnnotation instanceof Id) {
                    continue;
                } else if (fieldAnnotation instanceof Column column) {
                    if (column.type().equals(ColumnType.VARCHAR)) {
                        stringBuilder.append(String.format("'%s'", field.get(model)));
                    } else if (column.type().equals(ColumnType.INTEGER)) {
                        stringBuilder.append(field.get(model));
                    }
                } else if (fieldAnnotation instanceof OneToOne) {
                    Object linkedObj = field.get(model);

                    Class<?> linkedClass = linkedObj.getClass();
                    Field idField = linkedClass.getDeclaredField("id");
                    idField.setAccessible(true);
                    Object id = idField.get(linkedObj);

                    stringBuilder.append(id);
                }
            } catch (IllegalAccessException | NoSuchFieldException ex) {
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

    public static String generateUpdate(IModel model) {
        ReflectionUtil reflectionUtil = new ReflectionUtil();

        try {
            StringBuilder stringBuilder = new StringBuilder();
            String tableName = model.getClass().getSimpleName().toLowerCase();

            stringBuilder.append(String.format("UPDATE %s SET ", tableName));

            List<Field> fields = reflectionUtil.getFieldsOfModel(model.getClass())
                    .stream()
                    .filter(field -> !field.isAnnotationPresent(Id.class))
                    .toList();

            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(model);

                // TODO: Make this work with multiple annotations
                Annotation fieldAnnotation = field.getDeclaredAnnotations()[0];

                if (fieldAnnotation instanceof Column) {
                    if (((Column) fieldAnnotation).type().equals(ColumnType.INTEGER)) {
                        stringBuilder.append(String.format("%s=%s", fieldName, fieldValue));
                    } else {
                        stringBuilder.append(String.format("%s='%s'", fieldName, fieldValue));
                    }
                }


                if (i + 1 < fields.size()) {
                    stringBuilder.append(", ");
                }
            }

            Field idField = model.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(model);

            stringBuilder.append(String.format(" WHERE id=%s;", idValue));

            return stringBuilder.toString();
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String generateDelete(IModel model) {
        try {
            String tableName = model.getClass().getSimpleName().toLowerCase();
            Field idField = model.getClass().getDeclaredField("id");
            idField.setAccessible(true);

            Object idValue = idField.get(model);

            return String.format("DELETE FROM %s WHERE id=%s;", tableName, idValue);
        } catch (Exception ex) {
          ex.printStackTrace();
          return null;
        }
    }
}
