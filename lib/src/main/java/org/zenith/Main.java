package org.zenith;

import org.zenith.util.SQLiteDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SQLiteDatabase db = SQLiteDatabase.getInstance();

        try (Connection conn = db.getConnection()) {
            // Do something with the connection
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}