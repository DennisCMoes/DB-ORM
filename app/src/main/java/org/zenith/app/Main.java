package org.zenith.app;

import org.zenith.models.TodoItem;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.SQLGenerator;
import org.zenith.util.SQLiteDatabase;

import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SQLiteDatabase db = SQLiteDatabase.getInstance();
        List<Class<? extends IModel>> modelsToCreate = List.of(TodoItem.class);
        List<IModel> modelsToInsert = List.of(
                new TodoItem(1, "Test Title", "Test Description", false, new Date()),
                new TodoItem(2, "Second Test", "Another test description", true, new Date()));

        try {
            String createTablesQuery = SQLGenerator.generateCreateTable(modelsToCreate);
            db.executeQueryWithoutResult(createTablesQuery);

            for (IModel model : modelsToInsert) {
                String insertQuery = SQLGenerator.generateInsert(model);
                db.executeQueryWithResult(insertQuery);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        FxApplication.main(args);
    }
}
