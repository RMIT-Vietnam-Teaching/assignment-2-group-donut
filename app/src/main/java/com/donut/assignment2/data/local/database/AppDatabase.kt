package com.donut.assignment2.data.local.database

import androidx.room.*
import com.donut.assignment2.data.local.dao.*
import com.donut.assignment2.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        ReportEntity::class,
        PhotoEntity::class,
        DefectEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun reportDao(): ReportDao
    abstract fun photoDao(): PhotoDao
    abstract fun defectDao(): DefectDao

    companion object {
        const val DATABASE_NAME = "field_inspection_db"
    }
}