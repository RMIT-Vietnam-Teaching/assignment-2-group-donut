package com.donut.assignment2.domain.model

import java.time.LocalDateTime

data class Defect(
    val id: String = "",
    val reportId: String = "",
    val photoId: String = "",
    val type: DefectType = DefectType.OTHER,
    val severity: DefectSeverity = DefectSeverity.LOW,
    val description: String = "",
    val confidence: Float = 0.0f,
    val isMLDetected: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class DefectType {
    RUST,       // Rỉ sét
    CRACK,      // Nứt
    CORROSION,  // Ăn mòn
    DAMAGE,     // Hư hỏng
    OTHER       // Khác
}

enum class DefectSeverity {
    LOW,        // Thấp
    MEDIUM,     // Trung bình
    HIGH,       // Cao
    CRITICAL    // Nghiêm trọng
}
