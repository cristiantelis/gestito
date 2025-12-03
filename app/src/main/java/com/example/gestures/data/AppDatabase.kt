package com.example.gestures.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GestureMapping::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gestureMappingDao(): GestureMappingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestures_db"
                )
                    // para demo simplificado; en producción usa coroutines y quita allowMainThreadQueries
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
