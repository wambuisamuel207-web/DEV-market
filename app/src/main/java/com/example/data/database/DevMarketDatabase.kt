package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DevMarketDao
import com.example.data.model.DisputeLog
import com.example.data.model.DisputeMessage
import com.example.data.model.EscrowTransaction
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Project::class,
        Milestone::class,
        EscrowTransaction::class,
        DisputeLog::class,
        DisputeMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DevMarketDatabase : RoomDatabase() {
    abstract fun devMarketDao(): DevMarketDao

    companion object {
        @Volatile
        private var INSTANCE: DevMarketDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DevMarketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DevMarketDatabase::class.java,
                    "devmarket_database"
                )
                    .addCallback(DevMarketDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DevMarketDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.devMarketDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: DevMarketDao) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            // 1. Initial Users
            val users = listOf(
                User(
                    id = "usr_client_1",
                    email = "sarah@hypergrowth.io",
                    role = "client",
                    fullName = "Sarah Jenkins",
                    paypalMerchantId = null,
                    paypalConnected = false,
                    createdAt = now - day * 30
                ),
                User(
                    id = "usr_dev_1",
                    email = "alex@fullstackcraft.dev",
                    role = "developer",
                    fullName = "Alex Rivera",
                    paypalMerchantId = "PMR-DEV-88291",
                    paypalConnected = true,
                    createdAt = now - day * 60
                ),
                User(
                    id = "usr_admin_1",
                    email = "samuelgitau76@gmail.com",
                    role = "admin",
                    fullName = "Samuel Gitau (Platform Admin)",
                    paypalMerchantId = "PMR-ADMIN-MASTER-01",
                    paypalConnected = true,
                    createdAt = now - day * 120
                )
            )
            dao.insertUsers(users)

            // 2. Initial Projects
            val projects = listOf(
                Project(
                    id = "proj_1",
                    clientId = "usr_client_1",
                    developerId = "usr_dev_1",
                    title = "Enterprise SaaS Fintech Portal & PayPal Escrow Engine",
                    description = "Multi-tenant cloud billing dashboard with delayed disbursement integration, webhook listeners, and milestone reconciliation.",
                    totalBudget = 4500.0,
                    status = "active"
                ),
                Project(
                    id = "proj_2",
                    clientId = "usr_client_1",
                    developerId = "usr_dev_1",
                    title = "High-Throughput Vector Search & AI Inference Engine",
                    description = "Sub-50ms vector similarity clustering service built in Rust and gRPC with Python client bindings.",
                    totalBudget = 3200.0,
                    status = "disputed"
                )
            )
            dao.insertProjects(projects)

            // 3. Initial Milestones
            val milestones = listOf(
                // Project 1 milestones
                Milestone(
                    id = "m_1_1",
                    projectId = "proj_1",
                    title = "Phase 1: Architecture Blueprint & System Spec",
                    amount = 1000.0,
                    commissionAmount = 100.0,
                    developerAmount = 900.0,
                    status = "released",
                    deliverableNote = "Architecture spec diagrams, database schemas, and PayPal Commerce Platform API sequencing completed.",
                    deliverableUrl = "https://github.com/devmarket/fintech-portal/releases/tag/v0.1-spec",
                    submittedAt = now - day * 5,
                    reviewFeedback = "Approved by Sarah Jenkins. Funds disbursed atomically."
                ),
                Milestone(
                    id = "m_1_2",
                    projectId = "proj_1",
                    title = "Phase 2: Payment Escrow & Delayed Disbursement Module",
                    amount = 2000.0,
                    commissionAmount = 200.0,
                    developerAmount = 1800.0,
                    status = "under_review",
                    deliverableNote = "Completed delayed disbursement capture workflow with authorization hold, webhook handlers, and automated test suite.",
                    deliverableUrl = "https://github.com/devmarket/fintech-portal/pull/42",
                    submittedAt = now - 3600000L * 2,
                    reviewFeedback = null
                ),
                Milestone(
                    id = "m_1_3",
                    projectId = "proj_1",
                    title = "Phase 3: Production Deployment & Security Hardening",
                    amount = 1500.0,
                    commissionAmount = 150.0,
                    developerAmount = 1350.0,
                    status = "unfunded",
                    deliverableNote = null,
                    deliverableUrl = null,
                    submittedAt = null,
                    reviewFeedback = null
                ),

                // Project 2 milestones
                Milestone(
                    id = "m_2_1",
                    projectId = "proj_2",
                    title = "Phase 1: Core Indexing Engine & Vector Cluster",
                    amount = 1800.0,
                    commissionAmount = 180.0,
                    developerAmount = 1620.0,
                    status = "disputed",
                    deliverableNote = "Delivered core Rust crate, HNSW indexing algorithm, and initial benchmark results.",
                    deliverableUrl = "https://github.com/devmarket/vector-search/pull/12",
                    submittedAt = now - day * 3,
                    reviewFeedback = "Client flagged latency discrepancies."
                ),
                Milestone(
                    id = "m_2_2",
                    projectId = "proj_2",
                    title = "Phase 2: gRPC Microservice & Cloud Deployment",
                    amount = 1400.0,
                    commissionAmount = 140.0,
                    developerAmount = 1260.0,
                    status = "unfunded",
                    deliverableNote = null,
                    deliverableUrl = null,
                    submittedAt = null,
                    reviewFeedback = null
                )
            )
            dao.insertMilestones(milestones)

            // 4. Initial Escrow Transactions
            dao.insertTransaction(
                EscrowTransaction(
                    id = "tx_1_1",
                    milestoneId = "m_1_1",
                    paypalCaptureId = "CAP-PP-990142-DISBURSED",
                    status = "RELEASED",
                    amount = 1000.0,
                    commissionAmount = 100.0,
                    developerAmount = 900.0,
                    fundedAt = now - day * 6,
                    releasedAt = now - day * 4
                )
            )
            dao.insertTransaction(
                EscrowTransaction(
                    id = "tx_1_2",
                    milestoneId = "m_1_2",
                    paypalCaptureId = "CAP-PP-884210-DELAYED",
                    status = "HELD",
                    amount = 2000.0,
                    commissionAmount = 200.0,
                    developerAmount = 1800.0,
                    fundedAt = now - day * 2,
                    releasedAt = null
                )
            )
            dao.insertTransaction(
                EscrowTransaction(
                    id = "tx_2_1",
                    milestoneId = "m_2_1",
                    paypalCaptureId = "CAP-PP-771923-DISPUTE-HOLD",
                    status = "HELD",
                    amount = 1800.0,
                    commissionAmount = 180.0,
                    developerAmount = 1620.0,
                    fundedAt = now - day * 4,
                    releasedAt = null
                )
            )

            // 5. Initial Dispute Log
            dao.insertDisputeLog(
                DisputeLog(
                    id = "disp_1",
                    projectId = "proj_2",
                    milestoneId = "m_2_1",
                    raisedBy = "Sarah Jenkins (Client)",
                    reason = "Query latency on 1M vectors measures 310ms instead of the contracted <50ms threshold. Developer requested release without profiling fixes.",
                    adminResolution = null,
                    createdAt = now - day * 2
                )
            )

            // 6. Dispute Messages
            dao.insertMessage(
                DisputeMessage(
                    id = "msg_1",
                    projectId = "proj_2",
                    senderId = "usr_client_1",
                    senderName = "Sarah Jenkins",
                    senderRole = "client",
                    message = "Hi Alex, the milestone benchmark shows 310ms latency on the sample dataset. The contract explicitly specified <50ms at P95.",
                    createdAt = now - day * 2 - 3600000L * 4
                )
            )
            dao.insertMessage(
                DisputeMessage(
                    id = "msg_2",
                    projectId = "proj_2",
                    senderId = "usr_dev_1",
                    senderName = "Alex Rivera",
                    senderRole = "developer",
                    message = "Sarah, the test machine was a standard 2-vCPU droplet without AVX2 extensions enabled. On modern 8-core hardware with AVX-512 it benchmarked at 38ms.",
                    createdAt = now - day * 2 - 3600000L * 2
                )
            )
            dao.insertMessage(
                DisputeMessage(
                    id = "msg_3",
                    projectId = "proj_2",
                    senderId = "usr_client_1",
                    senderName = "Sarah Jenkins",
                    senderRole = "client",
                    message = "We need this guaranteed on commodity instances. Escrow funds are held until verified on our staging cluster. I have opened an admin mediation request.",
                    createdAt = now - day * 2
                )
            )
        }
    }
}
