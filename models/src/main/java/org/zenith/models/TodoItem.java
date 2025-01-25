package org.zenith.models;

import org.zenith.annotation.Id;
import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity
public class TodoItem implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.TEXT)
    public String title;
    @Column(type = ColumnType.TEXT)
    public String description;

    public TodoItem() { }

    public TodoItem(int id, String description, String title) {
        this.description = description;
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format("%s", title);
    }
}
