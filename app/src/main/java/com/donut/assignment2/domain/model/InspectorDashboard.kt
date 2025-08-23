package com.donut.assignment2.domain.model


data class InspectorDashboard(
    val user: User,
    val draftReports: Int,
    val submittedReports: Int,
    val approvedReports: Int,
    val rejectedReports: Int,
    val recentReports: List<Report>
) {
    // Computed properties cho UI
    val totalReports: Int
        get() = draftReports + submittedReports + approvedReports + rejectedReports

    val pendingWork: Int
        get() = draftReports + rejectedReports // Reports cần inspector làm tiếp

    val completedWork: Int
        get() = approvedReports // Reports đã hoàn thành

    val inReview: Int
        get() = submittedReports // Reports đang chờ supervisor review

    // Progress calculations (0.0 to 1.0)
    val completionRate: Float
        get() = if (totalReports > 0) approvedReports.toFloat() / totalReports else 0f

    val pendingRate: Float
        get() = if (totalReports > 0) pendingWork.toFloat() / totalReports else 0f

    val inReviewRate: Float
        get() = if (totalReports > 0) inReview.toFloat() / totalReports else 0f

    // Status checks
    val hasWork: Boolean
        get() = totalReports > 0

    val hasPendingWork: Boolean
        get() = pendingWork > 0

    val isFirstTimeUser: Boolean
        get() = totalReports == 0

    val needsAttention: Boolean
        get() = rejectedReports > 0 // Có reports bị reject cần fix

    // Performance metrics
    val approvalRate: Float
        get() = if (approvedReports + rejectedReports > 0) {
            approvedReports.toFloat() / (approvedReports + rejectedReports)
        } else 0f

    // Helper methods
    fun getStatusSummary(): String {
        return when {
            isFirstTimeUser -> "Chưa có report nào"
            hasPendingWork -> "$pendingWork report cần hoàn thành"
            inReview > 0 -> "$inReview report đang chờ duyệt"
            else -> "Tất cả reports đã hoàn thành"
        }
    }

    fun getPriorityMessage(): String? {
        return when {
            rejectedReports > 0 -> "Có $rejectedReports report bị từ chối cần chỉnh sửa"
            draftReports > 0 -> "Có $draftReports draft report cần hoàn thiện"
            else -> null
        }
    }
}
