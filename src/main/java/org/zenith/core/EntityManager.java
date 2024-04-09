package org.zenith.core;

import org.zenith.mapper.SQLGenerator;
import org.zenith.model.UserTable;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.DatabaseUtil;
import org.zenith.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Manages entity lifecycle and database operations.
 * It would provide methods for CRUD operations (e.g., save, find, update, delete) and transaction management.
 */
public class EntityManager {
    private final DatabaseUtil databaseUtil;

    public EntityManager() {
        this.databaseUtil = DatabaseUtil.getInstance();
    }

    public void createTable(String tableName, List<Field> fields) {
        String query = SQLGenerator.generateCreateTable(tableName, fields);
        databaseUtil.queryDb(query);
    }

    public void dropTable(String tableName) {
        String query = SQLGenerator.generateDropTable(List.of(tableName));
        databaseUtil.queryDb(query);
    }

    public void saveEntity(IModel model) {
        String query = SQLGenerator.generateInsert(model);
        System.out.println(query);
        databaseUtil.queryDb(query);
    }
}
