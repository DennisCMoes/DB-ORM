package org.zenith.database.interfaces;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public interface IDBConnection {
    PreparedStatement prepareQuery(String query);
    ResultSet queryDb(String query);
    ResultSet queryDb(PreparedStatement statement);
    void dropTables(List<String> tables);
}
