package org.zenith.util.strategies.reflection;

import org.zenith.model.interfaces.IModel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManyToManyMappingStrategy implements FieldMappingStrategy {
    @Override
    public void mapField(ResultSet resultSet, IModel model, Field field)
            throws SQLException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
        return;
//        ReflectionUtil reflectionUtil = new ReflectionUtil();
//        Class<?> relatedClass = reflectionUtil.getFieldType(modelClass, fieldName);
//        String relatedTableName = relatedClass.getSimpleName().toLowerCase();
//        String joinTableName = model.getClass().getSimpleName().toLowerCase() + "_" + relatedTableName;
//
//        String countQuery = SQLGenerator.generateCountSelect((Class<? extends IModel>) relatedClass, Map.of("id", reflectionUtil.getValueOfField(model,"id")));
//        String manyToManySelectQuery = SQLGenerator.generateManyToManySelect(model, (Class<? extends IModel>) relatedClass, null);
//
//        List<IModel> linkedSubClasses = new ArrayList<>();
//
//        try {
//            ResultSet countResultSet = SQLiteDatabase.getInstance().executeQueryWithResult(countQuery);
//
//            if (countResultSet.next()) {
//                int amount = countResultSet.getInt("COUNT(*)");
//
//                if (amount > 0) {
//                    ResultSet manyToManyResultSet = SQLiteDatabase.getInstance().executeQueryWithResult(manyToManySelectQuery);
//
//                    while (manyToManyResultSet.next()) {
//                        int categoryId = resultSet.getInt("category_id");
//                        System.out.println(categoryId);
//                    }
//                }
//            }
//
//
////                    ResultSet resultSet1 = SQLiteDatabase.getInstance().executeQueryWithResult(countQuery);
////
////                    if (resultSet1.next()) {
////                        int amount = resultSet1.getInt("COUNT(*)");
////
////                        if (amount > 0) {
////                            String selectQuery = SQLGenerator.generateManyToManySelect(model, (Class<? extends IModel>) relatedClass, null);
////                            ResultSet relatedModelsResultSet = SQLiteDatabase.getInstance().executeQueryWithResult(selectQuery);
////
////                            // SELECT * FROM todoitem_category WHERE todoitem_id=1
////                            // category_id=1, todoitem_id=3
////                            // category_id=2, todoitem_id=3
////                            while (relatedModelsResultSet.next()) {
////                                int category_id = resultSet.getInt("category_id");
////                                String categorySelectQuery = SQLGenerator.generateSelect((Class<? extends IModel>) relatedClass, null, Map.of("id", category_id));
////                                ResultSet categoryResultSet = SQLiteDatabase.getInstance().executeQueryWithResult(categorySelectQuery);
////
////                                while (categoryResultSet.next()) {
////                                    IModel relatedModel = mapToModel(relatedModelsResultSet, modelClass, model);
////                                    linkedSubClasses.add(relatedModel);
////                                }
////                            }
////                        }
////                    }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

        // TODO: SELECT * FROM todoitem_category WHERE todoitem_id = 1;

    }
}
