package org.zenith.util;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zenith.annotation.Column;
import org.zenith.annotation.Id;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TestModel implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.VARCHAR)
    public String name;
    @Column(type = ColumnType.INTEGER)
    public int age;
}

class ReflectionUtilTest {
    private ReflectionUtil reflectionUtil;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reflectionUtil = new ReflectionUtil();
    }

    @Test
    void testMapToModel() throws Exception {
        when(resultSet.getObject("id")).thenReturn(1);
        when(resultSet.getObject("name")).thenReturn("Test Name");
        when(resultSet.getObject("age")).thenReturn(25);

        Class<TestModel> modelClass = TestModel.class;
        TestModel model = (TestModel) ReflectionUtil.mapToModel(resultSet, modelClass);

        assertEquals("Test Name", model.name);
        assertEquals(25, model.age);
    }

    @Test
    void testMapToModelWithNullValue() throws Exception {
        when(resultSet.getObject("id")).thenReturn(1);

        Class<TestModel> modelClass = TestModel.class;
        TestModel model = (TestModel) ReflectionUtil.mapToModel(resultSet, modelClass);

        assertEquals(1, model.id);
        assertNull(model.name);
        assertEquals(0, model.age);
    }

    @Test
    void testGetFieldsOfModel() {
        Class<TestModel> modelClass = TestModel.class;
        List<Field> fields = reflectionUtil.getFieldsOfModel(modelClass);

        assertNotNull(fields);
        assertFalse(fields.isEmpty());
    }

    @Test
    void testGetFieldsOfModelWithoutTypes() {
        Class<TestModel> modelClass = TestModel.class;
        List<Class<? extends Annotation>> annotations = List.of(Id.class);

        List<Field> fieldsWithoutAnnotations = reflectionUtil.getFieldsOfModelWithoutTypes(modelClass, annotations);

        assertNotNull(fieldsWithoutAnnotations);
        assertEquals(2, fieldsWithoutAnnotations.size());
    }

    @Test
    void testGetValueOfField() throws Exception {
        TestModel model = new TestModel();
        model.name = "Test Name";
        model.age = 30;

        Object value = reflectionUtil.getValueOfField(model, "name");
        assertEquals("Test Name", value);
    }

    @Test
    void testGetFieldName() throws NoSuchFieldException {
        Field field = TestModel.class.getDeclaredField("name");

        String fieldName = reflectionUtil.getFieldName(field);
        assertEquals("name", fieldName);
    }

    @Test
    void testGetFieldByName() throws NoSuchFieldException {
        Field field = reflectionUtil.getFieldByName(TestModel.class, "name");

        assertNotNull(field);
        assertEquals("name", field.getName());
    }
}