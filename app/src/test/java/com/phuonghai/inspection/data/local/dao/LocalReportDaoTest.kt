package com.phuonghai.inspection.data.local.dao

import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.phuonghai.inspection.data.local.database.AppDatabase
import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalReportDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: LocalReportDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.localReportDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun trimReports_keepsUnsyncedWhenOverLimit() = runBlocking {
        val inspectorId = "inspector1"
        // Insert 33 synced reports
        for (i in 1..33) {
            dao.insertReport(
                LocalReportEntity(
                    reportId = i.toString(),
                    inspectorId = inspectorId,
                    taskId = "task$i",
                    title = "title$i",
                    description = "desc$i",
                    type = "ELECTRICAL",
                    lat = "0",
                    lng = "0",
                    address = "addr$i",
                    score = null,
                    priority = "NORMAL",
                    assignStatus = "PENDING_REVIEW",
                    responseStatus = "PENDING",
                    syncStatus = "SYNCED",
                    imageUrl = "",
                    videoUrl = "",
                    reviewNotes = "",
                    reviewedBy = "",
                    createdAt = i.toLong(),
                    completedAt = null,
                    needsSync = false
                )
            )
        }
        // Insert 2 unsynced reports
        for (i in 34..35) {
            dao.insertReport(
                LocalReportEntity(
                    reportId = i.toString(),
                    inspectorId = inspectorId,
                    taskId = "task$i",
                    title = "title$i",
                    description = "desc$i",
                    type = "ELECTRICAL",
                    lat = "0",
                    lng = "0",
                    address = "addr$i",
                    score = null,
                    priority = "NORMAL",
                    assignStatus = "PENDING_REVIEW",
                    responseStatus = "PENDING",
                    syncStatus = "UNSYNCED",
                    imageUrl = "",
                    videoUrl = "",
                    reviewNotes = "",
                    reviewedBy = "",
                    createdAt = i.toLong(),
                    completedAt = null,
                    needsSync = true
                )
            )
        }

        val unsyncedCount = dao.getUnsyncedReportsCountForInspector(inspectorId)
        dao.trimReports(inspectorId, 30 + unsyncedCount)

        val remaining = dao.getReportsByInspectorId(inspectorId).first()
        val syncedCount = remaining.count { !it.needsSync }
        val remainingUnsynced = remaining.count { it.needsSync }

        assertEquals(30, syncedCount)
        assertEquals(2, remainingUnsynced)
        assertEquals(32, remaining.size)
    }
}
