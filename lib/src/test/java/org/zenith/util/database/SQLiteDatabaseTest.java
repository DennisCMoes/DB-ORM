package org.zenith.util.database;

import org.junit.jupiter.api.*;
import org.zenith.util.SQLiteDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteDatabaseTest {
    private SQLiteDatabase db;

    @BeforeEach
    public void setUp() {
        try {
            db = SQLiteDatabase.getInstance();

            if (db.getConnection() == null || db.getConnection().isClosed()) {
                db.reinitializeConnection();
            }

            db.executeQueryWithoutResult("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT)");
            db.executeQueryWithoutResult("INSERT INTO users (name) VALUES ('John Doe')");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        db.close();
    }

    @Test
    void testSingletonInstance() {
        SQLiteDatabase db1 = SQLiteDatabase.getInstance();
        SQLiteDatabase db2 = SQLiteDatabase.getInstance();

        assertSame(db1, db2, "The database instances should be the same");
    }

    @Test
    void testConnectionIsNotNull() {
        assertNotNull(db.getConnection(), "The connection should not be null");
    }

    @Test
    void testExecuteQueryWithoutResult() throws SQLException {
        boolean updateResult = db.executeQueryWithoutResult("UPDATE users SET name = 'Jane Doe' WHERE id = 1");
        assertTrue(updateResult, "Update query should execute successfully.");
    }

    @Test
    void testExecuteQueryWithResult() throws SQLException {
        ResultSet resultSet = db.executeQueryWithResult("SELECT id, name FROM users WHERE name = 'John Doe'");
        assertNotNull(resultSet, "ResultSet should not be null.");

        assertTrue(resultSet.next(), "There should be a result in the ResultSet.");
        assertEquals(1, resultSet.getInt("id"), "The ID should be 1.");
        assertEquals("John Doe", resultSet.getString("name"), "The name should be 'John Doe'.");
    }

    @Test
    void testCloseConnection() throws SQLException {
        db.close();

        assertThrows(SQLException.class, () -> {
            String selectQuery = "SELECT * FROM users";
            db.executeQueryWithResult(selectQuery);
        }, "Should throw SQLException after closing the connection");
    }
}