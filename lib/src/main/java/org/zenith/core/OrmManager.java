package org.zenith.core;

import org.zenith.model.ToDoItem;
import org.zenith.model.ToDoList;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.ReflectionUtil;

import java.net.ConnectException;
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

        entityManager.createTables(classes);
        entityManager.alterTables(classes);
    }

    public void clearDatabase() {
        reflectionUtil.getDbModels()
                .stream()
                .map(Class::getSimpleName)
                .forEach(entityManager::dropTable);
    }

    public void migrateToDatabase() {
        ToDoList toDoList = entityManager.saveEntity(new ToDoList("List 1"), ToDoList.class);

        ToDoItem toDoItem1 = entityManager.saveEntity(new ToDoItem("Item 1", "Description", toDoList), ToDoItem.class);
        ToDoItem toDoItem2 = entityManager.saveEntity(new ToDoItem("Item 2", "Description", toDoList), ToDoItem.class);
        ToDoItem toDoItem3 = entityManager.saveEntity(new ToDoItem("Item 3", "Description", toDoList), ToDoItem.class);
    }
}
