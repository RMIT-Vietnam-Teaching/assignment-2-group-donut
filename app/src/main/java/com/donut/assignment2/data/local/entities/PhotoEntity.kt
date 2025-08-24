package com.donut.assignment2.data.local.entities

import androidx.room.*
import java.time.LocalDateTime

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId")]
)
data class PhotoEntity(
    @PrimaryKey
    val id: String,
    val reportId: String,
    val filePath: String,
    val fileName: String,
    val timestamp: LocalDateTime,
    val latitude: Double?,
    val longitude: Double?,
    val isProcessedByML: Boolean
)