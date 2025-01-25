package org.zenith.util;

import java.sql.*;

public class SQLiteDatabase {
    private final String DATABASE_URL = "jdbc:sqlite::memory:";

    private static SQLiteDatabase instance;
    private Connection connection;

    private SQLiteDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            System.out.println("In-memory SQLite database created");
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves the singleton instance of the SQLiteDatabase class.
     * If an instance has not been created yet, it will create a new instance.
     *
     * @return The singleton instance of the SQLiteDatabase class
     */
    public static synchronized SQLiteDatabase getInstance() {
        if (instance == null) {
            instance = new SQLiteDatabase();
        }

        return instance;
    }

    public void reinitializeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }

        connection = DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Retrieves the connection to the  in-memory SQLite database
     *
     * @return The connection to the in-memory SQLite database
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Executes a query on the database.
     * If the query was successful return true, false if there was an error
     *
     * @param query The SQL query to be executed
     * @return `true` if the query was successfully executed, `false` otherwise.
     */
    public boolean executeQueryWithoutResult(String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query) > 0;
        }
    }

    /**
     * Executes a query on the database and returns the resulting data.
     *
     * @param query The SQL query to be executed.
     * @return A {@link ResultSet} containing the result of the query, or `null` if an error occurred.
     */
    public ResultSet executeQueryWithResult(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }

    /**
     * Tries to the connection to the in-memory SQLite database
     */
    public void close() throws SQLException {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("In-memory SQLite database closed");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
