package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisputeLog
import com.example.data.model.DisputeMessage
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.supabase.SupabasePlatformAnalytics
import com.example.ui.components.ChartBarData
import com.example.ui.components.DonutSliceData
import com.example.ui.components.RechartsBarChart
import com.example.ui.components.RechartsDonutChart
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.PayPalSky
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.AdminMetrics

@Composable
fun AdminDashboardScreen(
    metrics: AdminMetrics,
    disputeLogs: List<DisputeLog>,
    projects: List<Project>,
    milestones: List<Milestone>,
    disputeMessages: List<DisputeMessage>,
    supabaseAnalytics: SupabasePlatformAnalytics,
    onRefreshSupabase: () -> Unit,
    onSyncSupabase: () -> Unit,
    onOpenResolutionModal: (DisputeLog, Milestone?) -> Unit,
    onSendMessage: (projectId: String, text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var adminMessageInput by remember { mutableStateOf("") }
    var selectedDisputeId by remember(disputeLogs) {
        mutableStateOf(disputeLogs.firstOrNull()?.id)
    }

    val activeDispute = disputeLogs.firstOrNull { it.id == selectedDisputeId } ?: disputeLogs.firstOrNull()
    val associatedProject = projects.firstOrNull { it.id == activeDispute?.projectId }
    val associatedMilestone = milestones.firstOrNull { it.id == activeDispute?.milestoneId }

    // Chart data mapping from Supabase or live database
    val barChartData = remember(metrics, supabaseAnalytics) {
        val inEscrow = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.totalVolumeInEscrow else metrics.totalVolumeInEscrow
        val commission = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.totalCommissionCollected else metrics.totalCommissionEarned
        val disbursed = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.totalDisbursedToDevs else (milestones.filter { it.status == "released" }.sumOf { it.developerAmount })

        listOf(
            ChartBarData("In Escrow", inEscrow, Emerald500),
            ChartBarData("Revenue", commission, Color(0xFFA78BFA)),
            ChartBarData("Disbursed", disbursed, PayPalSky)
        )
    }

    val donutChartData = remember(milestones, supabaseAnalytics) {
        val funded = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.activeEscrowsCount else milestones.count { it.status == "escrow_funded" }
        val inReview = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.underReviewCount else milestones.count { it.status == "under_review" }
        val released = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.completedMilestonesCount else milestones.count { it.status == "released" }
        val disputed = if (supabaseAnalytics.isLiveFromSupabase) supabaseAnalytics.disputedCount else milestones.count { it.status == "disputed" }

        listOf(
            DonutSliceData("Funded", funded.toDouble(), Emerald500),
            DonutSliceData("Review", inReview.toDouble(), Amber500),
            DonutSliceData("Released", released.toDouble(), PayPalSky),
            DonutSliceData("Disputed", disputed.toDouble(), Rose500)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C3AED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Admin Control & Escrow Mediation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Dispute resolution, commission ledger & PayPal fund controls",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = Color(0xFF7C3AED).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "RBAC Active", tint = Color(0xFFA78BFA), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Admin Guard: Verified",
                            color = Color(0xFFDDD6FE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Platform Financial Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Volume in Escrow
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Volume in Escrow", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${metrics.totalVolumeInEscrow.toInt()}",
                            fontSize = 20.sp,
                            color = Emerald500,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Active PPCP holds", fontSize = 10.sp, color = Slate500)
                    }
                }

                // Total Platform Commission
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Platform Revenue (10%)", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${metrics.totalCommissionEarned.toInt()}",
                            fontSize = 20.sp,
                            color = Color(0xFFA78BFA),
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Retained on release", fontSize = 10.sp, color = Slate400)
                    }
                }

                // Active Disputes
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (metrics.activeDisputesCount > 0) Rose500.copy(alpha = 0.5f) else Slate800, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Disputes", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${metrics.activeDisputesCount}",
                            fontSize = 20.sp,
                            color = if (metrics.activeDisputesCount > 0) Rose500 else Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Mediation queue", fontSize = 10.sp, color = Slate500)
                    }
                }
            }
        }

        // ==================== SUPABASE POSTGREST ANALYTICS & RECHARTS ====================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_analytics_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Emerald500.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Supabase",
                                    tint = Emerald500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Supabase PostgREST Analytics",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (supabaseAnalytics.isLiveFromSupabase) Emerald500.copy(alpha = 0.2f) else Slate800,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (supabaseAnalytics.isLiveFromSupabase) "LIVE POSTGREST" else "STANDBY / REHYDRATING",
                                            color = if (supabaseAnalytics.isLiveFromSupabase) Emerald500 else Slate400,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Automated escrow telemetry fetched via PostgREST",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Refresh & Sync Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = onRefreshSupabase,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate800)
                                    .testTag("refresh_supabase_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Supabase",
                                    tint = Slate200,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onSyncSupabase,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald600.copy(alpha = 0.8f))
                                    .testTag("sync_escrow_to_supabase_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Local to Supabase",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (supabaseAnalytics.fetchErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Amber500.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Notice: ${supabaseAnalytics.fetchErrorMessage}. Showing synced local telemetry.",
                                    color = Amber500,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recharts Bar Chart: Financial Distribution
        item {
            RechartsBarChart(
                data = barChartData,
                title = "Escrow Capital Allocation (USD)",
                subtitle = "Recharts-style visualization: Escrow Balances, 10% Fees, & 90% Disbursements",
                modifier = Modifier.testTag("recharts_bar_chart")
            )
        }

        // Recharts Donut Chart: Milestone Status Breakdown
        item {
            RechartsDonutChart(
                slices = donutChartData,
                centerLabel = "Active Pipeline",
                centerValue = "${milestones.size}",
                modifier = Modifier.testTag("recharts_donut_chart")
            )
        }

        // Dispute Resolution Center Section Header
        item {
            Text(
                text = "Dispute Resolution Center",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        if (disputeLogs.isEmpty()) {
            item {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate800, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Emerald500, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Disputes Active", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("All milestones and escrows are proceeding smoothly.", color = Slate400, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Selected Dispute Investigation Card
            if (activeDispute != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (activeDispute.adminResolution == null) Rose500.copy(alpha = 0.6f) else Slate800, RoundedCornerShape(16.dp))
                            .testTag("active_dispute_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Case #${activeDispute.id.takeLast(6).uppercase()}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Surface(
                                    color = if (activeDispute.adminResolution == null) Rose500.copy(alpha = 0.2f) else Emerald600.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (activeDispute.adminResolution == null) "PENDING MEDIATION" else "RESOLVED",
                                        color = if (activeDispute.adminResolution == null) Rose500 else Emerald500,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = associatedProject?.title ?: "Project Dispute",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (associatedMilestone != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Milestone: ${associatedMilestone.title} • \$${associatedMilestone.amount.toInt()} Held in PayPal",
                                    color = Emerald500,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Claim Reason box
                            Surface(
                                color = Slate950,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Slate800, RoundedCornerShape(10.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Claimant: ${activeDispute.raisedBy}", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(activeDispute.reason, color = Slate300, fontSize = 12.sp, lineHeight = 16.sp)
                                }
                            }

                            // Deliverable / Project files inspector
                            if (associatedMilestone?.deliverableNote != null || associatedMilestone?.deliverableUrl != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Project Files & Deliverables Inspector", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Slate950,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        if (associatedMilestone.deliverableNote != null) {
                                            Text(associatedMilestone.deliverableNote, color = Slate300, fontSize = 11.sp)
                                        }
                                        if (associatedMilestone.deliverableUrl != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(associatedMilestone.deliverableUrl, color = Color(0xFF38BDF8), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Resolution findings if resolved
                            if (activeDispute.adminResolution != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = Emerald600.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = activeDispute.adminResolution,
                                        color = Emerald500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            } else {
                                // Override Action Buttons
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onOpenResolutionModal(activeDispute, associatedMilestone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("admin_adjudicate_button")
                                ) {
                                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Adjudicate Dispute (Force Release / Force Refund)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Chat Log & Activity Viewer
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                            .testTag("dispute_chat_log_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Mediation Activity & Chat Log",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Auditable communications thread between Client and Developer",
                                color = Slate400,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Message history
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                disputeMessages.forEach { msg ->
                                    val isClient = msg.senderRole == "client"
                                    val isAdmin = msg.senderRole == "admin"
                                    Surface(
                                        color = when {
                                            isAdmin -> Color(0xFF7C3AED).copy(alpha = 0.2f)
                                            isClient -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                            else -> Emerald600.copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${msg.senderName} (${msg.senderRole.uppercase()})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = when {
                                                        isAdmin -> Color(0xFFA78BFA)
                                                        isClient -> Color(0xFF38BDF8)
                                                        else -> Emerald500
                                                    }
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.message,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Send Admin mediation message
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = adminMessageInput,
                                    onValueChange = { adminMessageInput = it },
                                    placeholder = { Text("Post official mediation directive...", color = Slate500, fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF7C3AED),
                                        unfocusedBorderColor = Slate700,
                                        focusedContainerColor = Slate950,
                                        unfocusedContainerColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_message_input")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (adminMessageInput.isNotBlank() && activeDispute != null) {
                                            onSendMessage(activeDispute.projectId, adminMessageInput)
                                            adminMessageInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF7C3AED))
                                        .testTag("send_admin_message_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
