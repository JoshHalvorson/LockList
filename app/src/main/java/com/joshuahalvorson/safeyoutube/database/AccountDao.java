package com.joshuahalvorson.safeyoutube.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM account")
    List<Account> getAll();

    @Query("SELECT * FROM account WHERE id = (:id)")
    Account getAccountById(int id);

    @Insert
    void insertAll(Account... accounts);

    @Delete
    void delete(Account account);
}
