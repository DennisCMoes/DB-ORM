package org.zenith.core;

import org.zenith.mapper.ResultSetMapper;
import org.zenith.mapper.SQLGenerator;
import org.zenith.model.LoginTable;
import org.zenith.model.UserTable;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.DatabaseUtil;
import org.zenith.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
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

    public void createTables(List<Class<? extends IModel>> classes) {
        String query = SQLGenerator.generateCreateTable(classes);
        databaseUtil.queryDb(query);
    }

    public void createTable(String tableName, List<Field> fields) {
        String query = SQLGenerator.generateCreateTable(tableName, fields);
        databaseUtil.queryDb(query);
    }

    public <T extends IModel> List<T> findAll(String tableName, Class<T> classObj) {
        String query = SQLGenerator.generateSelect(tableName);
        ResultSet resultSet = databaseUtil.queryDb(query);
        return ResultSetMapper.resultToList(resultSet, classObj);
    }

    public <T extends IModel> T findEntity(IModel entity, Class<T> classObj) {
        String query = SQLGenerator.generateSelect(entity);
        ResultSet resultSet = databaseUtil.queryDb(query);
        return ResultSetMapper.resultToObject(resultSet, classObj);
    }

    public void dropTable(String tableName) {
        String query = SQLGenerator.generateDropTable(List.of(tableName));
        databaseUtil.queryDb(query);
    }

    public void saveEntity(IModel model) {
        String query = SQLGenerator.generateInsert(model);
        databaseUtil.queryDb(query);
    }

    public void updateEntity(IModel model) {
        String query = SQLGenerator.generateUpdate(model);
        databaseUtil.queryDb(query);
    }

    public void deleteEntity(IModel model) {
        String query = SQLGenerator.generateDelete(model);
        databaseUtil.queryDb(query);
    }
}
