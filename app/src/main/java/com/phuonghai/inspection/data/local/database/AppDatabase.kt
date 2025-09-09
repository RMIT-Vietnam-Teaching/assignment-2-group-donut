package com.phuonghai.inspection.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.phuonghai.inspection.data.local.dao.LocalReportDao
import com.phuonghai.inspection.data.local.dao.LocalTaskDao
import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import com.phuonghai.inspection.data.local.entity.LocalTaskEntity

@Database(
    entities = [
        LocalReportEntity::class,
        LocalTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localReportDao(): LocalReportDao
    abstract fun localTaskDao(): LocalTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inspection_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}