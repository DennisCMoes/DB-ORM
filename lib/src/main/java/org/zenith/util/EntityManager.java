package org.zenith.util;

import org.zenith.model.interfaces.IModel;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityManager {
    private final SQLiteDatabase db;

    public EntityManager() {
        this(SQLiteDatabase.getInstance());
    }

    public EntityManager(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Saves a model object in the database
     * This method generates an INSERT SQL query based on the provided model and executes is
     *
     * @param model The model object to be saved
     * @return true if the operation was successful, false otherwise
     * @throws SQLException If there is an error while executing the SQL query
     * @throws NoSuchFieldException If a field specified in the model is not found
     * @throws IllegalAccessException If there is an access issue with a field in the model
     */
    public boolean save(IModel model) throws SQLException, NoSuchFieldException, IllegalAccessException {
        String insertQuery = SQLGenerator.generateInsert(model);
        return db.executeQueryWithoutResult(insertQuery);
    }

    /**
     * Updates a model object in the database
     * This method generates an UPDATE SQL query based on the provided model and executes it
     *
     * @param model The model object to be updated
     * @return true if the operation was successful, false otherwise
     * @throws SQLException If there is an error while executing the SQL query
     * @throws NoSuchFieldException If a field specified in the model is not found
     * @throws IllegalAccessException If there is an access issue with a field in the model
     */
    public boolean update(IModel model) throws SQLException, NoSuchFieldException, IllegalAccessException {
        String updateQuery = SQLGenerator.generateUpdate(model);
        return db.executeQueryWithoutResult(updateQuery);
    }

    /**
     * Deletes a model object from the database
     * This method generates a DELETE SQL query based on the provided model and executes it
     *
     * @param model The model object to be deleted
     * @return true if the operation was successful, false otherwise
     * @throws SQLException If there is an error while executing the SQL query
     * @throws NoSuchFieldException If a field specified in the model is not found
     * @throws IllegalAccessException If there is an access issue with a field in the model
     */
    public boolean delete(IModel model) throws SQLException, NoSuchFieldException, IllegalAccessException {
        String deleteQuery = SQLGenerator.generateDelete(model);
        return db.executeQueryWithoutResult(deleteQuery);
    }

    /**
     * Retrieves a list of model objects from the database based on the provided model class
     * This method generates a SELECT SQL query to retrieve all records of the specified model class
     *
     * @param modelClass The class type of the model
     * @param <T> The type of the model that extends IModel
     * @return A list of model objects fetched from the database
     * @throws SQLException If there is an error while executing the SQL query
     * @throws NoSuchFieldException If a field specified in the model is not found
     * @throws IllegalAccessException If there is an access issue with a field in the model
     * @throws InvocationTargetException If there is an issue invoking methods via reflection
     * @throws InstantiationException If there is an issue instantiating the model object
     * @throws NoSuchMethodException If there is an issue finding a method in the model class
     */
    public <T extends IModel> List<T> list(Class<T> modelClass)
            throws SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException{

        String selectQuery = SQLGenerator.generateSelect(modelClass, null, null);
        ResultSet resultSet = db.executeQueryWithResult(selectQuery);

        List<T> result = new ArrayList<>();

        while (resultSet.next()) {
            result.add(ReflectionUtil.mapToModel(resultSet, modelClass));
        }

        return result;
    }

    /**
     * Retrieves a model object by its ID from the database
     * This method generates a SELECT SQL query to retrieve a model object with the specified ID
     *
     * @param modelClass The class type of the model
     * @param fieldsToReturn The list of fields to be returned (or null for all fields)
     * @param id The ID of the model object to be retrieved
     * @return The model object corresponding to the specified ID
     * @throws SQLException If there is an error while executing the SQL query
     * @throws NoSuchFieldException If a field specified in the model is not found
     * @throws IllegalAccessException If there is an access issue with a field in the model
     * @throws InvocationTargetException If there is an issue invoking methods via reflection
     * @throws InstantiationException If there is an issue instantiating the model object
     * @throws NoSuchMethodException If there is an issue finding a method in the model class
     */
    public IModel findById(Class<? extends IModel> modelClass, List<String> fieldsToReturn, int id)
            throws SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {

        String selectQuery = SQLGenerator.generateSelect(modelClass, fieldsToReturn, Map.of("id", id));
        ResultSet resultSet = db.executeQueryWithResult(selectQuery);
        return ReflectionUtil.mapToModel(resultSet, modelClass);
    }

    /**
     * Retrieves a model object based on specific field values from the database
     * This method generates a SELECT SQL query to retrieve a model object that matches the specified field values
     *
     * @param modelClass The class type of the model
     * @param fieldsToReturn The list of fields to be returned
     * @param fieldsToQuery A map of field names and their corresponding values to query against
     * @return The model object that matches the specified fields
     * @throws SQLException If there is an error while executing the SQL query
     * @throws NoSuchFieldException If a field specified in the model is not found
     * @throws IllegalAccessException If there is an access issue with a field in the model
     * @throws InvocationTargetException If there is an issue invoking methods via reflection
     * @throws InstantiationException If there is an issue instantiating the model object
     * @throws NoSuchMethodException If there is an issue finding a method in the model class
     */
    public IModel findByField(Class<? extends IModel> modelClass, List<String> fieldsToReturn, Map<String, Object> fieldsToQuery)
            throws SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {

        String selectQuery = SQLGenerator.generateSelect(modelClass, fieldsToReturn, fieldsToQuery);
        ResultSet resultSet = db.executeQueryWithResult(selectQuery);
        return ReflectionUtil.mapToModel(resultSet, modelClass);
    }
}
