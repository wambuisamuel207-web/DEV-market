package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DisputeLog
import com.example.data.model.DisputeMessage
import com.example.data.model.EscrowTransaction
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface DevMarketDao {
    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    // Projects
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: String): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<Project>)

    @Update
    suspend fun updateProject(project: Project)

    // Milestones
    @Query("SELECT * FROM milestones")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE projectId = :projectId")
    fun getMilestonesForProject(projectId: String): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE id = :id")
    fun getMilestoneById(id: String): Flow<Milestone?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<Milestone>)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    // Escrow Transactions
    @Query("SELECT * FROM escrow_transactions ORDER BY fundedAt DESC")
    fun getAllTransactions(): Flow<List<EscrowTransaction>>

    @Query("SELECT * FROM escrow_transactions WHERE milestoneId = :milestoneId")
    fun getTransactionForMilestone(milestoneId: String): Flow<EscrowTransaction?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: EscrowTransaction)

    @Update
    suspend fun updateTransaction(tx: EscrowTransaction)

    // Dispute Logs
    @Query("SELECT * FROM dispute_logs ORDER BY createdAt DESC")
    fun getAllDisputeLogs(): Flow<List<DisputeLog>>

    @Query("SELECT * FROM dispute_logs WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getDisputeLogsForProject(projectId: String): Flow<List<DisputeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisputeLog(log: DisputeLog)

    @Update
    suspend fun updateDisputeLog(log: DisputeLog)

    // Messages / Activity
    @Query("SELECT * FROM dispute_messages WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getMessagesForProject(projectId: String): Flow<List<DisputeMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DisputeMessage)
}
