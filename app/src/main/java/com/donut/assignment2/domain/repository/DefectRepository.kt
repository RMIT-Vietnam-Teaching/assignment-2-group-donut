package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.*

interface DefectRepository {
    suspend fun createDefect(defect: Defect): Result<String>
    suspend fun updateDefect(defect: Defect): Result<Unit>
    suspend fun deleteDefect(defectId: String): Result<Unit>
    suspend fun getDefectById(defectId: String): Result<Defect?>
    suspend fun getDefectsByReportId(reportId: String): Result<List<Defect>>
    suspend fun getDefectsByPhotoId(photoId: String): Result<List<Defect>>
}