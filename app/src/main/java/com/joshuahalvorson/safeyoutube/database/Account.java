package com.joshuahalvorson.safeyoutube.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "account_password")
    public String accountPassword;

    public Account(String accountPassword) {
        this.accountPassword = accountPassword;
    }
}
