package org.zenith.database;

import org.zenith.database.interfaces.IDBConnection;

import java.sql.*;
import java.util.List;
import java.util.Properties;

public class DBConnection implements IDBConnection {
    public static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        String connectionUrl = "jdbc:postgresql://127.0.0.1:5432/postgres?user=user&password=password";
        Properties properties = new Properties();

        properties.setProperty("user", "user");
        properties.setProperty("password", "password");

        try {
            this.connection = DriverManager.getConnection(connectionUrl, properties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static DBConnection getInstance() {
        if (DBConnection.instance == null) {
            instance = new DBConnection();
        }

        return DBConnection.instance;
    }

    @Override
    public PreparedStatement prepareQuery(String query) {
        try {
            return connection.prepareStatement(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet queryDb(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet queryDb(PreparedStatement statement) {
        try {
            return statement.executeQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void dropTables(List<String> tables) {
        try {
            queryDb(String.format("DROP TABLE %s;", String.join(", ", tables)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
