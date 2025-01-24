package org.zenith.util.reflection;

import org.zenith.annotation.Entity;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.model.interfaces.IModel;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReflectionUtil {
    /**
     * Attempt to load the class by the classname
     *
     * @param classname The name of the class to be processed.
     * @return The Class object corresponding to the given classname, or null if the class is not found.
     */
    private Class<? extends IModel> processClass(String classname) {
        try {
            @SuppressWarnings("unchecked") Class<? extends IModel> model = (Class<? extends IModel>) Class.forName("org.zenith.model." + classname.replace(".java", ""));

            return model;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    /**
     * Retrieves a list of database model class annotated with the {@link Entity} annotation.
     *
     * @return A list of Class objects representing database model classes.
     */
    public List<Class<? extends IModel>> getDbModels() {
        List<Class<? extends IModel>> classes = new ArrayList<>();

        String path = System.getProperty("user.dir") + "/lib/src/main/java/org/zenith/model";
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) continue;

                Class<? extends IModel> classObj = processClass(file.getName());

                // Make sure that the model has the Entity annotation
                if (classObj != null && classObj.isAnnotationPresent(Entity.class)) {
                    classes.add(classObj);
                }
            }
        }

        return classes;
    }

    /**
     * Retrieves the fields of a given model class.
     *
     * @param model The class object representing the model class.
     * @return A list of {@link Field} objects representing the fields of the model class.
     */
    public List<Field> getFieldsOfModel(Class<? extends IModel> model) {
        return Arrays.stream(model.getFields()).toList();
    }

    /**
     * Retrieves the fields of a given model class without the specified annotations.
     *
     * @param model The object representing the model class.
     * @param annotations A list of annotations to ignore.
     * @return A list of {@link Field} objects representing the fields of the model class.
     */
    public List<Field> getFieldsOfModelWithoutTypes(Class<? extends IModel> model, List<Class<? extends Annotation>> annotations) {
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
    public List<Field> getFieldsOfModelWithTypes(Class<? extends IModel> model, List<Class<? extends Annotation>> annotations) {
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
    public Object getValueOfField(IModel model, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return getFieldByName(model.getClass(), fieldName).get(model);
    }

    public Field getFieldByName(Class<? extends IModel> model, String fieldName) throws NoSuchFieldException {
        return model.getDeclaredField(fieldName);
    }

    public String getFieldName(Field field) {
        String fieldName = field.getName();

        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
            fieldName += "_id";
        }

        return fieldName;
    }
}