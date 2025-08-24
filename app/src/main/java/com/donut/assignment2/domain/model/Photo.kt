package com.donut.assignment2.domain.model

import java.time.LocalDateTime

data class Photo(
    val id: String = "",
    val reportId: String = "",
    val filePath: String = "",
    val fileName: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isProcessedByML: Boolean = false
)