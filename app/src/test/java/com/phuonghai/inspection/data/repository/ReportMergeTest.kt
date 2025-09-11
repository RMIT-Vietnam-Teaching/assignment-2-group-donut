package com.phuonghai.inspection.data.repository

import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import com.phuonghai.inspection.data.local.entity.toLocalEntity
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportMergeTest {

    private class InMemoryReportStore {
        private val reports = mutableMapOf<String, LocalReportEntity>()

        suspend fun getReportById(id: String): LocalReportEntity? = reports[id]
        suspend fun insertReport(entity: LocalReportEntity) { reports[entity.reportId] = entity }
        fun snapshot(id: String): LocalReportEntity? = reports[id]
    }

    private suspend fun mergeRemoteReports(remote: List<Report>, store: InMemoryReportStore) {
        remote.forEach { report ->
            val local = store.getReportById(report.reportId)
            if (local?.needsSync == true) return@forEach
            store.insertReport(report.copy(syncStatus = SyncStatus.SYNCED).toLocalEntity())
        }
    }

    @Test
    fun unsynced_local_edits_persist_after_sync() = runBlocking {
        val store = InMemoryReportStore()
        val local = LocalReportEntity(
            reportId = "r1",
            inspectorId = "ins1",
            taskId = "t1",
            title = "Local Title",
            description = "Local Description",
            type = InspectionType.ELECTRICAL.name,
            lat = "0",
            lng = "0",
            address = "addr",
            score = null,
            priority = Priority.NORMAL.name,
            assignStatus = AssignStatus.DRAFT.name,
            responseStatus = ResponseStatus.PENDING.name,
            syncStatus = SyncStatus.UNSYNCED.name,
            imageUrl = "",
            videoUrl = "",
            reviewNotes = "",
            reviewedBy = "",
            createdAt = 0L,
            completedAt = null,
            needsSync = true
        )
        store.insertReport(local)

        val remote = Report(
            reportId = "r1",
            inspectorId = "ins1",
            taskId = "t1",
            title = "Remote Title"
        )

        mergeRemoteReports(listOf(remote), store)

        val result = store.snapshot("r1")
        assertTrue(result!!.needsSync)
        assertEquals("Local Title", result.title)
    }
}

