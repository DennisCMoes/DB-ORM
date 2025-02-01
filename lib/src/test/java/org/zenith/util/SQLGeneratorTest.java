package org.zenith.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Entity
class TestModel1 implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.VARCHAR)
    public String name;
}

@Entity
class TestModel2 implements IModel {
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

@Entity
class EmptyModel implements IModel { }

class SQLGeneratorTest {
    @Test
    void shouldGenerateCreateTableForSingleClass() {
        List<Class<? extends IModel>> classes = List.of(TestModel1.class);

        String result = SQLGenerator.generateCreateTable(classes);

        String expected = "CREATE TABLE testmodel1 (id SERIAL PRIMARY KEY, name VARCHAR (64));";

        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateCreateTableForMultipleClasses() {
        List<Class<? extends IModel>> classes = List.of(TestModel1.class, TestModel2.class);

        String result = SQLGenerator.generateCreateTable(classes);

        String expected = """
                CREATE TABLE testmodel1 (id SERIAL PRIMARY KEY, name VARCHAR (64));
                CREATE TABLE testmodel2 (id SERIAL PRIMARY KEY, age INTEGER, salary TEXT, isWorking INTEGER, parent_id INT, FOREIGN KEY (parent_id) REFERENCES testmodel1(id));""";

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

    @Nested
    class GenerateInsert {
        @Test
        void shouldGenerateValidInsertWithNullFields() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            TestModel1 model = new TestModel1();
            model.id = 1;
            model.name = null;

            List<String> result = SQLGenerator.generateInsert(model);
            String expected = "INSERT INTO testmodel1 (id, name) VALUES (1, NULL) RETURNING *;";

            assertEquals(1, result.size());
            assertEquals(expected, result.getFirst());
        }

        @Test
        void shouldGenerateValidInsertWithNullOneToOneRelationship() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            TestModel1 model = new TestModel1();
            model.id = 1;
            model.name = null;

            List<String> result = SQLGenerator.generateInsert(model);
            String expected = "INSERT INTO testmodel1 (id, name) VALUES (1, NULL) RETURNING *;";

            assertEquals(expected, result.getFirst());
        }

        @Test
        void shouldGenerateValidInsertForTestClass1() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            TestModel1 model = new TestModel1();
            model.id = 1;
            model.name = "Hello";

            List<String> result = SQLGenerator.generateInsert(model);
            String expected = "INSERT INTO testmodel1 (id, name) VALUES (1, 'Hello') RETURNING *;";

            assertEquals(expected, result.getFirst());
        }

        @Test
        void shouldGenerateValidInsertWithEmptyString() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            TestModel1 model = new TestModel1();
            model.id = 1;
            model.name = "";

            List<String> result = SQLGenerator.generateInsert(model);
            String expected = "INSERT INTO testmodel1 (id, name) VALUES (1, '') RETURNING *;";

            assertEquals(expected, result.getFirst());
        }

        @Test
        void shouldGenerateValidInsertForModelWIthAllNullFields() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            TestModel1 model = new TestModel1();

            List<String> result = SQLGenerator.generateInsert(model);
            String expected = "INSERT INTO testmodel1 (id, name) VALUES (0, NULL) RETURNING *;";

            assertEquals(expected, result.getFirst());
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

            List<String> result = SQLGenerator.generateInsert(model2);
            String expected = "INSERT INTO testmodel2 (id, age, salary, isWorking, parent_id) VALUES (0, 10, '10.0', 0, 1) RETURNING *;";

            assertEquals(expected, result.getFirst());
        }

        @Test
        void shouldGenerateValidInsertForTestClass2() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            TestModel2 model = new TestModel2();
            model.age = 10;
            model.salary = 12.0;

            List<String> result = SQLGenerator.generateInsert(model);
            String expected = "INSERT INTO testmodel2 (id, age, salary, isWorking, parent_id) VALUES (0, 10, '12.0', 0, NULL) RETURNING *;";

            assertEquals(expected, result.getFirst());
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenGeneratingInsertWithNoAnnotatedFields() {
            assertThrows(IllegalArgumentException.class, () -> SQLGenerator.generateInsert(new EmptyModel()));
        }
    }

    @Nested
    class GenerateSelect {
        @Test
        void shouldGenerateSelectAllColumnsWithoutFilters() throws NoSuchFieldException {
            String result = SQLGenerator.generateSelect(TestModel1.class, null, null);
            String expected = "SELECT * FROM testmodel1;";

            assertEquals(expected, result);
        }

        @Test
        void shouldGenerateSelectSpecificColumnsWithoutFilters() throws NoSuchFieldException {
            List<String> fieldsToReturn = List.of("id", "name");

            String result = SQLGenerator.generateSelect(TestModel1.class, fieldsToReturn, null);
            String expected = "SELECT id, name FROM testmodel1;";

            assertEquals(expected, result);
        }

        @Test
        void shouldGenerateSelectAllColumnsWithSingleFilter() throws NoSuchFieldException {
            Map<String, Object> fieldsToQuery = Map.of("id", 1);

            String result = SQLGenerator.generateSelect(TestModel1.class, null, fieldsToQuery);
            String expected = "SELECT * FROM testmodel1 WHERE id=1;";

            assertEquals(expected, result);
        }

        @Test
        void shouldGenerateSelectSpecificColumnsWithMultipleFilters() throws NoSuchFieldException {
            List<String> fieldsToReturn = List.of("id", "name");
            Map<String, Object> fieldsToQuery = new TreeMap<>();
            fieldsToQuery.put("id", 1);
            fieldsToQuery.put("name", "hello");

            String result = SQLGenerator.generateSelect(TestModel1.class, fieldsToReturn, fieldsToQuery);
            String expected = "SELECT id, name FROM testmodel1 WHERE id=1 AND name='hello';";

            assertEquals(expected, result);
        }

        @Test
        void shouldGenerateSelectAllColumnsWithNullFieldValue() throws NoSuchFieldException {
            Map<String, Object> fieldsToQuery = new HashMap<>();
            fieldsToQuery.put("name", null);
            fieldsToQuery.put("id", 1);

            String result = SQLGenerator.generateSelect(TestModel1.class, null, fieldsToQuery);
            String expected = "SELECT * FROM testmodel1 WHERE name='null' AND id=1;";

            assertEquals(expected, result);
        }

        @Test
        void shouldGenerateSelectAllColumnsWithNoFilters() throws NoSuchFieldException {
            String result = SQLGenerator.generateSelect(TestModel1.class, null, new HashMap<>());
            String expected = "SELECT * FROM testmodel1;";

            assertEquals(expected, result);
        }

        @Test
        void shouldThrowExceptionWhenFieldAnnotationsAreMissing() {
            assertThrows(NullPointerException.class, () -> {
                SQLGenerator.generateSelect(EmptyModel.class, List.of("id"), Map.of("id", null));
            });
        }
    }

    @Nested
    class GenerateUpdate {
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
    }

    @Nested
    class GenerateDelete {
        @Test
        void shouldGenerateDeleteQueryForValidModel() throws NoSuchFieldException, IllegalAccessException {
            TestModel1 model = new TestModel1();
            model.id = 1;

            String result = SQLGenerator.generateDelete(model);
            String expected = "DELETE FROM testmodel1 WHERE id=1 RETURNING *;";

            assertEquals(expected, result);
        }
    }
}