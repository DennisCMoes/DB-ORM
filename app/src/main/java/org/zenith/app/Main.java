package org.zenith.app;

import org.zenith.models.SubItem;
import org.zenith.models.TodoItem;
import org.zenith.model.interfaces.IModel;
import org.zenith.util.Logger;
import org.zenith.util.SQLGenerator;
import org.zenith.util.SQLiteDatabase;

import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Logger.clearAccess();

        SQLiteDatabase db = SQLiteDatabase.getInstance();
        List<Class<? extends IModel>> modelsToCreate = List.of(TodoItem.class, SubItem.class);
        TodoItem parentTodo = new TodoItem(3, "With sub items", "This todo will have multiple sub items", false, new Date());

        List<IModel> modelsToInsert = List.of(
                new TodoItem(1, "Test Title", "Test Description", false, new Date()),
                new TodoItem(2, "Second Test", "Another test description", true, new Date()),
                parentTodo,
                new SubItem(1, "Sub item 1", parentTodo),
                new SubItem(2, "Sub item 2", parentTodo),
                new SubItem(3, "Sub item 3", parentTodo));


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
