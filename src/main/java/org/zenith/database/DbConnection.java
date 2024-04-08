package org.zenith.database;

import java.sql.*;
import java.util.Properties;

public class DbConnection {
    public static DbConnection instance;
    private Connection connection;

    private DbConnection() {
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

    public static DbConnection getInstance() {
        if (DbConnection.instance == null) {
            instance = new DbConnection();
        }

        return DbConnection.instance;
    }

    public PreparedStatement prepareQuery(String query) {
        try {
            return connection.prepareStatement(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet queryDb(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet queryDb(PreparedStatement statement) {
        try {
            return statement.executeQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
