package org.zenith.model;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity
public class ToDoItem implements IModel {
    @Id()
    public int id;

    @Column(type = ColumnType.VARCHAR, size = 248)
    public String title;

    @Column(type = ColumnType.TEXT)
    public String description;

    @ManyToOne()
    public ToDoList parentList;

    public ToDoItem() { }

    public ToDoItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public ToDoItem(String title, String description, ToDoList parentList) {
        this.title = title;
        this.description = description;
        this.parentList = parentList;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, title=%s, description=%s, parentList=%d}", id, title, description, parentList.id);
    }
}
