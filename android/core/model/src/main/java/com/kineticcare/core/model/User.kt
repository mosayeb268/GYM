package com.kineticcare.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    SUPER_ADMIN,
    CLINICAL_DIRECTOR,
    CORRECTIVE_TRAINER,
    FITNESS_COACH,
    NUTRITIONIST,
    RECEPTIONIST,
    ATHLETE_CLIENT
}

@Serializable
data class User(
    val id: String,
    val phoneNumber: String,
    val nationalCode: String? = null,
    val fullName: String,
    val role: UserRole = UserRole.ATHLETE_CLIENT,
    val avatarUrl: String? = null,
    val gender: String? = null,
    val emergencyContact: String? = null,
    val medicalHistory: String? = null,
    val walletBalanceToman: Int = 0,
    val isActive: Boolean = true
)
