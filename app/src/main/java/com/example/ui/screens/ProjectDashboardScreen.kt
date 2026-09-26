package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisputeMessage
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import com.example.ui.components.EscrowTrustNotice
import com.example.ui.components.FeeSplitCard
import com.example.ui.components.MilestonePipelineVisualizer
import com.example.ui.components.MilestoneStatusBadge
import com.example.ui.components.RechartsMilestonePieBreakdownCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.ButtonYellow
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.OnButtonYellow
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.PayPalSky
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.UserRole

enum class MilestoneFilter(val label: String) {
    ALL("All Active"),
    ESCROW_HELD("In Escrow"),
    UNDER_REVIEW("Under Review"),
    UNFUNDED("Unfunded"),
    RELEASED("Released"),
    DISPUTED("Disputed")
}

enum class DashboardPanel(val label: String, val shortLabel: String) {
    WORK_SHOWCASE("Work Showcase & Review", "Work & Review"),
    COMMUNICATION("1-on-1 Chat Portal", "1-on-1 Chat"),
    ESCROW_PANEL("Escrow & Approvals", "Escrow"),
    ALL_MILESTONES("Milestone Tracker", "Milestones")
}

@Composable
fun ProjectDashboardScreen(
    projects: List<Project>,
    milestones: List<Milestone>,
    users: List<User>,
    currentUser: User,
    activeRole: UserRole,
    selectedProjectId: String?,
    feeCalculationMilestone: Milestone?,
    messages: List<DisputeMessage> = emptyList(),
    onSelectProject: (String?) -> Unit,
    onFundMilestone: (Milestone) -> Unit,
    onReleaseMilestone: (Milestone) -> Unit,
    onRequestRevision: (Milestone) -> Unit,
    onOpenDispute: (Milestone) -> Unit,
    onSubmitDeliverable: (Milestone) -> Unit,
    onToggleFeeCalculation: (Milestone?) -> Unit,
    onNewProject: () -> Unit,
    onOpenCommunicationPortal: (String?) -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onOpenPayPalDetails: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAccountManagement: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activePanel by remember { mutableStateOf(DashboardPanel.WORK_SHOWCASE) }
    var activeFilter by remember { mutableStateOf(MilestoneFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isProjectDetailsExpanded by remember { mutableStateOf(false) }
    var isSecurityBannerVisible by remember { mutableStateOf(true) }

    // Filter milestones based on selected project
    val projectFilteredMilestones = if (selectedProjectId != null) {
        milestones.filter { it.projectId == selectedProjectId }
    } else {
        milestones
    }

    // Secondary status filtering
    val statusFilteredMilestones = when (activeFilter) {
        MilestoneFilter.ALL -> projectFilteredMilestones
        MilestoneFilter.ESCROW_HELD -> projectFilteredMilestones.filter { it.status == "escrow_funded" }
        MilestoneFilter.UNDER_REVIEW -> projectFilteredMilestones.filter { it.status == "under_review" }
        MilestoneFilter.UNFUNDED -> projectFilteredMilestones.filter { it.status == "unfunded" }
        MilestoneFilter.RELEASED -> projectFilteredMilestones.filter { it.status == "released" }
        MilestoneFilter.DISPUTED -> projectFilteredMilestones.filter { it.status == "disputed" }
    }

    // Search query filtering
    val displayedMilestones = if (searchQuery.isBlank()) {
        statusFilteredMilestones
    } else {
        statusFilteredMilestones.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                (it.deliverableNote?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    // High level metrics
    val totalActiveMilestones = milestones.count { it.status != "released" }
    val totalEscrowSecured = milestones.filter { it.status == "escrow_funded" || it.status == "under_review" || it.status == "disputed" }
        .sumOf { it.amount }
    val totalUnderReviewCount = milestones.count { it.status == "under_review" }
    val totalReleasedCount = milestones.count { it.status == "released" }

    val activeProject = projects.firstOrNull { it.id == selectedProjectId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp)
            .testTag("project_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Refined Header & Direct Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeProject?.title ?: "Projects Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${displayedMilestones.size} milestones • \$${totalEscrowSecured.toInt()} locked in escrow",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                // Dedicated Quick Navigation Icons: Settings, Logout, Account Management
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Account Management icon button
                    IconButton(
                        onClick = onOpenAccountManagement,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate900)
                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                            .testTag("dashboard_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account Management",
                            tint = when (currentUser.role) {
                                "client" -> Color(0xFF38BDF8)
                                "developer" -> Emerald500
                                "admin" -> Color(0xFFA78BFA)
                                else -> Slate300
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Settings navigation icon button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate900)
                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Platform Settings",
                            tint = Slate300,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Logout navigation icon button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate900)
                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                            .testTag("dashboard_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Rose500,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // New Project action button (for clients and admins)
                    if (currentUser.role == "client" || currentUser.role == "admin") {
                        Button(
                            onClick = onNewProject,
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow, contentColor = OnButtonYellow),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("dashboard_new_project_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnButtonYellow)
                        }
                    }
                }
            }
        }

        // Refined User Perspective & Navigation Ribbon
        item {
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // User Perspective indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onOpenAccountManagement() }
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentUser.role) {
                                        "client" -> Color(0xFF0284C7)
                                        "developer" -> Emerald500
                                        "admin" -> Color(0xFF7C3AED)
                                        else -> Slate400
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.fullName.first().toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.fullName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = when (currentUser.role) {
                                        "client" -> Color(0xFF0284C7).copy(alpha = 0.2f)
                                        "developer" -> Emerald500.copy(alpha = 0.2f)
                                        "admin" -> Color(0xFF7C3AED).copy(alpha = 0.2f)
                                        else -> Slate700
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${currentUser.role.uppercase()} PERSPECTIVE",
                                        color = when (currentUser.role) {
                                            "client" -> Color(0xFF38BDF8)
                                            "developer" -> Emerald500
                                            "admin" -> Color(0xFFA78BFA)
                                            else -> Slate300
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "PayPal Delayed Disbursement Active • 10% Fee",
                                color = Slate400,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Quick navigation pill buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onOpenAccountManagement() }
                        ) {
                            Text(
                                text = "Account",
                                color = Slate300,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onOpenSettings() }
                        ) {
                            Text(
                                text = "Settings",
                                color = Slate300,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = Rose500.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onLogout() }
                        ) {
                            Text(
                                text = "Logout",
                                color = Rose500,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sleek, compact security strip (dismissible)
        if (isSecurityBannerVisible) {
            item {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald700.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PayPal Delayed Disbursement Active • 10% Platform Fee",
                                fontSize = 11.sp,
                                color = Color(0xFFD1FAE5)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Slate500,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { isSecurityBannerVisible = false }
                        )
                    }
                }
            }
        }

        // Streamlined high-level summary metrics bar
        item {
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Escrow Metric
                    Column {
                        Text("IN ESCROW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            text = "\$${totalEscrowSecured.toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Emerald500
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Slate800))

                    // In Review Metric
                    Column {
                        Text("IN REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            text = "$totalUnderReviewCount",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (totalUnderReviewCount > 0) Amber500 else Color.White
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Slate800))

                    // Active Tasks Metric
                    Column {
                        Text("ACTIVE TASKS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            text = "$totalActiveMilestones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Slate800))

                    // Completed Metric
                    Column {
                        Text("RELEASED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            text = "$totalReleasedCount",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = PayPalSky
                        )
                    }
                }
            }
        }

        // Recharts Visual Breakdown of Project Milestones & Escrow Distribution (when viewing all milestones)
        if (activePanel == DashboardPanel.ALL_MILESTONES) {
            item {
                RechartsMilestonePieBreakdownCard(
                    milestones = projectFilteredMilestones,
                    projectTitle = activeProject?.title,
                    modifier = Modifier.testTag("recharts_milestone_pie_chart")
                )
            }
        }

        // Project Filter Selector Chips
        item {
            Column {
                Text(
                    text = "SELECT PROJECT",
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Projects Chip
                    val isAllSelected = selectedProjectId == null
                    Surface(
                        color = if (isAllSelected) Emerald600.copy(alpha = 0.2f) else Slate900,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAllSelected) Emerald500 else Slate800
                        ),
                        modifier = Modifier
                            .clickable { onSelectProject(null) }
                            .testTag("filter_all_projects")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Projects (${projects.size})",
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) Emerald500 else Slate300
                            )
                        }
                    }

                    // Individual Project Chips
                    projects.forEach { project ->
                        val isSelected = selectedProjectId == project.id
                        Surface(
                            color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Slate900,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PayPalSky else Slate800
                            ),
                            modifier = Modifier
                                .clickable { onSelectProject(project.id) }
                                .testTag("filter_project_${project.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = project.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF38BDF8) else Slate300,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Project Overview Card (if a specific project is selected)
        if (activeProject != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeProject.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeProject.description,
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = if (isProjectDetailsExpanded) 5 else 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Emerald500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "\$${activeProject.totalBudget.toInt()} Contract",
                                    color = Emerald500,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val clientUser = users.firstOrNull { it.id == activeProject.clientId }
                            val devUser = users.firstOrNull { it.id == activeProject.developerId }

                            Text(
                                text = "Client: ${clientUser?.fullName?.split(" ")?.first() ?: "Client"}  •  Developer: ${devUser?.fullName?.split(" ")?.first() ?: "Alex"}",
                                color = Slate500,
                                fontSize = 10.sp
                            )

                            Text(
                                text = if (isProjectDetailsExpanded) "Show Less" else "Details",
                                color = PayPalSky,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { isProjectDetailsExpanded = !isProjectDetailsExpanded }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dedicated 3-Panel Segmented Navigation Bar
        item {
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "WORKSPACE PANELS",
                        color = Slate400,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DashboardPanel.values().forEach { panel ->
                            val isSelected = activePanel == panel
                            Surface(
                                color = if (isSelected) {
                                    when (panel) {
                                        DashboardPanel.WORK_SHOWCASE -> Color(0xFF0284C7)
                                        DashboardPanel.COMMUNICATION -> Emerald600
                                        DashboardPanel.ESCROW_PANEL -> PayPalBlue
                                        DashboardPanel.ALL_MILESTONES -> Slate800
                                    }
                                } else Slate950,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.35f) else Slate800
                                ),
                                modifier = Modifier
                                    .clickable { activePanel = panel }
                                    .testTag("dashboard_panel_tab_${panel.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (panel) {
                                            DashboardPanel.WORK_SHOWCASE -> Icons.Default.Code
                                            DashboardPanel.COMMUNICATION -> Icons.Default.Forum
                                            DashboardPanel.ESCROW_PANEL -> Icons.Default.AccountBalanceWallet
                                            DashboardPanel.ALL_MILESTONES -> Icons.Default.FilterList
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Slate400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = panel.label,
                                        color = if (isSelected) Color.White else Slate300,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // PANEL 1: WORK SHOWCASE & REVIEW
        if (activePanel == DashboardPanel.WORK_SHOWCASE) {
            item {
                WorkShowcasePanel(
                    project = activeProject,
                    milestones = projectFilteredMilestones,
                    currentUser = currentUser,
                    onSubmitDeliverable = onSubmitDeliverable,
                    onApproveDeliverable = onReleaseMilestone,
                    onRequestRevision = onRequestRevision,
                    onOpenDispute = onOpenDispute,
                    onContactParty = { onOpenCommunicationPortal(selectedProjectId) }
                )
            }
        }

        // PANEL 2: 1-ON-1 COMMUNICATION & PUBLIC PORTAL
        if (activePanel == DashboardPanel.COMMUNICATION) {
            item {
                CommunicationPortalPanel(
                    project = activeProject,
                    messages = messages,
                    currentUser = currentUser,
                    onOpenDedicatedPortal = { onOpenCommunicationPortal(selectedProjectId) },
                    onSendMessage = onSendMessage
                )
            }
        }

        // PANEL 3: ESCROW PANEL (BOTH SEE, ONLY CLIENT APPROVES)
        if (activePanel == DashboardPanel.ESCROW_PANEL) {
            item {
                EscrowPanel(
                    project = activeProject,
                    milestones = projectFilteredMilestones,
                    currentUser = currentUser,
                    onFundMilestone = onFundMilestone,
                    onApproveReleaseMilestone = onReleaseMilestone,
                    onOpenPayPalDetails = onOpenPayPalDetails
                )
            }
        }

        // PANEL 4: ALL MILESTONES PIPELINE TRACKER
        if (activePanel == DashboardPanel.ALL_MILESTONES) {
            // Milestone Status Filter Tabs & Search Field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search milestones by title or keyword...", color = Slate500, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Slate800
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("milestone_search_field")
                    )

                    // Status Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MilestoneFilter.values().forEach { filter ->
                            val isSelected = activeFilter == filter
                            val count = when (filter) {
                                MilestoneFilter.ALL -> projectFilteredMilestones.size
                                MilestoneFilter.ESCROW_HELD -> projectFilteredMilestones.count { it.status == "escrow_funded" }
                                MilestoneFilter.UNDER_REVIEW -> projectFilteredMilestones.count { it.status == "under_review" }
                                MilestoneFilter.UNFUNDED -> projectFilteredMilestones.count { it.status == "unfunded" }
                                MilestoneFilter.RELEASED -> projectFilteredMilestones.count { it.status == "released" }
                                MilestoneFilter.DISPUTED -> projectFilteredMilestones.count { it.status == "disputed" }
                            }

                            Surface(
                                color = if (isSelected) Slate800 else Slate900,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        when (filter) {
                                            MilestoneFilter.ESCROW_HELD -> Emerald500
                                            MilestoneFilter.UNDER_REVIEW -> Amber500
                                            MilestoneFilter.RELEASED -> PayPalSky
                                            MilestoneFilter.DISPUTED -> Rose500
                                            else -> Slate600
                                        }
                                    } else Slate800
                                ),
                                modifier = Modifier
                                    .clickable { activeFilter = filter }
                                    .testTag("status_filter_${filter.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (filter) {
                                                    MilestoneFilter.ALL -> Slate400
                                                    MilestoneFilter.ESCROW_HELD -> Emerald500
                                                    MilestoneFilter.UNDER_REVIEW -> Amber500
                                                    MilestoneFilter.UNFUNDED -> Slate500
                                                    MilestoneFilter.RELEASED -> Color(0xFF0284C7)
                                                    MilestoneFilter.DISPUTED -> Rose500
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${filter.label} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Slate400
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Title: Active Milestones List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milestones Pipeline (${displayedMilestones.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Sorted by order",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }
            }

            // Clean Milestones List
            if (displayedMilestones.isEmpty()) {
                item {
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No milestones found",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or selecting a different status filter.",
                                color = Slate400,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    activeFilter = MilestoneFilter.ALL
                                    searchQuery = ""
                                    onSelectProject(null)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset All Filters", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(displayedMilestones, key = { it.id }) { milestone ->
                    val project = projects.firstOrNull { it.id == milestone.projectId }
                    val isFeePopoverOpen = feeCalculationMilestone?.id == milestone.id

                    ProjectMilestoneListItem(
                        milestone = milestone,
                        projectTitle = project?.title ?: "Contract Project",
                        currentUserRole = currentUser.role,
                        activeRole = activeRole,
                        isFeePopoverOpen = isFeePopoverOpen,
                        onToggleFeeCalculation = { onToggleFeeCalculation(if (isFeePopoverOpen) null else milestone) },
                        onFund = { onFundMilestone(milestone) },
                        onRelease = { onReleaseMilestone(milestone) },
                        onRequestRevision = { onRequestRevision(milestone) },
                        onDispute = { onOpenDispute(milestone) },
                        onSubmitDeliverable = { onSubmitDeliverable(milestone) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProjectMilestoneListItem(
    milestone: Milestone,
    projectTitle: String,
    currentUserRole: String,
    activeRole: UserRole,
    isFeePopoverOpen: Boolean,
    onToggleFeeCalculation: () -> Unit,
    onFund: () -> Unit,
    onRelease: () -> Unit,
    onRequestRevision: () -> Unit,
    onDispute: () -> Unit,
    onSubmitDeliverable: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusAccentColor = when (milestone.status) {
        "escrow_funded" -> Emerald500
        "under_review" -> Amber500
        "released" -> Color(0xFF0284C7)
        "disputed" -> Rose500
        else -> Slate600
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("milestone_card_${milestone.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Status color stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(statusAccentColor)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                // Top Meta Row: Milestone sequence, Project Name & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TASK",
                                color = Slate400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = projectTitle,
                            color = Slate400,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    MilestoneStatusBadge(status = milestone.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Milestone Title
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Financial Breakdown Bar
                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Escrow", color = Slate500, fontSize = 9.sp)
                            Text(
                                text = "\$${milestone.amount.toInt()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Text("Developer Net (90%)", color = Slate500, fontSize = 9.sp)
                            Text(
                                text = "\$${milestone.developerAmount.toInt()}",
                                color = Emerald500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Text("Platform Fee (10%)", color = Slate500, fontSize = 9.sp)
                            Text(
                                text = "\$${milestone.commissionAmount.toInt()}",
                                color = Slate300,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }

                        IconButton(
                            onClick = onToggleFeeCalculation,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("fee_split_toggle_${milestone.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Fee Split Details",
                                tint = if (isFeePopoverOpen) Emerald500 else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Expandable Fee Split Card
                AnimatedVisibility(visible = isFeePopoverOpen) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        FeeSplitCard(milestone = milestone, onDismiss = onToggleFeeCalculation)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Milestone Progress Pipeline
                MilestonePipelineVisualizer(status = milestone.status)

                // Deliverable Details (if available)
                if (!milestone.deliverableNote.isNullOrBlank() || !milestone.deliverableUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = PayPalSky, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Submitted Deliverables",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!milestone.deliverableNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = milestone.deliverableNote,
                                    color = Slate300,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            if (!milestone.deliverableUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { /* external open */ }
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = PayPalSky, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = milestone.deliverableUrl,
                                        color = PayPalSky,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Review feedback if revision requested
                if (!milestone.reviewFeedback.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Amber500.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Revision Request: ${milestone.reviewFeedback}",
                                color = Amber500,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Role-Specific Action Controls
                val isClientPerspective = activeRole == UserRole.CLIENT || (currentUserRole == "client" && activeRole != UserRole.DEVELOPER)

                if (isClientPerspective) {
                    // =================== CLIENT ACTIONS ===================
                    when (milestone.status) {
                        "unfunded" -> {
                            Button(
                                onClick = onFund,
                                colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow, contentColor = OnButtonYellow),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fund_button_${milestone.id}")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fund Escrow via PayPal (\$${milestone.amount.toInt()})", fontWeight = FontWeight.Bold, color = OnButtonYellow)
                            }
                        }

                        "escrow_funded" -> {
                            Surface(
                                color = Emerald500.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Funds secured in PayPal Escrow. Developer is working on deliverables.",
                                        color = Emerald500,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        "under_review" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onRelease,
                                    colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow, contentColor = OnButtonYellow),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("release_button_${milestone.id}")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve Deliverables & Release Payout", fontWeight = FontWeight.Bold, color = OnButtonYellow)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onRequestRevision,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("revision_button_${milestone.id}")
                                    ) {
                                        Text("Request Revision", fontSize = 11.sp, color = Slate300)
                                    }

                                    OutlinedButton(
                                        onClick = onDispute,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("dispute_button_${milestone.id}")
                                    ) {
                                        Text("Open Dispute", fontSize = 11.sp, color = Rose500)
                                    }
                                }
                            }
                        }

                        "released" -> {
                            Surface(
                                color = Color(0xFF0284C7).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Milestone Approved. Funds Disbursed: \$${milestone.developerAmount.toInt()} (Dev Net) + \$${milestone.commissionAmount.toInt()} (Fee)",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        "disputed" -> {
                            Surface(
                                color = Rose500.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Escrow funds held in dispute. Platform administrator mediation active.",
                                        color = Rose500,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // =================== DEVELOPER ACTIONS ===================
                    when (milestone.status) {
                        "unfunded" -> {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Awaiting Client Escrow Deposit (\$${milestone.amount.toInt()}) before starting work.",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        "escrow_funded" -> {
                            Button(
                                onClick = onSubmitDeliverable,
                                colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow, contentColor = OnButtonYellow),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_deliverable_button_${milestone.id}")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Deliverables for Review", fontWeight = FontWeight.Bold, color = OnButtonYellow)
                            }
                        }

                        "under_review" -> {
                            Surface(
                                color = Amber500.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Deliverables submitted! Client is currently reviewing for payout approval.",
                                        color = Amber500,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        "released" -> {
                            Surface(
                                color = Emerald500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payout Completed: \$${milestone.developerAmount.toInt()} disbursed to your connected PayPal account.",
                                        color = Emerald500,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        "disputed" -> {
                            Surface(
                                color = Rose500.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Milestone disputed. Admin is reviewing deliverables and evidence.",
                                        color = Rose500,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
