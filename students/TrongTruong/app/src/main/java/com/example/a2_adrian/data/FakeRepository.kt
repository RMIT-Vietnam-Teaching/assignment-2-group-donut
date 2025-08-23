package com.example.a2_adrian.data


import java.time.LocalDate

object FakeRepository {
    val reports = listOf(
        Report(
            id = "A-102",
            title = "Report A-102",
            date = "January 24, 2024",     // <- đổi thành chuỗi
            status = ReportStatus.PASSED,
            inspectionType = "Safety",
            inspector = "Jane Doe / INSP-00123",
            score = 92,
            outcome = "Satisfactory",
            notes = "Minor issues found on aisle 3; corrective action documented.",
            synced = true,
            attachments = listOf("img1", "img2", "img3"),
            latitude = 10.775, longitude = 106.700
        ),
        Report(
            id = "B-305",
            title = "Report B-305",
            date = "January 24, 2024",     // <- đổi thành chuỗi
            status = ReportStatus.FAILED,
            inspectionType = "Quality",
            inspector = "John Smith / INSP-88771",
            score = 58,
            outcome = "Failed",
            notes = "Critical item out of spec. Re-inspection required.",
            synced = false,
            attachments = listOf("img1"),
            latitude = 10.776, longitude = 106.695
        ),
        Report(
            id = "C-212",
            title = "Report C-212",
            date = "January 24, 2024",     // <- đổi thành chuỗi
            status = ReportStatus.NEEDS,
            inspectionType = "Maintenance",
            inspector = "Jane Doe / INSP-00123",
            score = 74,
            outcome = "Needs Attention",
            notes = "Schedule maintenance within 7 days.",
            synced = false,
            attachments = emptyList(),
            latitude = 10.777, longitude = 106.702
        )
    )

    fun getById(id: String): Report? = reports.find { it.id == id }
}
