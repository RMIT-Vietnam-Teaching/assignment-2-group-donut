package com.donut.assignment2.data.local.entities

import androidx.room.*
import java.time.LocalDateTime

@Entity(
    tableName = "defects",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId"), Index("photoId")]
)
data class DefectEntity(
    @PrimaryKey
    val id: String,
    val reportId: String,
    val photoId: String,
    val type: String,
    val severity: String,
    val description: String,
    val confidence: Float,
    val isMLDetected: Boolean,
    val isVerified: Boolean,
    val createdAt: LocalDateTime
)