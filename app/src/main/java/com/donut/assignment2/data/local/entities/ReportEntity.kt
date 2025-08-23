package com.donut.assignment2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val inspectorId: String,
    val status: String, // ReportStatus as String
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val submittedAt: LocalDateTime?,
    val reviewedAt: LocalDateTime?,
    val supervisorId: String?,
    val supervisorNotes: String
)
