package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.PayPalSky
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

enum class DocumentType {
    PRIVACY_POLICY,
    ESCROW_AGREEMENT
}

@Composable
fun LegalDocumentModal(
    documentType: DocumentType,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("legal_document_modal"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (documentType == DocumentType.PRIVACY_POLICY)
                                        Emerald500.copy(alpha = 0.2f)
                                    else PayPalSky.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (documentType == DocumentType.PRIVACY_POLICY) Emerald500 else PayPalSky,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (documentType == DocumentType.PRIVACY_POLICY)
                                    Icons.Default.Shield
                                else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (documentType == DocumentType.PRIVACY_POLICY) Emerald500 else PayPalSky,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (documentType == DocumentType.PRIVACY_POLICY)
                                    "Privacy Policy & Data Security"
                                else "Milestone Escrow Terms & Agreement",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "DevMarket Compliance & PayPal Commerce Platform",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_legal_modal_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (documentType == DocumentType.PRIVACY_POLICY) {
                    // Privacy Policy Content
                    LegalSection(
                        title = "1. Information We Collect",
                        content = "DevMarket collects necessary business information to facilitate milestone contracting and escrow payouts: full name, corporate email, role identification (Client, Developer, Administrator), and connected PayPal Merchant IDs. We do not store raw credit card numbers or banking passwords on our servers."
                    )

                    LegalSection(
                        title = "2. Financial Transactions & Zero Client-Side Math",
                        content = "All calculations regarding escrow holds, 10% platform service commissions, and 90% developer disbursements are processed strictly on isolated server-side endpoints. Client applications never compute financial balances. Financial audit logs are stored in PostgreSQL with Row-Level Security (RLS)."
                    )

                    LegalSection(
                        title = "3. PostgREST & Supabase Data Governance",
                        content = "Analytical telemetry transmitted to our Supabase database is governed by strict JWT authorization. Clients may only query contract records where client_id matches auth.uid(); developers may only query projects where developer_id matches auth.uid(). Platform administrators maintain scoped access for dispute mediation."
                    )

                    LegalSection(
                        title = "4. Data Retention and Deletion",
                        content = "Transactional history, PayPal capture IDs, and dispute logs are retained in accordance with financial regulatory standards (FINCEN & PCI-DSS compliance). Users may request an archive or deletion of non-contractual personal profiles by contacting security@devmarket.internal."
                    )
                } else {
                    // Escrow Agreement Content
                    LegalSection(
                        title = "1. Delayed Disbursement Escrow Architecture",
                        content = "Funds deposited by Clients for project milestones are captured using PayPal Commerce Platform (PPCP) with disbursement_mode: 'DELAYED'. Funds remain securely held in PayPal partner escrow custody until deliverable approval or administrative dispute resolution."
                    )

                    LegalSection(
                        title = "2. Deliverable Review & Automatic Payout",
                        content = "Upon developer submission of milestone deliverables, the milestone enters 'UNDER_REVIEW' status. Once the Client approves the submission, our server triggers the PayPal /v2/payments/captures/{id}/disburse API, automatically distributing 90% to the developer and retaining 10% platform fee."
                    )

                    LegalSection(
                        title = "3. Dispute Mediation & Escrow Arbitration",
                        content = "In the event of non-performance or contract breach, either party may elevate the milestone to active dispute status. DevMarket Platform Administrators act as impartial arbitrators, reviewing submitted code artifacts and communication logs before executing a final release to developer or refund to client."
                    )

                    LegalSection(
                        title = "4. Anti-Circumvention Rule",
                        content = "Clients and Developers agree not to solicit or accept direct payments outside of the DevMarket escrow framework. Doing so forfeits all PayPal protection, code repository escrow insurance, and mediation privileges."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last updated: September 2026 • Certified for PayPal Delayed Disbursement",
                            color = Slate400,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("I Understand & Acknowledge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LegalSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            color = Slate300,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
