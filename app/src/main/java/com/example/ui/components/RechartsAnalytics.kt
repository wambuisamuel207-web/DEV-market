package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Milestone
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
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

data class ChartBarData(
    val label: String,
    val value: Double,
    val color: Color
)

data class DonutSliceData(
    val label: String,
    val value: Double,
    val color: Color,
    val count: Int = 0,
    val statusKey: String = ""
)

/**
 * Modern interactive Bar Chart component modeled after Recharts ResponsiveContainer / BarChart.
 */
@Composable
fun RechartsBarChart(
    data: List<ChartBarData>,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    maxValue: Double? = null
) {
    val maxVal = maxValue ?: (data.maxOfOrNull { it.value }?.takeIf { it > 0.0 } ?: 100.0)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Slate400,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bars display container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    val barHeightFraction = if (maxVal > 0) {
                        ((item.value / maxVal) * animationProgress.value).coerceIn(0.04, 1.0).toFloat()
                    } else 0.04f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        // Value label above bar
                        Text(
                            text = "\$${item.value.toInt()}",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Animated Bar
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(barHeightFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            item.color,
                                            item.color.copy(alpha = 0.65f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = item.color.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Axis Label
                        Text(
                            text = item.label,
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            // Horizontal Grid line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Slate800)
            )
        }
    }
}

/**
 * Modern Donut / Pie chart component modeled after Recharts PieChart.
 */
@Composable
fun RechartsDonutChart(
    slices: List<DonutSliceData>,
    modifier: Modifier = Modifier,
    title: String = "Milestone Status Distribution",
    subtitle: String = "Live PostgREST aggregated pipeline breakdown",
    centerLabel: String = "",
    centerValue: String = "",
    isCurrency: Boolean = false,
    showPercentages: Boolean = true,
    selectedIndex: Int? = null,
    onSliceClick: ((Int) -> Unit)? = null
) {
    val total = slices.sumOf { it.value }.takeIf { it > 0 } ?: 1.0
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = Slate400,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Canvas
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baseStroke = 18.dp.toPx()
                        var startAngle = -90f

                        slices.forEachIndexed { index, slice ->
                            val sweepAngle = ((slice.value / total) * 360f * animationProgress.value).toFloat()
                            val isSelected = selectedIndex == index
                            val strokeWidth = if (isSelected) baseStroke + 4.dp.toPx() else baseStroke

                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = if (selectedIndex == null || isSelected) slice.color else slice.color.copy(alpha = 0.4f),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                    size = Size(size.width - baseStroke, size.height - baseStroke),
                                    topLeft = Offset(baseStroke / 2, baseStroke / 2)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    // Center KPI text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = centerValue,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = centerLabel,
                            color = Slate400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend List
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEachIndexed { index, slice ->
                        val isSelected = selectedIndex == index
                        val percent = if (total > 0) ((slice.value / total) * 100).toInt() else 0

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(enabled = onSliceClick != null) { onSliceClick?.invoke(index) }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 11.dp else 9.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slice.label,
                                    color = if (isSelected) Color.White else Slate300,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (showPercentages) {
                                    Text(
                                        text = "$percent%",
                                        color = Slate400,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                }
                                Text(
                                    text = if (isCurrency) "\$${slice.value.toInt()}" else "${slice.value.toInt()}",
                                    color = if (isSelected) slice.color else Color.White,
                                    fontWeight = FontWeight.Bold,
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

/**
 * Interactive Recharts-styled visual breakdown of project milestones.
 * Provides both an Escrow Capital Distribution mode (showing total escrow distribution by status in dollars)
 * and a Milestone Count mode (showing milestone pipeline distribution).
 */
@Composable
fun RechartsMilestonePieBreakdownCard(
    milestones: List<Milestone>,
    modifier: Modifier = Modifier,
    projectTitle: String? = null,
    defaultExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }
    // 0: By Escrow Amount ($), 1: By Milestone Count (#)
    var selectedMetricMode by remember { mutableIntStateOf(0) }
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }

    // Aggregate data by status
    val escrowFundedMilestones = milestones.filter { it.status == "escrow_funded" }
    val underReviewMilestones = milestones.filter { it.status == "under_review" }
    val releasedMilestones = milestones.filter { it.status == "released" }
    val unfundedMilestones = milestones.filter { it.status == "unfunded" }
    val disputedMilestones = milestones.filter { it.status == "disputed" }

    val escrowFundedAmount = escrowFundedMilestones.sumOf { it.amount }
    val underReviewAmount = underReviewMilestones.sumOf { it.amount }
    val releasedAmount = releasedMilestones.sumOf { it.amount }
    val unfundedAmount = unfundedMilestones.sumOf { it.amount }
    val disputedAmount = disputedMilestones.sumOf { it.amount }

    val totalEscrowVolume = escrowFundedAmount + underReviewAmount + releasedAmount + unfundedAmount + disputedAmount
    val totalMilestonesCount = milestones.size

    val slices = if (selectedMetricMode == 0) {
        listOf(
            DonutSliceData(
                label = "In Escrow",
                value = escrowFundedAmount,
                color = Emerald500,
                count = escrowFundedMilestones.size,
                statusKey = "escrow_funded"
            ),
            DonutSliceData(
                label = "Under Review",
                value = underReviewAmount,
                color = Amber500,
                count = underReviewMilestones.size,
                statusKey = "under_review"
            ),
            DonutSliceData(
                label = "Released",
                value = releasedAmount,
                color = PayPalSky,
                count = releasedMilestones.size,
                statusKey = "released"
            ),
            DonutSliceData(
                label = "Unfunded",
                value = unfundedAmount,
                color = Slate500,
                count = unfundedMilestones.size,
                statusKey = "unfunded"
            ),
            DonutSliceData(
                label = "Disputed",
                value = disputedAmount,
                color = Rose500,
                count = disputedMilestones.size,
                statusKey = "disputed"
            )
        )
    } else {
        listOf(
            DonutSliceData(
                label = "In Escrow",
                value = escrowFundedMilestones.size.toDouble(),
                color = Emerald500,
                count = escrowFundedMilestones.size,
                statusKey = "escrow_funded"
            ),
            DonutSliceData(
                label = "Under Review",
                value = underReviewMilestones.size.toDouble(),
                color = Amber500,
                count = underReviewMilestones.size,
                statusKey = "under_review"
            ),
            DonutSliceData(
                label = "Released",
                value = releasedMilestones.size.toDouble(),
                color = PayPalSky,
                count = releasedMilestones.size,
                statusKey = "released"
            ),
            DonutSliceData(
                label = "Unfunded",
                value = unfundedMilestones.size.toDouble(),
                color = Slate500,
                count = unfundedMilestones.size,
                statusKey = "unfunded"
            ),
            DonutSliceData(
                label = "Disputed",
                value = disputedMilestones.size.toDouble(),
                color = Rose500,
                count = disputedMilestones.size,
                statusKey = "disputed"
            )
        )
    }

    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_milestone_breakdown_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title, Recharts badge, and collapse/expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PayPalSky.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Pie Chart",
                            tint = PayPalSky,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Milestones Breakdown",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Emerald500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Recharts Pie",
                                    color = Emerald500,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (projectTitle != null) "$projectTitle • Total Escrow Distribution" else "Total Escrow Distribution by Status",
                            color = Slate400,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse Chart" else "Expand Chart",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Metric Switcher: Escrow Distribution ($) vs Milestone Count (#)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate950, RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = if (selectedMetricMode == 0) Slate800 else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            border = if (selectedMetricMode == 0) androidx.compose.foundation.BorderStroke(1.dp, Slate700) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMetricMode = 0
                                    selectedSliceIndex = null
                                }
                                .testTag("breakdown_toggle_escrow")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Escrow Volume ($)",
                                    color = if (selectedMetricMode == 0) Color.White else Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedMetricMode == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            color = if (selectedMetricMode == 1) Slate800 else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            border = if (selectedMetricMode == 1) androidx.compose.foundation.BorderStroke(1.dp, Slate700) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMetricMode = 1
                                    selectedSliceIndex = null
                                }
                                .testTag("breakdown_toggle_count")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Milestones Count (#)",
                                    color = if (selectedMetricMode == 1) Color.White else Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedMetricMode == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recharts Donut / Pie Chart representation
                    RechartsDonutChart(
                        slices = slices,
                        title = if (selectedMetricMode == 0) "Escrow Capital Distribution" else "Milestone Pipeline Distribution",
                        subtitle = if (selectedMetricMode == 0) "Total allocation across active escrow and disbursements" else "Status breakdown across all deliverables",
                        centerLabel = if (selectedMetricMode == 0) "Total Escrow" else "Milestones",
                        centerValue = if (selectedMetricMode == 0) "\$${totalEscrowVolume.toInt()}" else "$totalMilestonesCount",
                        isCurrency = selectedMetricMode == 0,
                        showPercentages = true,
                        selectedIndex = selectedSliceIndex,
                        onSliceClick = { index ->
                            selectedSliceIndex = if (selectedSliceIndex == index) null else index
                        }
                    )

                    // Detail callout on selected slice
                    selectedSliceIndex?.let { index ->
                        val slice = slices.getOrNull(index)
                        if (slice != null) {
                            val totalVal = slices.sumOf { it.value }.takeIf { it > 0 } ?: 1.0
                            val pct = ((slice.value / totalVal) * 100).toInt()
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = slice.color.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, slice.color.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(slice.color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${slice.label}: ${if (selectedMetricMode == 0) "\$${slice.value.toInt()}" else "${slice.value.toInt()} milestones"} ($pct% of total)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = when (slice.statusKey) {
                                            "escrow_funded" -> "Protected in escrow"
                                            "under_review" -> "Awaiting client sign-off"
                                            "released" -> "Paid to developer"
                                            "unfunded" -> "Requires deposit"
                                            "disputed" -> "Admin mediation active"
                                            else -> ""
                                        },
                                        color = slice.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
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
