package org.zenith.models;

import org.zenith.annotation.Id;
import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.relation.OneToMany;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

import java.util.Date;
import java.util.List;

@Entity
public class TodoItem implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.TEXT)
    public String title;
    @Column(type = ColumnType.TEXT)
    public String description;
    @Column(type = ColumnType.DATETIME)
    public Date expiresAt;
    @Column(type = ColumnType.BOOLEAN)
    public boolean isCompleted;

    @OneToMany
    public List<SubItem> subItems;

    public TodoItem() { }

    public TodoItem(int id, String description, String title, boolean isCompleted, Date expiresAt) {
        this.description = description;
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return String.format("%s", title);
    }
}
