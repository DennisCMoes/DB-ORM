package org.zenith.model;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity()
public class ToDoList implements IModel {
    @Id()
    public int id;

    @Column(type = ColumnType.VARCHAR)
    public String name;

    public ToDoList() { }

    public ToDoList(String name) {
        this.name = name;
    }
}
