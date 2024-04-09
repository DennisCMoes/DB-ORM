package org.zenith.util;

import org.zenith.annotation.Entity;
import org.zenith.model.interfaces.IModel;

import java.io.File;
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
    private Class<?> processClass(String classname) {
        try {
            return Class.forName("org.zenith.model." + classname.replace(".java", ""));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
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

        String path = System.getProperty("user.dir") + "/src/main/java/org/zenith/model";
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory())
                    continue;

                Class<? extends IModel> classObj = (Class<? extends IModel>) processClass(file.getName());

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
     * @param classObj The class object representing the model class.
     * @return A list of {@link Field} objects representing the fields of the model class.
     */
    public List<Field> getFieldsOfModel(Class<?> classObj) {
        return Arrays.stream(classObj.getFields()).toList();
    }
}
