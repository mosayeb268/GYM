package com.kineticcare.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class PosturalView {
    ANTERIOR,
    POSTERIOR,
    LATERAL_LEFT,
    LATERAL_RIGHT
}

@Serializable
data class PosturalFinding(
    val id: String? = null,
    val region: String,
    val anomalyName: String,
    val severity: String,
    val notes: String? = null
)

@Serializable
data class Assessment(
    val id: String,
    val clientId: String,
    val evaluatorId: String,
    val supervisorId: String? = null,
    val isApproved: Boolean = false,
    val assessedAtUtc: String,
    val notesFa: String? = null,
    val findings: List<PosturalFinding> = emptyList()
)

@Serializable
data class Exercise(
    val id: String,
    val titleFa: String,
    val muscleGroup: String,
    val videoUrl: String? = null,
    val cuesFa: String,
    val contraindications: List<String> = emptyList(),
    val regressionExerciseId: String? = null
)

@Serializable
enum class PaymentMethod {
    ONLINE_GATEWAY,
    POS_TERMINAL,
    CASH,
    WALLET
}

@Serializable
data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val userId: String,
    val titleFa: String,
    val amountToman: Int,
    val discountToman: Int = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.POS_TERMINAL,
    val paymentRef: String? = null,
    val paidAtUtc: String? = null
)
