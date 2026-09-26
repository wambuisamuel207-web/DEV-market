package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
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
import com.example.data.model.User
import com.example.ui.components.MilestoneStatusBadge
import com.example.ui.components.RechartsMilestonePieBreakdownCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.ButtonYellow
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.OnButtonYellow
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

@Composable
fun EscrowPanel(
    project: Project?,
    milestones: List<Milestone>,
    currentUser: User,
    onFundMilestone: (Milestone) -> Unit,
    onApproveReleaseMilestone: (Milestone) -> Unit,
    onOpenPayPalDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClient = currentUser.role == "client"
    val isDeveloper = currentUser.role == "developer"

    val totalEscrowLocked = milestones
        .filter { it.status == "escrow_funded" || it.status == "under_review" || it.status == "disputed" }
        .sumOf { it.amount }
    val totalReleased = milestones.filter { it.status == "released" }.sumOf { it.amount }
    val devNetEscrow = totalEscrowLocked * 0.90
    val platformCommission = totalEscrowLocked * 0.10

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("escrow_panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Panel Header & Authority Tag
        Surface(
            color = Slate900,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PayPalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Escrow Vault",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PayPal Delayed Disbursement Escrow",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Both client and developer see funds • Only client can approve",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = if (isClient) Emerald500.copy(alpha = 0.2f) else Slate800,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isClient) Emerald500 else Slate700)
                    ) {
                        Text(
                            text = if (isClient) "CLIENT APPROVER" else "READ-ONLY DEVELOPER",
                            color = if (isClient) Emerald500 else Slate300,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Permissible Action Notice
                Surface(
                    color = if (isClient) Color(0xFF0369A1).copy(alpha = 0.15f) else Slate950,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isClient) Color(0xFF38BDF8).copy(alpha = 0.3f) else Slate800
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isClient) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isClient) Color(0xFF38BDF8) else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isClient) {
                                "Approval Authority: As the client, you have the exclusive power to authorize and release escrow payouts once deliverables meet specifications."
                            } else {
                                "Escrow Transparency: You have full visibility into locked balances and fee breakdowns. Approval and disbursement authorization is restricted exclusively to the Client."
                            },
                            color = if (isClient) Color(0xFF38BDF8) else Slate300,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // PAYPAL DETAILS & SETUP GUIDANCE CARD (Directly answers user question)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PayPalBlue.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .testTag("paypal_guidance_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = PayPalSky, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PayPal Details & Setup Status",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        color = if (currentUser.paypalConnected) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (currentUser.paypalConnected) "Connected" else "Action Needed",
                            color = if (currentUser.paypalConnected) Emerald500 else Amber500,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isDeveloper) {
                        "Are you needed to fill PayPal details? YES — Developers need a linked PayPal email or Merchant ID so your 90% payout is automatically disbursed when the client approves your work."
                    } else {
                        "Are you needed to fill PayPal details? YES — Clients need a connected PayPal account or business wallet to fund project milestones into delayed disbursement escrow."
                    },
                    color = Slate300,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Linked Email: ${currentUser.email}", color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Merchant ID: ${currentUser.paypalMerchantId ?: "Not Configured"}", color = Slate400, fontSize = 10.sp)
                        }

                        Button(
                            onClick = onOpenPayPalDetails,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonYellow,
                                contentColor = OnButtonYellow
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("manage_paypal_button")
                        ) {
                            Text("Manage PayPal Details", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnButtonYellow)
                        }
                    }
                }
            }
        }

        // Metrics Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total in Escrow", color = Slate400, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\$${totalEscrowLocked.toInt()}", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Delayed Hold", color = Slate500, fontSize = 9.sp)
                }
            }

            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Dev Guaranteed (90%)", color = Slate400, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\$${devNetEscrow.toInt()}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Disbursement Net", color = Slate500, fontSize = 9.sp)
                }
            }

            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Released", color = Slate400, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\$${totalReleased.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Completed", color = Emerald500, fontSize = 9.sp)
                }
            }
        }

        // Recharts Milestone Pie Breakdown
        RechartsMilestonePieBreakdownCard(
            milestones = milestones,
            projectTitle = project?.title ?: "All Projects"
        )

        // Escrow Milestones List with Explicit Approval Controls
        Text(
            text = "Milestone Escrow Ledger & Approvals",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        milestones.forEach { milestone ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .testTag("escrow_item_${milestone.id}"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = milestone.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = "Contract: \$${milestone.amount.toInt()} • Dev Net: \$${milestone.developerAmount.toInt()} • Fee: \$${milestone.commissionAmount.toInt()}",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                        MilestoneStatusBadge(status = milestone.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // APPROVAL CONTROLS: CLIENT ONLY!
                    if (isClient) {
                        // CLIENT APPROVAL PERMISSION
                        when (milestone.status) {
                            "unfunded" -> {
                                Button(
                                    onClick = { onFundMilestone(milestone) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ButtonYellow,
                                        contentColor = OnButtonYellow
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("escrow_fund_button_${milestone.id}")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fund Escrow via PayPal (\$${milestone.amount.toInt()})", color = OnButtonYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            "under_review" -> {
                                Button(
                                    onClick = { onApproveReleaseMilestone(milestone) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ButtonYellow,
                                        contentColor = OnButtonYellow
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("escrow_approve_button_${milestone.id}")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Client Approve: Release Escrow Payout", color = OnButtonYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            "escrow_funded" -> {
                                Surface(
                                    color = Emerald500.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Escrow Secured. Developer is actively implementing deliverables.", color = Emerald500, fontSize = 10.sp)
                                    }
                                }
                            }

                            "released" -> {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Approved by Client: Disbursed \$${milestone.developerAmount.toInt()} to Developer.", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // DEVELOPER VIEW: CANNOT APPROVE
                        when (milestone.status) {
                            "under_review" -> {
                                Surface(
                                    color = Slate950,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Client Approval Required: Only the Client can authorize escrow disbursement. Developers cannot approve release.",
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            "escrow_funded" -> {
                                Surface(
                                    color = Emerald500.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Emerald500, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Escrow Protected: \$${milestone.amount.toInt()} secured in PayPal Vault for your work.", color = Emerald500, fontSize = 10.sp)
                                    }
                                }
                            }

                            "released" -> {
                                Surface(
                                    color = Emerald500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Payout Received: \$${milestone.developerAmount.toInt()} disbursed to your connected PayPal account.", color = Emerald500, fontSize = 10.sp)
                                    }
                                }
                            }

                            "unfunded" -> {
                                Surface(
                                    color = Slate950,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Slate500, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Awaiting Client funding before starting work.", color = Slate500, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
