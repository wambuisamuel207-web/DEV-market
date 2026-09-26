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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import com.example.ui.components.MilestoneStatusBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.ButtonYellow
import com.example.ui.theme.ButtonYellowHover
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
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkShowcasePanel(
    project: Project?,
    milestones: List<Milestone>,
    currentUser: User,
    onSubmitDeliverable: (Milestone) -> Unit,
    onApproveDeliverable: (Milestone) -> Unit,
    onRequestRevision: (Milestone) -> Unit,
    onOpenDispute: (Milestone) -> Unit,
    onContactParty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClient = currentUser.role == "client"
    val isDeveloper = currentUser.role == "developer"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("work_showcase_panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Panel Header & Strict Role Access Bar
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Work Showcase",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Work Showcase & Deliverables Review",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Developers submit code & artifacts • Clients review and approve",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // STRICT ACCESS CONTROL NOTICE
                Surface(
                    color = if (isClient) Color(0xFF0369A1).copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isClient) Color(0xFF38BDF8).copy(alpha = 0.4f) else Amber500.copy(alpha = 0.4f)
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
                            tint = if (isClient) Color(0xFF38BDF8) else Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isClient) {
                                "Client Review Authority: You have exclusive permissions to inspect code deliverables, request revisions, or release payment."
                            } else {
                                "Developer Mode: Showcase your work deliverables below. Review authority is strictly reserved for clients only."
                            },
                            color = if (isClient) Color(0xFF38BDF8) else Amber500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Deliverables List / Work Cards
        val milestonesWithWork = milestones.filter {
            !it.deliverableNote.isNullOrBlank() || !it.deliverableUrl.isNullOrBlank() || it.status == "escrow_funded" || it.status == "under_review"
        }

        if (milestonesWithWork.isEmpty()) {
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = Slate600, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No submitted deliverables yet.", color = Slate400, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("When milestones are funded in escrow, developers showcase their work here.", color = Slate500, fontSize = 11.sp)
                }
            }
        } else {
            milestonesWithWork.forEach { milestone ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                        .testTag("showcase_card_${milestone.id}"),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title & Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Milestone Value: \$${milestone.amount.toInt()} (Net Payout: \$${milestone.developerAmount.toInt()})",
                                    color = Emerald500,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            MilestoneStatusBadge(status = milestone.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Showcase Details Box
                        Surface(
                            color = Slate950,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = PayPalSky, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Developer Work Showcase",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                if (!milestone.deliverableNote.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = milestone.deliverableNote,
                                        color = Slate300,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                if (!milestone.deliverableUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { /* external open */ }
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = PayPalSky, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = milestone.deliverableUrl,
                                            color = PayPalSky,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (milestone.submittedAt != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val timeStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(milestone.submittedAt))
                                    Text(
                                        text = "Submitted by developer: $timeStr",
                                        color = Slate500,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Revision Feedback (if any)
                        if (!milestone.reviewFeedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Amber500.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = milestone.reviewFeedback,
                                        color = Amber500,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ROLE-BASED REVIEW PERMISSION LOGIC
                        if (isClient) {
                            // CLIENT PERSPECTIVE: Can actively review and approve!
                            if (milestone.status == "under_review") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onApproveDeliverable(milestone) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ButtonYellow,
                                            contentColor = OnButtonYellow
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("client_approve_deliverable_${milestone.id}")
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Approve Work & Authorize Escrow Payout", fontWeight = FontWeight.Bold, color = OnButtonYellow)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onRequestRevision(milestone) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("client_request_revision_${milestone.id}")
                                        ) {
                                            Text("Request Revisions", fontSize = 11.sp, color = Slate300)
                                        }

                                        OutlinedButton(
                                            onClick = { onOpenDispute(milestone) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.5f)),
                                            modifier = Modifier.weight(1f).testTag("client_dispute_${milestone.id}")
                                        ) {
                                            Text("Open Dispute", fontSize = 11.sp, color = Rose500)
                                        }
                                    }
                                }
                            } else if (milestone.status == "released") {
                                Surface(
                                    color = Emerald500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Approved by Client • Escrow Released: \$${milestone.developerAmount.toInt()}", color = Emerald500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // DEVELOPER PERSPECTIVE: DEVELOPER CANNOT REVIEW!
                            if (milestone.status == "under_review") {
                                Surface(
                                    color = Slate950,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Review Authority: Client Only. Developers cannot review or approve their own deliverables.",
                                            color = Slate400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else if (milestone.status == "escrow_funded") {
                                Button(
                                    onClick = { onSubmitDeliverable(milestone) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ButtonYellow,
                                        contentColor = OnButtonYellow
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("dev_submit_deliverable_${milestone.id}")
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, tint = OnButtonYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Showcase Work: Submit Deliverables", fontWeight = FontWeight.Bold, color = OnButtonYellow)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
