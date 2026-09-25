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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InsightEntity
import com.example.ui.components.BentoBadge
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoMicroBars
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoEmeraldLight
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoPurpleContainer
import com.example.ui.theme.BentoRose
import com.example.viewmodel.LifeVaultViewModel

@Composable
fun InsightsScreen(
    viewModel: LifeVaultViewModel,
    modifier: Modifier = Modifier
) {
    val insights by viewModel.insights.collectAsState()
    var selectedPeriod by remember { mutableStateOf("This Month") }

    val activeInsight = insights.firstOrNull { it.period == selectedPeriod }
        ?: insights.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Header
        item {
            Column {
                BentoBadge(text = "Life Intelligence", color = BentoPurple, containerColor = BentoPurpleContainer)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "What Changed?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Meaningful behavioral shifts and AI-detected explanations.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Period Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("This Month", "This Week").forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) BentoIndigo else Color.Transparent)
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Key Metrics Bento 2x2 Grid
        item {
            val spending = activeInsight?.spendingChangePercent ?: 18
            val shopping = activeInsight?.shoppingChangePercent ?: 31
            val study = activeInsight?.studyChangePercent ?: 14
            val goals = activeInsight?.goalsChangePercent ?: 22

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBentoBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Payments,
                        iconColor = BentoAmber,
                        iconContainer = BentoAmberContainer,
                        label = "Spending",
                        value = "${if (spending >= 0) "↑" else "↓"} ${Math.abs(spending)}%",
                        trend = if (spending >= 0) "Higher spending" else "Lower spending",
                        isPositiveTrend = spending <= 0
                    )

                    MetricBentoBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.ShoppingBag,
                        iconColor = BentoIndigoLight,
                        iconContainer = BentoIndigoContainer,
                        label = "Shopping",
                        value = "${if (shopping >= 0) "↑" else "↓"} ${Math.abs(shopping)}%",
                        trend = "Tech & ergonomics",
                        isPositiveTrend = false
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBentoBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.MenuBook,
                        iconColor = BentoEmerald,
                        iconContainer = BentoEmeraldContainer,
                        label = "Study & Focus",
                        value = "↑ ${study}%",
                        trend = "+4.5 hrs / week",
                        isPositiveTrend = true
                    )

                    MetricBentoBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.TrackChanges,
                        iconColor = BentoPurple,
                        iconContainer = BentoPurpleContainer,
                        label = "Goals Met",
                        value = "↑ ${goals}%",
                        trend = "5 completed",
                        isPositiveTrend = true
                    )
                }
            }
        }

        // Deep AI Explanation Bento Card
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = BentoIndigoLight.copy(alpha = 0.4f),
                cornerRadius = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = BentoIndigoLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "YOUR BIGGEST CHANGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoIndigoLight,
                                letterSpacing = 1.sp
                            )
                        }

                        BentoMicroBars()
                    }

                    Text(
                        text = activeInsight?.explanation
                            ?: "Shopping increased significantly this month. Your saved purchases show that most of the increase came from electronics and study gear.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Observation: Increased study gear investment correlated with a 14% uptick in completed focus sessions.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Interconnected Intelligence Card
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                cornerRadius = 22.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INTERCONNECTED INTELLIGENCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "How LifeVault connected your single input:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ConnectedStep("“I bought wireless headphones for $348”", "Original Saved Memory")
                        ConnectedStep("Categorized in Purchases ($348)", "Decision Tracking")
                        ConnectedStep("Contributed to +31% Electronics spike", "Monthly Spending Shift")
                        ConnectedStep("Auto-generated warranty alert (expires in 14 days)", "Proactive Vault Alert")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun MetricBentoBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    iconContainer: Color,
    label: String,
    value: String,
    trend: String,
    isPositiveTrend: Boolean
) {
    BentoCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        cornerRadius = 20.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = trend,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPositiveTrend) BentoEmeraldLight else BentoAmber
            )
        }
    }
}

@Composable
private fun ConnectedStep(text: String, tag: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = BentoIndigoLight,
            modifier = Modifier.size(14.dp)
        )
        Column {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = tag,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
