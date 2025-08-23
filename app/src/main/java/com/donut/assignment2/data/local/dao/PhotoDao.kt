package com.donut.assignment2.data.local.dao

import androidx.room.*
import com.donut.assignment2.data.local.entities.PhotoEntity

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE reportId = :reportId ORDER BY timestamp DESC")
    suspend fun getPhotosByReportId(reportId: String): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: String): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}