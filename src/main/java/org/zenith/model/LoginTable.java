package org.zenith.model;

import org.zenith.annotation.Column;
import org.zenith.annotation.Entity;
import org.zenith.annotation.Id;
import org.zenith.annotation.relation.OneToOne;
import org.zenith.enumeration.ColumnType;
import org.zenith.model.interfaces.IModel;

@Entity()
public class LoginTable implements IModel {
    @Id()
    public int id;

    @OneToOne()
    public UserTable user;

    @Column(type = ColumnType.VARCHAR, size = 64)
    public String email;

    public LoginTable() {}

    public LoginTable(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, email=%s}", id, email);
    }
}
