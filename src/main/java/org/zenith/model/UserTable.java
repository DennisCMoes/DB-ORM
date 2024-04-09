package org.zenith.model;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity()
public class UserTable implements IModel {
    @Id()
    public int id;

    @Column(type = ColumnType.VARCHAR, size = 64)
    public String name;

    public UserTable(String name) {
        this.name = name;
    }
}
