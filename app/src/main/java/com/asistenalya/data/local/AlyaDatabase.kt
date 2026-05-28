package com.asistenalya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatEntity::class], version = 1, exportSchema = false)
abstract class AlyaDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AlyaDatabase? = null

        fun getInstance(context: Context): AlyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlyaDatabase::class.java,
                    "alya_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
