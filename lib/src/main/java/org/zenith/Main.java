package org.zenith;

import org.zenith.core.OrmManager;
import java.net.ConnectException;

public class Main {
    public static void main(String[] args) {
        try {
            OrmManager ormManager = new OrmManager();

            ormManager.clearDatabase();
            ormManager.initializeDatabase();
            ormManager.migrateToDatabase();
        } catch (ConnectException ex) {
            ex.printStackTrace();
        }
    }
}