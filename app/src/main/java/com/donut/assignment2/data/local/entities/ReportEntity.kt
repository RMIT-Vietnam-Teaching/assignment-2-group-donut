package com.donut.assignment2.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "reports",
    indices = [
        Index(value = ["inspectorPhone"]),
        Index(value = ["supervisorPhone"]),
        Index(value = ["status"])
    ]
)
data class ReportEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val inspectorPhone: String,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val submittedAt: LocalDateTime?,
    val reviewedAt: LocalDateTime?,
    val supervisorPhone: String?,
    val supervisorNotes: String
)
