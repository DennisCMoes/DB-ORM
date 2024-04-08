package org.zenith.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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

    public ResultSet queryDb() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM test_table");

            return resultSet;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
