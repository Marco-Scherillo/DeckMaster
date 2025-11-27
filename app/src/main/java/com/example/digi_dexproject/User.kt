package com.example.digi_dexproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val username: String,
    val passwordHash: String,
    val scannedCards: List<String> = emptyList()
)
