package com.phuonghai.inspection.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uId: String = "",
    var fullName: String = "",
    var email: String? = null,
    var phoneNumber: String = "",
    var role: UserRole = UserRole.INSPECTOR,
    var supervisorId: String? = null,
    var profileImageUrl: String? = null
)

enum class UserRole { INSPECTOR, SUPERVISOR }
