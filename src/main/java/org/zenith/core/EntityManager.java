package org.zenith.core;

import org.zenith.mapper.EntityMapper;
import org.zenith.mapper.SQLGenerator;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.DatabaseUtil;

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
        return EntityMapper.resultToList(resultSet, classObj);
    }

    public <T extends IModel> T findEntity(IModel entity, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateSelect(entity);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void dropTable(String tableName) {
        String query = SQLGenerator.generateDropTable(List.of(tableName));
        databaseUtil.queryDb(query);
    }

    public <T extends IModel> T saveEntity(IModel model, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateInsert(model);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends IModel> T updateEntity(IModel model, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateUpdate(model);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public <T extends IModel> T deleteEntity(IModel model, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateDelete(model);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
