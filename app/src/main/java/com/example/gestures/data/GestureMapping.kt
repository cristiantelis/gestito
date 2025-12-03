package com.example.gestures.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mappings")
data class GestureMapping(
    @PrimaryKey val gestureName: String,
    val action: String
)
