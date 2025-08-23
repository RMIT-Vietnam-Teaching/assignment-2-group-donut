package com.example.a2_adrian.data


import java.time.LocalDate

enum class ReportStatus { PASSED, FAILED, NEEDS }

data class Report(
    val id: String,
    val title: String,
    val date: String,
    val status: ReportStatus,
    val inspectionType: String,
    val inspector: String,
    val score: Int,
    val outcome: String,
    val notes: String,
    val synced: Boolean,
    val attachments: List<String>, // giả URL/đường dẫn
    val latitude: Double,
    val longitude: Double
)
