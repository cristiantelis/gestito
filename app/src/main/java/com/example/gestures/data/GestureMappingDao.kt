package com.example.gestures.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface GestureMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mapping: GestureMapping)

    @Query("SELECT action FROM mappings WHERE gestureName = :name LIMIT 1")
    fun getActionForGesture(name: String): String?

    @Query("SELECT * FROM mappings")
    fun getAll(): List<GestureMapping>

    @Query("DELETE FROM mappings WHERE gestureName = :name")
    fun deleteByName(name: String)

    @Delete
    fun delete(mapping: GestureMapping)
}
