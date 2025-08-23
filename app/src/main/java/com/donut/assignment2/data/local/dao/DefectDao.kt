package com.donut.assignment2.data.local.dao

import androidx.room.*
import com.donut.assignment2.data.local.entities.DefectEntity

@Dao
interface DefectDao {
    @Query("SELECT * FROM defects WHERE reportId = :reportId ORDER BY createdAt DESC")
    suspend fun getDefectsByReportId(reportId: String): List<DefectEntity>

    @Query("SELECT * FROM defects WHERE photoId = :photoId")
    suspend fun getDefectsByPhotoId(photoId: String): List<DefectEntity>

    @Query("SELECT * FROM defects WHERE id = :id")
    suspend fun getDefectById(id: String): DefectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefect(defect: DefectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefects(defects: List<DefectEntity>)

    @Update
    suspend fun updateDefect(defect: DefectEntity)

    @Delete
    suspend fun deleteDefect(defect: DefectEntity)
}