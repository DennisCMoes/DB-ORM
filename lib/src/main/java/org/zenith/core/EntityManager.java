package org.zenith.core;

import org.zenith.mapper.EntityMapper;
import org.zenith.mapper.SQLGenerator;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.DatabaseUtil;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Manages entity lifecycle and database operations.
 * It would provide methods for CRUD operations (e.g., save, find, update, delete) and transaction management.
 */
public class EntityManager {
    private final DatabaseUtil databaseUtil;

    public EntityManager() throws ConnectException {
        this.databaseUtil = DatabaseUtil.getInstance();
    }

    public void createTables(List<Class<? extends IModel>> classes) {
        try {
            String query = SQLGenerator.generateCreateTable(classes);
            databaseUtil.updateDb(query);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void alterTables(List<Class<? extends IModel>> classes) {
        try {
            String query = SQLGenerator.generateAddForeignKey(classes);
            databaseUtil.updateDb(query);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void dropTable(String tableName) {
        try {
            String query = SQLGenerator.generateDropTable(List.of(tableName));
            databaseUtil.updateDb(query);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends IModel> List<T> findAll(Class<T> classObj) {
        try {
            String query = SQLGenerator.generateSelect(classObj.getSimpleName().toLowerCase());
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToList(resultSet, classObj);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends IModel> T findEntity(IModel entity, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateSelect(entity);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends IModel> T saveEntity(IModel model, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateInsert(model);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends IModel> T updateEntity(IModel model, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateUpdate(model);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends IModel> T deleteEntity(IModel model, Class<T> classObj) {
        try {
            String query = SQLGenerator.generateDelete(model);
            ResultSet resultSet = databaseUtil.queryDb(query);
            return EntityMapper.resultToObject(resultSet, classObj);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
