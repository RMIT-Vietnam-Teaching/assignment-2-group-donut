package com.donut.assignment2.data.repository

import com.donut.assignment2.data.local.dao.*
import com.donut.assignment2.data.mapper.*
import com.donut.assignment2.domain.model.*
import com.donut.assignment2.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao,
    private val photoDao: PhotoDao,
    private val defectDao: DefectDao,
    private val reportMapper: ReportMapper,
    private val photoMapper: PhotoMapper,
    private val defectMapper: DefectMapper
) : ReportRepository {

    override suspend fun createReport(report: Report): Result<String> {
        return try {
            val reportEntity = reportMapper.toEntity(report)
            reportDao.insertReport(reportEntity)
            Result.success(report.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReport(report: Report): Result<Unit> {
        return try {
            val reportEntity = reportMapper.toEntity(report)
            reportDao.updateReport(reportEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportById(id: String): Result<Report?> {
        return try {
            val reportEntity = reportDao.getReportById(id)
            if (reportEntity != null) {
                // Get photos and defects for this report
                val photoEntities = photoDao.getPhotosByReportId(id)
                val defectEntities = defectDao.getDefectsByReportId(id)

                val photos = photoEntities.map { photoMapper.fromEntity(it) }
                val defects = defectEntities.map { defectMapper.fromEntity(it) }

                val report = reportMapper.fromEntity(reportEntity).copy(
                    photos = photos,
                    defects = defects
                )
                Result.success(report)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReport(id: String): Result<Unit> {
        return try {
            reportDao.deleteReport(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportsByInspector(inspectorId: String): Result<List<Report>> {
        return try {
            val reportEntities = reportDao.getReportsByInspector(inspectorId)
            val reports = reportEntities.map { entity ->
                // Load related data for each report
                val photos = photoDao.getPhotosByReportId(entity.id)
                    .map { photoMapper.fromEntity(it) }
                val defects = defectDao.getDefectsByReportId(entity.id)
                    .map { defectMapper.fromEntity(it) }

                reportMapper.fromEntity(entity).copy(
                    photos = photos,
                    defects = defects
                )
            }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReportsByInspectorFlow(inspectorId: String): Flow<List<Report>> {
        return reportDao.getReportsByInspectorFlow(inspectorId)
            .map { entities -> reportMapper.fromEntities(entities) }
    }

    override suspend fun getAllReports(): Result<List<Report>> {
        return try {
            val reportEntities = reportDao.getAllReportsFlow()
            // Note: For flow, we'll keep it simple and not load related data
            // Individual report details will be loaded when needed
            Result.success(emptyList()) // This would need proper implementation
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingReportsFlow(): Flow<List<Report>> {
        return reportDao.getPendingReportsFlow()
            .map { entities -> reportMapper.fromEntities(entities) }
    }

    override fun getApprovedReportsFlow(): Flow<List<Report>> {
        return reportDao.getApprovedReportsFlow()
            .map { entities -> reportMapper.fromEntities(entities) }
    }

    override suspend fun updateReportStatus(reportId: String, status: ReportStatus): Result<Unit> {
        return try {
            reportDao.updateReportStatus(reportId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
