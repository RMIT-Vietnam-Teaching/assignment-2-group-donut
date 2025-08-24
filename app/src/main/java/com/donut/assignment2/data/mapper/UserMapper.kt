package com.donut.assignment2.data.mapper

import com.donut.assignment2.data.local.entities.UserEntity
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset // Assuming UTC, adjust if needed

// Extension function to convert Long (timestamp) to LocalDateTime
fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
}

// Extension function to convert LocalDateTime to Long (timestamp)
fun LocalDateTime.toEpochMilli(): Long {
    return this.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
}
// Firestore specific mapping functions
fun DocumentSnapshot.toUser(): User {

    return User(
        phoneNumber = getString("phoneNumber") ?: "",
        fullName = getString("displayName") ?: "",
        email = getString("email") ?: "",
        role = UserRole.valueOf(getString("role") ?: "INSPECTOR"),
        supervisorPhone = getString("supervisorPhone"),
     )
}

fun User.toFirestoreMap(): Map<String, Any?> {
    return hashMapOf(
        "phoneNumber" to phoneNumber,
        "displayName" to fullName,
        "email" to email,
        "role" to role.name,
        "supervisorPhone" to supervisorPhone,
    )
}

// Map from UserEntity to domain User
fun UserEntity.toDomain(): User {
    return User(
        phoneNumber = phoneNumber,
        fullName = fullName,
        email = email,
        role = UserRole.valueOf(role), // Convert String to UserRole enum
        supervisorPhone = supervisorPhone
    )
}

// Map from domain User to UserEntity
fun User.toEntity(): UserEntity {
    return UserEntity(
        phoneNumber = phoneNumber,
        fullName = fullName,
        email = email,
        role = role.name, // Convert UserRole enum to String
        supervisorPhone = supervisorPhone
    )
}