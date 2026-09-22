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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.ui.components.EscrowTrustNotice
import com.example.ui.components.FeeSplitCard
import com.example.ui.components.MilestonePipelineVisualizer
import com.example.ui.components.MilestoneStatusBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
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
import com.example.ui.viewmodel.ClientMetrics

@Composable
fun ClientDashboardScreen(
    metrics: ClientMetrics,
    projects: List<Project>,
    milestones: List<Milestone>,
    selectedProjectId: String?,
    feeCalculationMilestone: Milestone?,
    onSelectProject: (String) -> Unit,
    onFundMilestone: (Milestone) -> Unit,
    onReleaseMilestone: (Milestone) -> Unit,
    onRequestRevision: (Milestone) -> Unit,
    onOpenDispute: (Milestone) -> Unit,
    onToggleFeeCalculation: (Milestone?) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject = projects.firstOrNull { it.id == selectedProjectId } ?: projects.firstOrNull()
    val projectMilestones = if (activeProject != null) {
        milestones.filter { it.projectId == activeProject.id }
    } else {
        emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp)
            .testTag("client_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Trust Notice Banner
            EscrowTrustNotice()
        }

        // 1. Active Escrows Summary Card
        item {
            Text(
                text = "Client Financial Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Funded
                SummaryMetricCard(
                    title = "Total in Escrow",
                    amount = "\$${metrics.totalEscrowFunded.toInt()}",
                    subtitle = "Delayed Disbursement",
                    accentColor = Emerald500,
                    modifier = Modifier.weight(1f)
                )

                // Pending Release
                SummaryMetricCard(
                    title = "In Review",
                    amount = "\$${metrics.totalPendingReview.toInt()}",
                    subtitle = "Awaiting Approval",
                    accentColor = Amber500,
                    modifier = Modifier.weight(1f)
                )

                // Completed
                SummaryMetricCard(
                    title = "Released",
                    amount = "\$${metrics.totalReleased.toInt()}",
                    subtitle = "Disbursed to Devs",
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Fee Split Popover if active
        if (feeCalculationMilestone != null) {
            item {
                FeeSplitCard(
                    milestone = feeCalculationMilestone,
                    onDismiss = { onToggleFeeCalculation(null) }
                )
            }
        }

        // 2. Project Workspace Selector & Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Project Workspace",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Track milestone deliverables & authorize disbursements",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
                Button(
                    onClick = onNewProject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("post_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Projects selector pill row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(projects) { proj ->
                    val isSelected = proj.id == activeProject?.id
                    Surface(
                        color = if (isSelected) Color(0xFF0284C7) else Slate900,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectProject(proj.id) }
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Slate800, RoundedCornerShape(10.dp))
                            .testTag("project_pill_${proj.id}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                text = proj.title.take(28) + if (proj.title.length > 28) "..." else "",
                                color = if (isSelected) Color.White else Slate300,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "\$${proj.totalBudget.toInt()} Budget • ${proj.status.uppercase()}",
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Slate500,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Active project details banner
        if (activeProject != null) {
            item {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeProject.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = if (activeProject.status == "disputed") Rose500.copy(alpha = 0.2f) else Emerald600.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = activeProject.status.uppercase(),
                                    color = if (activeProject.status == "disputed") Rose500 else Emerald500,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (activeProject.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeProject.description,
                                color = Slate400,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Milestones list for active project
        item {
            Text(
                text = "Milestones & Escrow Pipeline",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        items(projectMilestones) { milestone ->
            ClientMilestoneCard(
                milestone = milestone,
                onFund = { onFundMilestone(milestone) },
                onRelease = { onReleaseMilestone(milestone) },
                onRequestRevision = { onRequestRevision(milestone) },
                onOpenDispute = { onOpenDispute(milestone) },
                onShowFeeSplit = { onToggleFeeCalculation(milestone) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    amount: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = subtitle, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ClientMilestoneCard(
    milestone: Milestone,
    onFund: () -> Unit,
    onRelease: () -> Unit,
    onRequestRevision: () -> Unit,
    onOpenDispute: () -> Unit,
    onShowFeeSplit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .testTag("milestone_card_${milestone.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title, Amount & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = milestone.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\$${milestone.amount.toInt()}.00 USD",
                            color = Emerald500,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fee Split ℹ️",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onShowFeeSplit() }
                        )
                    }
                }
                MilestoneStatusBadge(status = milestone.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Milestone Status Pipeline
            MilestonePipelineVisualizer(status = milestone.status)

            Spacer(modifier = Modifier.height(14.dp))

            // Deliverables review box if submitted
            if (milestone.status == "under_review" || milestone.deliverableNote != null) {
                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate800, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Developer Deliverables", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        if (milestone.deliverableNote != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = milestone.deliverableNote,
                                color = Slate300,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        if (milestone.deliverableUrl != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = milestone.deliverableUrl,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Review feedback notice if revision was requested
            if (milestone.reviewFeedback != null) {
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = milestone.reviewFeedback,
                        color = Slate300,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Contextual Action Buttons
            when (milestone.status) {
                "unfunded" -> {
                    Button(
                        onClick = onFund,
                        colors = ButtonDefaults.buttonColors(containerColor = PayPalSky),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("fund_escrow_button_${milestone.id}")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fund Escrow (\$${milestone.amount.toInt()} Held in PayPal)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                "escrow_funded" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Awaiting Developer Deliverables", color = Slate300, fontSize = 12.sp)
                        }
                        Text(
                            text = "Open Dispute",
                            color = Rose500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { onOpenDispute() }
                                .padding(4.dp)
                                .testTag("open_dispute_button_${milestone.id}")
                        )
                    }
                }

                "under_review" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Approve & Release
                        Button(
                            onClick = onRelease,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("approve_release_button_${milestone.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve & Release Payment", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRequestRevision,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("request_revision_button_${milestone.id}")
                            ) {
                                Text("Request Revision", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = onOpenDispute,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_dispute_review_button_${milestone.id}")
                            ) {
                                Text("Open Dispute", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                "released" -> {
                    Surface(
                        color = Color(0xFF0369A1).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Funds successfully disbursed to Developer's PayPal balance.",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                "disputed" -> {
                    Surface(
                        color = Rose500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dispute active. Admin investigation underway in Resolution Center.",
                                color = Rose500,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
