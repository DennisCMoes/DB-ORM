package org.zenith.models;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity
public class Category implements IModel {
    @Id
    public int id;
    @Column(type = ColumnType.VARCHAR)
    public String label;

    public Category() { }

    public Category(int id, String label) {
        this.id = id;
        this.label = label;
    }
}
