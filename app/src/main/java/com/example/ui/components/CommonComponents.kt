package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Milestone
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.UserRole

@Composable
fun RoleSwitcherBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    userRole: String? = null,
    modifier: Modifier = Modifier
) {
    // Developers and clients must NOT access the admin page
    val isAdminUser = userRole == "admin"

    Surface(
        color = Slate900,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .testTag("role_switcher_bar")
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val visibleRoles = if (isAdminUser) {
                UserRole.values().toList()
            } else {
                // If user is client, only allow Client & Ledger
                // If user is developer, only allow Developer & Ledger
                // Neither client nor developer can see/switch to Admin
                when (userRole) {
                    "developer" -> listOf(UserRole.DEVELOPER)
                    "client" -> listOf(UserRole.CLIENT)
                    else -> listOf(UserRole.CLIENT, UserRole.DEVELOPER)
                }
            }

            visibleRoles.forEach { role ->
                val isSelected = currentRole == role
                val bgColor = if (isSelected) {
                    when (role) {
                        UserRole.CLIENT -> Color(0xFF0284C7)
                        UserRole.DEVELOPER -> Emerald600
                        UserRole.ADMIN -> Color(0xFF7C3AED)
                    }
                } else {
                    Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onRoleSelected(role) }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("role_button_${role.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (role) {
                                UserRole.CLIENT -> Icons.Default.Person
                                UserRole.DEVELOPER -> Icons.Default.AccountBalanceWallet
                                UserRole.ADMIN -> Icons.Default.Gavel
                            },
                            contentDescription = role.label,
                            tint = if (isSelected) Color.White else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = role.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = if (isSelected) Color.White else Slate300
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor, icon) = when (status) {
        "unfunded" -> Quad("Unfunded (Draft)", Slate800, Slate300, Icons.Default.Info)
        "escrow_funded" -> Quad("Escrow Secured", Emerald700, Color.White, Icons.Default.Lock)
        "under_review" -> Quad("In Review", Amber600, Color.White, Icons.Default.HourglassTop)
        "released" -> Quad("Approved & Paid", Color(0xFF0369A1), Color.White, Icons.Default.CheckCircle)
        "disputed" -> Quad("Disputed", Rose500, Color.White, Icons.Default.Warning)
        else -> Quad(status, Slate800, Slate300, Icons.Default.Info)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.testTag("milestone_status_badge_$status")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun MilestonePipelineVisualizer(
    status: String,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Draft" to (status in listOf("unfunded", "escrow_funded", "under_review", "released", "disputed")),
        "In Escrow" to (status in listOf("escrow_funded", "under_review", "released", "disputed")),
        "Submitted" to (status in listOf("under_review", "released")),
        "Paid" to (status == "released")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, (label, isCompleted) ->
                val isCurrent = when (status) {
                    "unfunded" -> index == 0
                    "escrow_funded" -> index == 1
                    "under_review" -> index == 2
                    "released" -> index == 3
                    "disputed" -> index == 1 // paused at escrow
                    else -> false
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    status == "disputed" && index == 1 -> Rose500
                                    isCompleted -> Emerald500
                                    isCurrent -> Amber500
                                    else -> Slate700
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate300
                            )
                        }
                    }

                    if (index < steps.size - 1) {
                        val nextStepCompleted = steps[index + 1].second
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (nextStepCompleted) Emerald500 else Slate800)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEach { (label, _) ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EscrowTrustNotice(
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF064E3B).copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Emerald700.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Emerald600),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Escrow Protection",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "PayPal Delayed Disbursement Escrow",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Emerald100
                )
                Text(
                    text = "Funds are held securely in PayPal Escrow until you approve the deliverables. 10% platform fee and 90% developer payout are disbursed atomically upon release.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = Emerald100.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun FeeSplitCard(
    milestone: Milestone,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .testTag("fee_split_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Milestone Fee Split Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Milestone Escrow:", color = Slate300, fontSize = 13.sp)
                Text("\$${milestone.amount.toInt()}.00 USD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Emerald500))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Developer Payout (90%):", color = Slate300, fontSize = 13.sp)
                }
                Text("\$${milestone.developerAmount.toInt()}.00 USD", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Platform Commission (10%):", color = Slate300, fontSize = 13.sp)
                }
                Text("\$${milestone.commissionAmount.toInt()}.00 USD", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Slate800,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PPCP delayed disbursement authorization holds the complete \$${milestone.amount.toInt()} until Client releases. Split happens atomically via server-side disbursement.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Slate400,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
