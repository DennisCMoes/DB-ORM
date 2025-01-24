package org.zenith;

import org.zenith.core.EntityManager;
import org.zenith.core.OrmManager;
import org.zenith.util.database.SQLiteDatabase;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
//        try {
//            OrmManager ormManager = new OrmManager();
//            EntityManager entityManager = new EntityManager();
//
//            ormManager.clearDatabase();
//            ormManager.initializeDatabase();
//            ormManager.migrateToDatabase();
//        } catch (ConnectException ex) {
//            ex.printStackTrace();
//        }

        SQLiteDatabase db = SQLiteDatabase.getInstance();

        try (Connection conn = db.getConnection()) {
            // Do something with the connection
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}