package com.example.data.repository

import com.example.data.dao.DevMarketDao
import com.example.data.database.DevMarketDatabase
import com.example.data.model.DisputeLog
import com.example.data.model.DisputeMessage
import com.example.data.model.EscrowTransaction
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

enum class DisputeResolutionType {
    FORCE_RELEASE,
    FORCE_REFUND
}

class DevMarketRepository(
    private val dao: DevMarketDao
) {
    val allProjects: Flow<List<Project>> = dao.getAllProjects()
    val allMilestones: Flow<List<Milestone>> = dao.getAllMilestones()
    val allTransactions: Flow<List<EscrowTransaction>> = dao.getAllTransactions()
    val allDisputeLogs: Flow<List<DisputeLog>> = dao.getAllDisputeLogs()
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    fun getMilestonesForProject(projectId: String): Flow<List<Milestone>> =
        dao.getMilestonesForProject(projectId)

    fun getMessagesForProject(projectId: String): Flow<List<DisputeMessage>> =
        dao.getMessagesForProject(projectId)

    fun getDisputeLogsForProject(projectId: String): Flow<List<DisputeLog>> =
        dao.getDisputeLogsForProject(projectId)

    suspend fun fundMilestoneEscrow(milestone: Milestone, captureIdPrefix: String = "CAP-PP") {
        val commission = milestone.amount * 0.10
        val developerPayout = milestone.amount * 0.90
        val captureId = "$captureIdPrefix-${System.currentTimeMillis().toString().takeLast(6)}-DELAYED"

        val updatedMilestone = milestone.copy(
            status = "escrow_funded",
            commissionAmount = commission,
            developerAmount = developerPayout,
            reviewFeedback = null
        )
        dao.updateMilestone(updatedMilestone)

        val tx = EscrowTransaction(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            milestoneId = milestone.id,
            paypalCaptureId = captureId,
            status = "HELD",
            amount = milestone.amount,
            commissionAmount = commission,
            developerAmount = developerPayout,
            fundedAt = System.currentTimeMillis(),
            releasedAt = null
        )
        dao.insertTransaction(tx)
    }

    suspend fun submitDeliverables(
        milestoneId: String,
        deliverableNote: String,
        deliverableUrl: String
    ) {
        val current = dao.getMilestoneById(milestoneId).firstOrNull() ?: return
        val updated = current.copy(
            status = "under_review",
            deliverableNote = deliverableNote,
            deliverableUrl = deliverableUrl,
            submittedAt = System.currentTimeMillis(),
            reviewFeedback = null
        )
        dao.updateMilestone(updated)
    }

    suspend fun requestRevision(
        milestoneId: String,
        revisionNotes: String
    ) {
        val current = dao.getMilestoneById(milestoneId).firstOrNull() ?: return
        val updated = current.copy(
            status = "escrow_funded",
            reviewFeedback = "Revision Requested: $revisionNotes"
        )
        dao.updateMilestone(updated)
    }

    suspend fun approveAndReleasePayment(milestoneId: String) {
        val current = dao.getMilestoneById(milestoneId).firstOrNull() ?: return
        val updated = current.copy(
            status = "released",
            reviewFeedback = "Approved by Client. Released via PayPal Delayed Disbursement (90% to developer, 10% platform fee)."
        )
        dao.updateMilestone(updated)

        val tx = dao.getTransactionForMilestone(milestoneId).firstOrNull()
        if (tx != null) {
            dao.updateTransaction(
                tx.copy(
                    status = "RELEASED",
                    releasedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun openDispute(
        milestone: Milestone,
        reason: String,
        raisedByName: String,
        raisedById: String
    ) {
        val updated = milestone.copy(status = "disputed")
        dao.updateMilestone(updated)

        val disputeLog = DisputeLog(
            id = "disp_${UUID.randomUUID().toString().take(8)}",
            projectId = milestone.projectId,
            milestoneId = milestone.id,
            raisedBy = "$raisedByName",
            reason = reason,
            adminResolution = null,
            createdAt = System.currentTimeMillis()
        )
        dao.insertDisputeLog(disputeLog)

        dao.insertMessage(
            DisputeMessage(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                projectId = milestone.projectId,
                senderId = raisedById,
                senderName = raisedByName,
                senderRole = if (raisedById.contains("client")) "client" else "developer",
                message = "DISPUTE OPENED: $reason",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun adminResolveDispute(
        disputeLogId: String,
        milestoneId: String,
        resolutionType: DisputeResolutionType,
        notes: String
    ) {
        val milestone = dao.getMilestoneById(milestoneId).firstOrNull()
        val tx = dao.getTransactionForMilestone(milestoneId).firstOrNull()

        when (resolutionType) {
            DisputeResolutionType.FORCE_RELEASE -> {
                if (milestone != null) {
                    dao.updateMilestone(
                        milestone.copy(
                            status = "released",
                            reviewFeedback = "Admin Override: Force Released to Developer. $notes"
                        )
                    )
                }
                if (tx != null) {
                    dao.updateTransaction(
                        tx.copy(
                            status = "RELEASED",
                            releasedAt = System.currentTimeMillis()
                        )
                    )
                }
                val allLogs = dao.getAllDisputeLogs().firstOrNull() ?: emptyList()
                val log = allLogs.firstOrNull { it.id == disputeLogId }
                if (log != null) {
                    dao.updateDisputeLog(
                        log.copy(adminResolution = "FORCE RELEASED TO DEVELOPER: $notes")
                    )
                }
            }

            DisputeResolutionType.FORCE_REFUND -> {
                if (milestone != null) {
                    dao.updateMilestone(
                        milestone.copy(
                            status = "unfunded",
                            reviewFeedback = "Admin Override: Force Refunded to Client. $notes"
                        )
                    )
                }
                if (tx != null) {
                    dao.updateTransaction(
                        tx.copy(
                            status = "REFUNDED",
                            releasedAt = System.currentTimeMillis()
                        )
                    )
                }
                val allLogs = dao.getAllDisputeLogs().firstOrNull() ?: emptyList()
                val log = allLogs.firstOrNull { it.id == disputeLogId }
                if (log != null) {
                    dao.updateDisputeLog(
                        log.copy(adminResolution = "FORCE REFUNDED TO CLIENT: $notes")
                    )
                }
            }
        }
    }

    suspend fun sendDisputeMessage(
        projectId: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        message: String
    ) {
        val msg = DisputeMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            projectId = projectId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            message = message,
            createdAt = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
    }

    suspend fun updateDeveloperPayPalStatus(
        userId: String,
        connected: Boolean,
        merchantId: String?
    ) {
        val user = dao.getUserById(userId).firstOrNull() ?: return
        dao.updateUser(
            user.copy(
                paypalConnected = connected,
                paypalMerchantId = merchantId
            )
        )
    }

    suspend fun updateUserPayPalDetails(
        userId: String,
        email: String,
        merchantId: String?,
        connected: Boolean
    ) {
        val user = dao.getUserById(userId).firstOrNull() ?: return
        dao.updateUser(
            user.copy(
                email = email.trim(),
                paypalMerchantId = merchantId?.trim(),
                paypalConnected = connected
            )
        )
    }

    suspend fun createNewProject(
        clientId: String,
        developerId: String,
        title: String,
        description: String,
        milestones: List<Pair<String, Double>>
    ) {
        val projectId = "proj_${UUID.randomUUID().toString().take(6)}"
        val total = milestones.sumOf { it.second }
        val proj = Project(
            id = projectId,
            clientId = clientId,
            developerId = developerId,
            title = title,
            description = description,
            totalBudget = total,
            status = "active"
        )
        dao.insertProjects(listOf(proj))

        val createdMilestones = milestones.mapIndexed { idx, pair ->
            val amt = pair.second
            Milestone(
                id = "m_${projectId}_${idx + 1}",
                projectId = projectId,
                title = pair.first,
                amount = amt,
                commissionAmount = amt * 0.10,
                developerAmount = amt * 0.90,
                status = "unfunded"
            )
        }
        dao.insertMilestones(createdMilestones)
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        role: String
    ): User {
        val newUser = User(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            email = email.trim().lowercase(),
            role = role,
            fullName = fullName.trim(),
            paypalMerchantId = if (role == "developer") "PPMERCH-${System.currentTimeMillis().toString().takeLast(6)}" else null,
            createdAt = System.currentTimeMillis()
        )
        dao.insertUsers(listOf(newUser))
        return newUser
    }

    suspend fun resetToSampleData() {
        DevMarketDatabase.populateInitialData(dao)
    }
}
