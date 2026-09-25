package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WhereToVote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LostFoundItemEntity
import com.example.ui.components.BentoBadge
import com.example.ui.components.BentoCard
import com.example.ui.components.SaveSuccessToast
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoEmeraldLight
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.BentoRose
import com.example.viewmodel.LifeVaultViewModel

@Composable
fun LostFoundScreen(
    viewModel: LifeVaultViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.lostFoundItems.collectAsState()
    val showSaveSuccess by viewModel.showSaveSuccess.collectAsState()
    val lastSavedSummary by viewModel.lastSavedSummary.collectAsState()

    var activeTab by remember { mutableStateOf("All") }
    var showReportDialog by remember { mutableStateOf(false) }

    val tabs = listOf("All", "Matches", "Lost", "Found")

    val filteredItems = when (activeTab) {
        "Matches" -> items.filter { it.status == "MATCHED" }
        "Lost" -> items.filter { it.type == "LOST" }
        "Found" -> items.filter { it.type == "FOUND" }
        else -> items
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showReportDialog = true },
                containerColor = BentoEmerald,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("report_lost_found_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Report Item")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Header
                item {
                    Column {
                        BentoBadge(
                            text = "Community Intelligence",
                            color = BentoEmerald,
                            containerColor = BentoEmeraldContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lost & Found",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Campus, hostel, and community matching with AI similarity detection.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Filter Tabs
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(tabs) { tab ->
                            val isSelected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) BentoEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) BentoEmerald else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(100.dp)
                                    )
                                    .clickable { activeTab = tab }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tab,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BentoEmerald else MaterialTheme.colorScheme.onBackground
                                    )
                                    if (tab == "Matches") {
                                        val matchCount = items.count { it.status == "MATCHED" }
                                        if (matchCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(BentoEmerald),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$matchCount",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Empty state or Items
                if (filteredItems.isEmpty()) {
                    item {
                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WhereToVote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Nothing lost here.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Hopefully it stays that way. 😄",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredItems, key = { it.id }) { item ->
                        LostFoundCard(
                            item = item,
                            onMarkRecovered = { viewModel.markItemRecovered(item) },
                            onDelete = { viewModel.deleteLostFoundItem(item) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            SaveSuccessToast(
                visible = showSaveSuccess,
                message = lastSavedSummary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }

    if (showReportDialog) {
        ReportLostFoundDialog(
            onDismiss = { showReportDialog = false },
            onConfirm = { type, title, desc, cat, loc, time, contact ->
                viewModel.reportLostOrFound(type, title, desc, cat, loc, time, contact)
                showReportDialog = false
            }
        )
    }
}

@Composable
private fun LostFoundCard(
    item: LostFoundItemEntity,
    onMarkRecovered: () -> Unit,
    onDelete: () -> Unit
) {
    val isLost = item.type == "LOST"
    val isRecovered = item.status == "RECOVERED"
    val isMatched = item.status == "MATCHED"

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = when {
            isRecovered -> BentoEmerald.copy(alpha = 0.4f)
            isMatched -> BentoIndigoLight.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        cornerRadius = 20.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BentoBadge(
                        text = item.type,
                        color = if (isLost) BentoRose else BentoEmerald,
                        containerColor = if (isLost) Color(0x26F43F5E) else BentoEmeraldContainer
                    )

                    if (isMatched) {
                        BentoBadge(
                            text = "Possible Match — ${item.matchConfidence}%",
                            color = BentoIndigoLight,
                            containerColor = BentoIndigoContainer
                        )
                    } else if (isRecovered) {
                        BentoBadge(
                            text = "Recovered ✓",
                            color = BentoEmerald,
                            containerColor = BentoEmeraldContainer
                        )
                    }
                }

                BentoBadge(
                    text = item.category,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PinDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.location,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.reportedTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Secure Contact Info & Recovered action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contact: ${item.contactInfo}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = BentoIndigoLight
                )

                if (!isRecovered) {
                    Button(
                        onClick = onMarkRecovered,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoEmeraldContainer,
                            contentColor = BentoEmerald
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recovered ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportLostFoundDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, title: String, desc: String, cat: String, loc: String, time: String, contact: String) -> Unit
) {
    var type by remember { mutableStateOf("LOST") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bags") }
    var location by remember { mutableStateOf("") }
    var reportedTime by remember { mutableStateOf("Today at 2:00 PM") }
    var contactInfo by remember { mutableStateOf("Hostel Block B / Desk") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report Item",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "LOST" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "LOST") BentoRose else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "LOST") Color.White else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lost")
                    }
                    Button(
                        onClick = { type = "FOUND" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "FOUND") BentoEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "FOUND") Color.White else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Found")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item Title (e.g. Navy Backpack)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Bags, Electronics, Keys)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Library 2nd floor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & identifying details") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Contact Info / Collection Point") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(type, title, description, category, location, reportedTime, contactInfo)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "LOST") BentoRose else BentoEmerald,
                    contentColor = Color.White
                )
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
