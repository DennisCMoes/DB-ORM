package org.zenith.mapper;

import org.junit.jupiter.api.Test;
import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLGeneratorTest {
    public static class TestModel1 implements IModel {
        @Id
        public int id;
        @Column(type = ColumnType.VARCHAR)
        public String name;
    }

    public static class TestModel2 implements IModel {
        @Id
        public int id;
        @Column(type = ColumnType.INTEGER)
        public int age;
        @Column(type = ColumnType.TEXT)
        public double salary;
        @Column(type = ColumnType.BOOLEAN)
        public boolean isWorking;

        @OneToOne
        public TestModel1 parent;
    }

    public static class EmptyModel implements IModel { }

    @Test
    void shouldGenerateCreateTableForSingleClass() {
        List<Class<? extends IModel>> classes = List.of(TestModel1.class);

        String result = SQLGenerator.generateCreateTable(classes);

        String expected = """
                CREATE TABLE testmodel1 (id SERIAL PRIMARY KEY, name VARCHAR (64));""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateCreateTableForMultipleClasses() {
        List<Class<? extends IModel>> classes = List.of(TestModel1.class, TestModel2.class);

        String result = SQLGenerator.generateCreateTable(classes);

        String expected = """
                CREATE TABLE testmodel1 (id SERIAL PRIMARY KEY, name VARCHAR (64));
                CREATE TABLE testmodel2 (id SERIAL PRIMARY KEY, age INTEGER (64), salary TEXT, isWorking BOOLEAN (64), parent_id INT);""";

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGenerateCreateTableIsCalledWithAnEmptyModel() {
        List<Class<? extends IModel>> classes = List.of(EmptyModel.class);
        assertThrows(IllegalArgumentException.class, () -> SQLGenerator.generateCreateTable(classes));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGenerateCreateTableIsCalledWithEmptyList() {
        List<Class<? extends IModel>> classes = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> SQLGenerator.generateCreateTable(classes));
    }

    @Test
    void shouldGenerateDropTableForSingleClass() {
        List<Class<? extends IModel>> classes = List.of(TestModel1.class);

        String result = SQLGenerator.generateDropTable(classes);

        String expected = """
                DROP TABLE IF EXISTS testmodel1 CASCADE;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateDropTableForMultipleClass() {
        List<Class<? extends IModel>> classes = List.of(TestModel1.class, TestModel2.class);

        String result = SQLGenerator.generateDropTable(classes);

        String expected = """
                DROP TABLE IF EXISTS testmodel1 CASCADE;
                DROP TABLE IF EXISTS testmodel2 CASCADE;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGenerateDropTableIsCalledWithEmptyList() {
        List<Class<? extends IModel>> classes = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> SQLGenerator.generateDropTable(classes));
    }

    @Test
    void shouldGenerateValidInsertWithNullFields() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = null;

        String result = SQLGenerator.generateInsert(model);
        String expected = """
                INSERT INTO testmodel1 (id, name) VALUES (1, NULL) RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateValidInsertWithNullOneToOneRelationship() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = null;

        String result = SQLGenerator.generateInsert(model);
        String expected = """
                INSERT INTO testmodel1 (id, name) VALUES (1, NULL) RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateValidInsertForTestClass1() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = "Hello";

        String result = SQLGenerator.generateInsert(model);
        String expected = """
                INSERT INTO testmodel1 (id, name) VALUES (1, 'Hello') RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateValidInsertWithEmptyString() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = "";

        String result = SQLGenerator.generateInsert(model);
        String expected = """
                INSERT INTO testmodel1 (id, name) VALUES (1, '') RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateValidInsertForModelWIthAllNullFields() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();

        String result = SQLGenerator.generateInsert(model);
        String expected = """
                INSERT INTO testmodel1 (id, name) VALUES (0, NULL) RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateValidInsertForTestClass1WithOneToOne() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = "Hello";

        TestModel2 model2 = new TestModel2();
        model2.age = 10;
        model2.salary = 10;
        model2.parent = model;

        String result = SQLGenerator.generateInsert(model2);
        String expected = """
                INSERT INTO testmodel2 (id, age, salary, isWorking, parent_id) VALUES (0, 10, '10.0', false, 1) RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateValidInsertForTestClass2() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        TestModel2 model = new TestModel2();
        model.age = 10;
        model.salary = 12.0;

        String result = SQLGenerator.generateInsert(model);
        String expected = """
                INSERT INTO testmodel2 (id, age, salary, isWorking, parent_id) VALUES (0, 10, '12.0', false, NULL) RETURNING *;""";

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGeneratingInsertWithNoAnnotatedFields() {
        assertThrows(IllegalArgumentException.class, () -> SQLGenerator.generateInsert(new EmptyModel()));
    }

    @Test
    void shouldGenerateSelectAllColumnsWithoutFilters() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();

        String result = SQLGenerator.generateSelect(model, null, null);
        String expected = "SELECT * FROM testmodel1;";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateSelectSpecificColumnsWithoutFilters() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();

        List<String> fieldsToReturn = List.of("id", "name");

        String result = SQLGenerator.generateSelect(model, fieldsToReturn, null);
        String expected = "SELECT id, name FROM testmodel1;";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateSelectAllColumnsWithSingleFilter() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;

        List<String> fieldsToQuery = List.of("id");

        String result = SQLGenerator.generateSelect(model, null, fieldsToQuery);
        String expected = "SELECT * FROM testmodel1 WHERE id=1;";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateSelectSpecificColumnsWithMultipleFilters() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = "Hello";

        List<String> fieldsToReturn = List.of("id", "name");
        List<String> fieldsToQuery = List.of("id", "name");

        String result = SQLGenerator.generateSelect(model, fieldsToReturn, fieldsToQuery);
        String expected = "SELECT id, name FROM testmodel1 WHERE id=1 AND name='Hello';";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateSelectAllColumnsWithNullFieldValue() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = null;

        List<String> fieldsToQuery = List.of("id", "name");

        String result = SQLGenerator.generateSelect(model, null, fieldsToQuery);
        String expected = "SELECT * FROM testmodel1 WHERE id=1 AND name='null';";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateSelectAllColumnsWithNoFilters() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();

        List<String> fieldsToQuery = new ArrayList<>();

        String result = SQLGenerator.generateSelect(model, null, fieldsToQuery);
        String expected = "SELECT * FROM testmodel1;";

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowExceptionWhenFieldAnnotationsAreMissing() {
        assertThrows(NoSuchFieldException.class, () -> {
            SQLGenerator.generateSelect(new EmptyModel(), List.of("id"), List.of("id"));
        });
    }

    @Test
    void shouldGenerateUpdateQueryForSimpleFields() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;
        model.name = "UpdatedName";

        String result = SQLGenerator.generateUpdate(model);
        String expected = "UPDATE testmodel1 SET name='UpdatedName' WHERE id=1 RETURNING *;";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateUpdateQueryWithNullValues() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 2;
        model.name = null;

        String result = SQLGenerator.generateUpdate(model);
        String expected = "UPDATE testmodel1 SET name='null' WHERE id=2 RETURNING *;";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateUpdateQueryWithRelationshipFields() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model1 = new TestModel1();
        model1.id = 1;
        model1.name = "John Doe";

        TestModel2 model2 = new TestModel2();
        model2.id = 1;
        model2.age = 10;
        model2.parent = model1;
        model2.isWorking = true;

        String result = SQLGenerator.generateUpdate(model2);
        String expected = "UPDATE testmodel2 SET age=10, salary='0.0', isWorking=true, parent_id=1 WHERE id=1 RETURNING *;";

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowExceptionForNoFieldsToUpdate() {
        EmptyModel model = new EmptyModel();
        assertThrows(NoSuchFieldException.class, () -> SQLGenerator.generateUpdate(model));
    }

    @Test
    void shouldGenerateDeleteQueryForValidModel() throws NoSuchFieldException, IllegalAccessException {
        TestModel1 model = new TestModel1();
        model.id = 1;

        String result = SQLGenerator.generateDelete(model);
        String expected = "DELETE FROM testmodel1 WHERE id=1 RETURNING *;";

        assertEquals(expected, result);
    }
}