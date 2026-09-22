package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val role: String, // "client", "developer", "admin"
    val fullName: String,
    val paypalMerchantId: String? = null,
    val paypalConnected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String,
    val clientId: String,
    val developerId: String,
    val title: String,
    val description: String = "",
    val totalBudget: Double,
    val status: String = "active" // "active", "completed", "disputed", "draft"
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val amount: Double,
    val commissionAmount: Double, // 10%
    val developerAmount: Double, // 90%
    val status: String = "unfunded", // 'unfunded', 'escrow_funded', 'under_review', 'released', 'disputed'
    val deliverableNote: String? = null,
    val deliverableUrl: String? = null,
    val submittedAt: Long? = null,
    val reviewFeedback: String? = null
)

@Entity(tableName = "escrow_transactions")
data class EscrowTransaction(
    @PrimaryKey val id: String,
    val milestoneId: String,
    val paypalCaptureId: String,
    val status: String, // "HELD", "RELEASED", "REFUNDED"
    val amount: Double,
    val commissionAmount: Double,
    val developerAmount: Double,
    val fundedAt: Long = System.currentTimeMillis(),
    val releasedAt: Long? = null
)

@Entity(tableName = "dispute_logs")
data class DisputeLog(
    @PrimaryKey val id: String,
    val projectId: String,
    val milestoneId: String?,
    val raisedBy: String,
    val reason: String,
    val adminResolution: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dispute_messages")
data class DisputeMessage(
    @PrimaryKey val id: String,
    val projectId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String, // "client", "developer", "admin"
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)
