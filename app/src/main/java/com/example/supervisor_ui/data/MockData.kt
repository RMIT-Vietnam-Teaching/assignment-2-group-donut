package com.example.supervisor_ui.data

object MockData {
    val inspectionItems = listOf(
        InspectionItem(
            id = 1,
            title = "Electrical Panel Inspection",
            description = "Routine inspection of electrical panel and circuit breakers in main building.",
            type = "Electrical",
            status = InspectionStatus.PENDING_REVIEW,
            date = "August 15",
            location = "Main Building",
            action = InspectionAction.APPROVE
        ),
        InspectionItem(
            id = 2,
            title = "Fire Alarm System Test",
            description = "Monthly testing of fire alarm system and emergency evacuation procedures.",
            type = "Fire",
            status = InspectionStatus.PASSED,
            date = "August 18",
            location = "All Floors",
            action = InspectionAction.REJECT

        ),
        InspectionItem(
            id = 3,
            title = "Safety Equipment Check",
            description = "Verification of safety equipment including first aid kits and safety gear.",
            type = "Safety",
            status = InspectionStatus.FAILED,
            date = "August 12",
            location = "Warehouse",
            action = InspectionAction.APPROVE

        ),
        InspectionItem(
            id = 4,
            title = "Structural Assessment",
            description = "Annual structural integrity assessment of building foundation and supports.",
            type = "Structural",
            status = InspectionStatus.NEEDS_ATTENTION,
            date = "August 25",
            location = "Building Foundation",
            action = InspectionAction.REJECT

        ),
        InspectionItem(
            id = 5,
            title = "Electrical Panel Inspection",
            description = "Electrical problems in warehouse, problems with underground internet wires.",
            type = "Electrical",
            status = InspectionStatus.PASSED,
            date = "August 15",
            location = "Warehouse",
            action = InspectionAction.APPROVE
        ),
        InspectionItem(
            id = 6,
            title = "Structural Failure",
            description = "Structure collapsed due to earthquake, needs immediate attention.",
            type = "Structural",
            status = InspectionStatus.NEEDS_ATTENTION,
            date = "August 15",
            location = "Warehouse",
            action = InspectionAction.NONE
        ),
        InspectionItem(
            id = 7,
            title = "Electrical Panel Inspection",
            description = "Electrical problems in warehouse, problems with underground internet wires.",
            type = "Fire",
            status = InspectionStatus.PASSED,
            date = "August 12",
            location = "Main Building",
            action = InspectionAction.NONE
        )
    )
}