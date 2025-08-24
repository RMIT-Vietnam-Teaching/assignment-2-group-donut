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
class DefectRepositoryImpl @Inject constructor(
    private val defectDao: DefectDao,
    private val defectMapper: DefectMapper
) : DefectRepository {

    override suspend fun createDefect(defect: Defect): Result<String> {
        return try {
            val defectEntity = defectMapper.toEntity(defect)
            defectDao.insertDefect(defectEntity)
            Result.success(defect.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDefect(defect: Defect): Result<Unit> {
        return try {
            val defectEntity = defectMapper.toEntity(defect)
            defectDao.updateDefect(defectEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDefect(defectId: String): Result<Unit> {
        return try {
            val defectEntity = defectDao.getDefectById(defectId)
            if (defectEntity != null) {
                defectDao.deleteDefect(defectEntity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDefectById(defectId: String): Result<Defect?> {
        return try {
            val defectEntity = defectDao.getDefectById(defectId)
            val defect = defectEntity?.let { defectMapper.fromEntity(it) }
            Result.success(defect)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDefectsByReportId(reportId: String): Result<List<Defect>> {
        return try {
            val defectEntities = defectDao.getDefectsByReportId(reportId)
            val defects = defectEntities.map { defectMapper.fromEntity(it) }
            Result.success(defects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDefectsByPhotoId(photoId: String): Result<List<Defect>> {
        return try {
            val defectEntities = defectDao.getDefectsByPhotoId(photoId)
            val defects = defectEntities.map { defectMapper.fromEntity(it) }
            Result.success(defects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}