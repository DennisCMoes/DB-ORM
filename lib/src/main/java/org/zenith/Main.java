package org.zenith;

import org.zenith.core.EntityManager;
import org.zenith.core.OrmManager;
import org.zenith.model.LoginTable;
import org.zenith.model.UserTable;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            OrmManager ormManager = new OrmManager();
            EntityManager entityManager = new EntityManager();

            ormManager.clearDatabase();
            ormManager.initializeDatabase();

            UserTable user = entityManager.saveEntity(new UserTable("Dennis"), UserTable.class);
            LoginTable login = entityManager.saveEntity(new LoginTable("dennismoes@me.com", user), LoginTable.class);

            System.out.println(login);
        } catch (ConnectException ex) {
            ex.printStackTrace();
        }
    }
}