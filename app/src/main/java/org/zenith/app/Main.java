package org.zenith.app;

import org.zenith.models.Category;
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
        Logger.clearError();

        initializeData();

        FxApplication.main(args);
    }

    private static void initializeData() {
        SQLiteDatabase db = SQLiteDatabase.getInstance();

        List<Class<? extends IModel>> modelsToCreate = List.of(TodoItem.class, SubItem.class, Category.class);

        List<Category> categories = List.of(
                new Category(1, "Category 1"),
                new Category(2, "Category 2"));
        List<TodoItem> todoItems = List.of(
                new TodoItem(1, "Test Title", "Test Description", false, new Date()),
                new TodoItem(2, "Second Test", "Another test description", true, new Date()),
                new TodoItem(3, "With sub items", "This todo will have multiple sub items", false, new Date()));
        List<SubItem> subItems = List.of(
                new SubItem(1, "Sub item 1", todoItems.get(2)),
                new SubItem(2, "Sub item 2", todoItems.get(2)),
                new SubItem(3, "Sub item 3", todoItems.get(2)));

        try {
            Logger.info("=== Creating tables ===");
            List<String> createTableQueries = SQLGenerator.generateCreateTable(modelsToCreate);

            for (String createTableQuery : createTableQueries) {
                db.executeQueryWithoutResult(createTableQuery);
            }

            Logger.info("=== Inserting todo items ===");
            for (TodoItem todoItem : todoItems) {
                List<String> queries = SQLGenerator.generateInsert(todoItem);
                for (String query : queries) {
                    db.executeQueryWithoutResult(query);
                }
            }

            Logger.info("=== Inserting sub items ===");
            for (SubItem subItem : subItems) {
                List<String> queries = SQLGenerator.generateInsert(subItem);
                for (String query : queries) {
                    db.executeQueryWithoutResult(query);
                }
            }

            Logger.info("Inserting categories");
            for (Category category : categories) {
                List<String> queries = SQLGenerator.generateInsert(category);
                for (String query : queries) {
                    db.executeQueryWithoutResult(query);
                }
            }

            Logger.info("=== Finished setting up application ===");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
