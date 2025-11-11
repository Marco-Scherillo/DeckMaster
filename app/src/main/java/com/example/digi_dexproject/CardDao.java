package com.example.digi_dexproject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CardEntity> cards);

    @Query("SELECT * FROM cards")
    List<CardEntity> getAll();

    @Query("SELECT COUNT(*) FROM cards")
    int countCards();
}
