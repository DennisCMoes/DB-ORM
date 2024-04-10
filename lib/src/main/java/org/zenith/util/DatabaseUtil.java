package org.zenith.util;

import java.net.ConnectException;
import java.sql.*;

public class DatabaseUtil {
    public static DatabaseUtil instance;
    private Connection connection;

    private DatabaseUtil() throws ConnectException {
        String connectionUrl = "jdbc:postgresql://127.0.0.1:5432/postgres?user=user&password=password";

        try {
            this.connection = DriverManager.getConnection(connectionUrl);
        } catch (SQLException ex) {
            if (ex.getCause() instanceof ConnectException) {
                throw (ConnectException) ex.getCause();
            } else {
                ex.printStackTrace();
            }
        }
    }

    public static DatabaseUtil getInstance() throws ConnectException {
        if (DatabaseUtil.instance == null) {
            instance = new DatabaseUtil();
        }

        return DatabaseUtil.instance;
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