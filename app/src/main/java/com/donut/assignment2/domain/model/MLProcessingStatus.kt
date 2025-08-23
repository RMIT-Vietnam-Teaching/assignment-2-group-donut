package com.donut.assignment2.domain.model

enum class MLProcessingStatus {
    PENDING,     // Chưa xử lý
    PROCESSING,  // Đang xử lý ML
    COMPLETED,   // Đã hoàn thành ML
    FAILED       // ML thất bại
}