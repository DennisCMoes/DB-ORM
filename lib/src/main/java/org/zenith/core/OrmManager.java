package org.zenith.core;

import org.zenith.model.interfaces.IModel;
import org.zenith.util.ReflectionUtil;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;

/**
 * A central class responsible for initializing and configuring the ORM framework.
 * It might handle tasks such as setting up database connections,
 * scanning for entity classes, and initializing the entity manager.
 */
public class OrmManager {
    private final EntityManager entityManager;
    private final ReflectionUtil reflectionUtil;

    public OrmManager() throws ConnectException {
        this.entityManager = new EntityManager();
        this.reflectionUtil = new ReflectionUtil();
    }

    public void initializeDatabase() {
        List<Class<? extends IModel>> classes = reflectionUtil.getDbModels();
//        classes.forEach(classObj -> entityManager.createTable(classObj.getSimpleName(), reflectionUtil.getFieldsOfModel(classObj)));
        entityManager.createTables(classes);
        entityManager.alterTables(classes);
    }

    public void clearDatabase() {
        List<Class<? extends IModel>> classes = reflectionUtil.getDbModels();
        classes.forEach(classObj -> entityManager.dropTable(classObj.getSimpleName()));
    }

    public void migrateToDatabase() {

    }
}
