package org.zenith.models;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.ManyToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity
public class SubItem implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.TEXT)
    public String title;
    @ManyToOne
    public TodoItem todoItem;

    public SubItem() { }

    public SubItem(int id, String title, TodoItem parent) {
        this.id = id;
        this.title = title;
        this.todoItem = parent;
    }

    @Override
    public String toString() {
        return String.format("%s", this.title);
    }
}
