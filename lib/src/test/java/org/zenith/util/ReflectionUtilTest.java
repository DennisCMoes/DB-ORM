package org.zenith.util;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Entity
class TestModel implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.VARCHAR)
    public String name;
    @Column(type = ColumnType.INTEGER)
    public int age;
}

class ReflectionUtilTest {
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMapToModel() throws Exception {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getObject("name")).thenReturn("Test Name");
        when(resultSet.getObject("age")).thenReturn(25);

        TestModel model = ReflectionUtil.mapToModel(resultSet, TestModel.class);

        assertEquals(1, model.id);
        assertEquals("Test Name", model.name);
        assertEquals(25, model.age);
    }

    @Test
    void testMapToModelWithNullValue() throws Exception {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getObject("name")).thenReturn(null);
        when(resultSet.getObject("age")).thenReturn(0);

        TestModel model = ReflectionUtil.mapToModel(resultSet, TestModel.class);

        assertEquals(1, model.id);
        assertNull(model.name);
        assertEquals(0, model.age);
    }

    @Test
    void testGetFieldsOfModel() {
        List<Field> fields = ReflectionUtil.getFieldsOfModel(TestModel.class);

        assertNotNull(fields);
        assertFalse(fields.isEmpty());
    }

    @Test
    void testGetFieldsOfModelWithoutTypes() {
        List<Class<? extends Annotation>> annotations = List.of(Id.class);

        List<Field> fieldsWithoutAnnotations = ReflectionUtil.getFieldsOfModelWithoutTypes(TestModel.class, annotations);

        assertNotNull(fieldsWithoutAnnotations);
        assertEquals(2, fieldsWithoutAnnotations.size());
    }

    @Test
    void testGetFieldsOfModelWithAnnotations() {
        List<Class<? extends Annotation>> annotations = List.of(Column.class);
        List<Field> fieldsWithAnnotations = ReflectionUtil.getFieldsOfModelWithTypes(TestModel.class, annotations);

        assertNotNull(fieldsWithAnnotations);
        assertEquals(2, fieldsWithAnnotations.size());
    }

    @Test
    void testGetValueOfField() throws Exception {
        TestModel model = new TestModel();
        model.name = "Test Name";
        model.age = 30;

        Object value = ReflectionUtil.getValueOfField(model, "name");
        assertEquals("Test Name", value);
    }

    @Test
    void testGetFieldName() throws NoSuchFieldException {
        Field field = TestModel.class.getDeclaredField("name");

        String fieldName = ReflectionUtil.getFieldName(field);
        assertEquals("name", fieldName);
    }

    @Test
    void testGetFieldNameWIthRelationAnnotation() throws NoSuchFieldException {
        class RelatedModel implements IModel {
            @OneToOne
            public TestModel relatedModel;
        }

        Field field = RelatedModel.class.getDeclaredField("relatedModel");
        String fieldName = ReflectionUtil.getFieldName(field);
        assertEquals("relatedModel_id", fieldName);
    }

    @Test
    void testGetFieldByName() throws NoSuchFieldException {
        Field field = ReflectionUtil.getFieldByName(TestModel.class, "name");

        assertNotNull(field);
        assertEquals("name", field.getName());
    }

    @Test
    void testGetFieldType() throws NoSuchFieldException {
        Class<?> fieldType = ReflectionUtil.getFieldType(TestModel.class, "name");
        assertEquals(String.class, fieldType);
    }

    @Test
    void testGetFieldTypeWithList() throws NoSuchFieldException {
        class ListModel implements IModel {
            @Column(type = ColumnType.VARCHAR)
            public List<String> items;
        }

        Class<?> fieldType = ReflectionUtil.getFieldType(ListModel.class, "items");
        assertEquals(String.class, fieldType);
    }
}