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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
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
import com.example.data.model.User
import com.example.ui.components.MilestonePipelineVisualizer
import com.example.ui.components.MilestoneStatusBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.PayPalSky
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.DeveloperMetrics

@Composable
fun DeveloperDashboardScreen(
    metrics: DeveloperMetrics,
    developerUser: User?,
    projects: List<Project>,
    milestones: List<Milestone>,
    onSubmitDeliverable: (Milestone) -> Unit,
    onManagePayPal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPayPalConnected = developerUser?.paypalConnected == true
    val merchantId = developerUser?.paypalMerchantId ?: "PMR-DEV-88291"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp)
            .testTag("developer_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // PayPal Account Connection Banner
            DeveloperPayPalBanner(
                isConnected = isPayPalConnected,
                merchantId = merchantId,
                onManage = onManagePayPal
            )
        }

        // Financial Overview (Guaranteed Escrow vs Total Earned Gross & Net)
        item {
            Text(
                text = "Developer Earnings & Guaranteed Escrow",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Guaranteed Escrow
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Emerald600.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Guaranteed Escrow", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${metrics.guaranteedEscrowBalance.toInt()}",
                            fontSize = 20.sp,
                            color = Emerald500,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Funds held in PayPal", fontSize = 10.sp, color = Emerald500)
                    }
                }

                // Total Earned Net (90%)
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Earned (Net)", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${metrics.totalEarnedNet.toInt()}",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Disbursed to PayPal", fontSize = 10.sp, color = Color(0xFF38BDF8))
                    }
                }

                // Gross
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Gross Contracted", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${metrics.totalEarnedGross.toInt()}",
                            fontSize = 20.sp,
                            color = Slate300,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("10% fee deducted", fontSize = 10.sp, color = Slate500)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Contracted Milestones",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${milestones.size} Milestones",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        // Active Milestones for Developer
        items(milestones) { milestone ->
            val project = projects.firstOrNull { it.id == milestone.projectId }
            DeveloperMilestoneCard(
                milestone = milestone,
                projectTitle = project?.title ?: "Contract Project",
                onSubmit = { onSubmitDeliverable(milestone) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DeveloperPayPalBanner(
    isConnected: Boolean,
    merchantId: String,
    onManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isConnected) Color(0xFF0F2537) else Color(0xFF2D1619),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isConnected) PayPalSky.copy(alpha = 0.5f) else Rose500.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .testTag("paypal_connection_banner")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) PayPalSky else Rose500),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isConnected) "PayPal Partner Onboarded" else "PayPal Connection Pending",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isConnected)
                            "Merchant ID: $merchantId • DELAY_FUNDS_DISBURSEMENT"
                        else
                            "Onboard via Partner Referral to receive delayed payouts",
                        color = if (isConnected) Color(0xFF38BDF8) else Color(0xFFFCA5A5),
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onManage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Slate800 else PayPalSky
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("manage_paypal_button")
            ) {
                Text(
                    text = if (isConnected) "Settings" else "Connect",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DeveloperMilestoneCard(
    milestone: Milestone,
    projectTitle: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .testTag("dev_milestone_card_${milestone.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Project title tag
            Text(
                text = projectTitle,
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))

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
                            text = "Payout: \$${milestone.developerAmount.toInt()} Net",
                            color = Emerald500,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(\$${milestone.amount.toInt()} gross)",
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                }
                MilestoneStatusBadge(status = milestone.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            MilestonePipelineVisualizer(status = milestone.status)
            Spacer(modifier = Modifier.height(12.dp))

            // Context details & Actions
            when (milestone.status) {
                "escrow_funded" -> {
                    Surface(
                        color = Color(0xFF064E3B).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Funds 100% secured in PayPal Escrow! Complete your code and submit deliverables.",
                                color = Emerald500,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("dev_submit_deliverables_button_${milestone.id}")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Deliverables for Review", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                "under_review" -> {
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Slate800, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("In Review by Client", color = Amber500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            if (milestone.deliverableNote != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(milestone.deliverableNote, color = Slate300, fontSize = 11.sp)
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
                                text = "\$${milestone.developerAmount.toInt()} disbursed to your PayPal balance.",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                "unfunded" -> {
                    Text(
                        text = "Waiting for client to fund escrow before work commences.",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }

                "disputed" -> {
                    Text(
                        text = "Client opened a dispute. Admin is reviewing evidence.",
                        color = Rose500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
