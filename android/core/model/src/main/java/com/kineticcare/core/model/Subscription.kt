package com.kineticcare.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SubscriptionType {
    TIME_BASED,
    SESSION_BASED,
    HYBRID
}

@Serializable
enum class SubscriptionStatus {
    ACTIVE,
    FROZEN,
    EXPIRED
}

@Serializable
data class Subscription(
    val id: String,
    val userId: String,
    val planTitle: String,
    val type: SubscriptionType,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val startDateUtc: String,
    val endDateUtc: String,
    val totalSessions: Int? = null,
    val remainingSessions: Int? = null,
    val includesClinical: Boolean = false,
    val priceToman: Int,
    val maxFreezeDays: Int = 15,
    val usedFreezeDays: Int = 0
)
