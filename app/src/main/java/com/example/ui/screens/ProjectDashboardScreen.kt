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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import com.example.ui.components.EscrowTrustNotice
import com.example.ui.components.FeeSplitCard
import com.example.ui.components.MilestonePipelineVisualizer
import com.example.ui.components.MilestoneStatusBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
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

@Composable
fun ProjectDashboardScreen(
    projects: List<Project>,
    milestones: List<Milestone>,
    users: List<User>,
    currentUser: User,
    activeRole: UserRole,
    selectedProjectId: String?,
    feeCalculationMilestone: Milestone?,
    onSelectProject: (String?) -> Unit,
    onFundMilestone: (Milestone) -> Unit,
    onReleaseMilestone: (Milestone) -> Unit,
    onRequestRevision: (Milestone) -> Unit,
    onOpenDispute: (Milestone) -> Unit,
    onSubmitDeliverable: (Milestone) -> Unit,
    onToggleFeeCalculation: (Milestone?) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeFilter by remember { mutableStateOf(MilestoneFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isProjectDetailsExpanded by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Screen Header & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Project Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = when (activeRole) {
                                UserRole.CLIENT -> Color(0xFF0284C7).copy(alpha = 0.2f)
                                UserRole.DEVELOPER -> Emerald500.copy(alpha = 0.2f)
                                UserRole.ADMIN -> Color(0xFF7C3AED).copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (activeRole) {
                                    UserRole.CLIENT -> Color(0xFF38BDF8).copy(alpha = 0.5f)
                                    UserRole.DEVELOPER -> Emerald500.copy(alpha = 0.5f)
                                    UserRole.ADMIN -> Color(0xFFA78BFA).copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Text(
                                text = "${activeRole.label} View",
                                color = when (activeRole) {
                                    UserRole.CLIENT -> Color(0xFF38BDF8)
                                    UserRole.DEVELOPER -> Emerald500
                                    UserRole.ADMIN -> Color(0xFFA78BFA)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Active milestone tracking, escrow delivery & status verification",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                if (currentUser.role == "client" || currentUser.role == "admin") {
                    Button(
                        onClick = onNewProject,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("dashboard_new_project_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Project", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Trust & Delayed Disbursement Guarantee Banner
        item {
            EscrowTrustNotice()
        }

        // High-level summary metrics cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Escrow Value Card
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Emerald600.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("In Escrow", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${totalEscrowSecured.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Emerald500
                        )
                        Text("Delayed disbursement", fontSize = 9.sp, color = Slate500)
                    }
                }

                // Under Review Card
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (totalUnderReviewCount > 0) Amber500.copy(alpha = 0.5f) else Slate800,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Amber500, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("In Review", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalUnderReviewCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (totalUnderReviewCount > 0) Amber500 else Color.White
                        )
                        Text("Awaiting client check", fontSize = 9.sp, color = Slate500)
                    }
                }

                // Active Milestones Count
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = PayPalSky, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Active Tasks", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalActiveMilestones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text("$totalReleasedCount completed", fontSize = 9.sp, color = Slate500)
                    }
                }
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
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fund_button_${milestone.id}")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fund Escrow via PayPal (\$${milestone.amount.toInt()})", fontWeight = FontWeight.Bold, color = Color.White)
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("release_button_${milestone.id}")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve Deliverables & Release Payout", fontWeight = FontWeight.Bold, color = Color.White)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_deliverable_button_${milestone.id}")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Deliverables for Review", fontWeight = FontWeight.Bold, color = Color.White)
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
