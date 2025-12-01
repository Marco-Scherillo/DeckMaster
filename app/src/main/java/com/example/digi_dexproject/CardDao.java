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

    // Required for the ScanFragment logic
    @Query("SELECT * FROM cards WHERE name LIKE :searchQuery LIMIT 1")
    CardEntity findByName(String searchQuery);


    @Query("SELECT * FROM cards WHERE name IN (:names)")
    List<CardEntity> getCardsByNames(List<String> names);

    @Query("SELECT name FROM cards WHERE name IN (:names)")
    List<String> getExistingNames(List<String> names);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CardEntity card);

}