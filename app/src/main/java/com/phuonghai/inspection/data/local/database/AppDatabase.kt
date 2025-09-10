package com.phuonghai.inspection.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3, // Increment version for new fields
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localReportDao(): LocalReportDao
    abstract fun localTaskDao(): LocalTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 2 to 3 (add new fields to local_tasks)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new fields to local_tasks table for enhanced offline support
                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN cacheTimestamp INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN lastSyncAttempt INTEGER NOT NULL DEFAULT 0
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN syncRetryCount INTEGER NOT NULL DEFAULT 0
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN localModifiedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN localNotes TEXT NOT NULL DEFAULT ''
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN originalStatus TEXT NOT NULL DEFAULT ''
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN isLocalOnly INTEGER NOT NULL DEFAULT 0
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN isDownloaded INTEGER NOT NULL DEFAULT 1
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                try {
                    database.execSQL("""
                        ALTER TABLE local_tasks 
                        ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                    """)
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }

                // Add indexes for better performance
                try {
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS idx_local_tasks_inspector_id 
                        ON local_tasks(inspectorId)
                    """)
                } catch (e: Exception) {
                    // Index might already exist, ignore
                }

                try {
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS idx_local_tasks_cache_timestamp 
                        ON local_tasks(cacheTimestamp)
                    """)
                } catch (e: Exception) {
                    // Index might already exist, ignore
                }

                try {
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS idx_local_tasks_needs_sync 
                        ON local_tasks(needsSync)
                    """)
                } catch (e: Exception) {
                    // Index might already exist, ignore
                }

                // Add indexes for local_reports if not exist
                try {
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS idx_local_reports_inspector_id 
                        ON local_reports(inspectorId)
                    """)
                } catch (e: Exception) {
                    // Index might already exist, ignore
                }

                try {
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS idx_local_reports_needs_sync 
                        ON local_reports(needsSync)
                    """)
                } catch (e: Exception) {
                    // Index might already exist, ignore
                }

                try {
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS idx_local_reports_sync_status 
                        ON local_reports(syncStatus)
                    """)
                } catch (e: Exception) {
                    // Index might already exist, ignore
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inspection_database"
                )
                    .addMigrations(MIGRATION_2_3) // Add migration
                    .fallbackToDestructiveMigration() // Fallback for development
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Method to clear database if needed (for testing/development)
        fun clearDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                context.deleteDatabase("inspection_database")
                INSTANCE = null
            }
        }
    }
}