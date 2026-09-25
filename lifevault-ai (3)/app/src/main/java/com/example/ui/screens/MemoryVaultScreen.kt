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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MemoryEntity
import com.example.ui.components.BentoBadge
import com.example.ui.components.BentoCard
import com.example.ui.components.SaveSuccessToast
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.viewmodel.LifeVaultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryVaultScreen(
    viewModel: LifeVaultViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    val showSaveSuccess by viewModel.showSaveSuccess.collectAsState()
    val lastSavedSummary by viewModel.lastSavedSummary.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var naturalInput by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Belongings", "Dates", "Plans", "Ideas", "Notes", "Purchases", "Goals")

    val filteredMemories = memories.filter { memory ->
        val matchesCategory = selectedCategory == "All" || memory.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                memory.rawText.contains(searchQuery, ignoreCase = true) ||
                memory.title.contains(searchQuery, ignoreCase = true) ||
                memory.location.contains(searchQuery, ignoreCase = true) ||
                memory.objectName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BentoIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("add_memory_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memory")
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

                // Title & Tagline
                item {
                    Column {
                        BentoBadge(text = "The Heart of LifeVault", color = BentoIndigoLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Memory Vault",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Save information naturally without tedious forms.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Natural Language Instant Remember Bar
                item {
                    BentoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        borderColor = BentoIndigoLight.copy(alpha = 0.4f),
                        cornerRadius = 22.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    text = "NATURAL MEMORY CAPTURE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoLight,
                                    letterSpacing = 1.sp
                                )
                            }

                            TextField(
                                value = naturalInput,
                                onValueChange = { naturalInput = it },
                                placeholder = {
                                    Text(
                                        text = "e.g. \"I kept my calculator inside my blue college backpack\"",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("natural_memory_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        if (naturalInput.isNotBlank()) {
                                            viewModel.saveNaturalMemory(naturalInput)
                                            naturalInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BentoIndigo,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = naturalInput.isNotBlank(),
                                    modifier = Modifier.testTag("save_memory_button")
                                ) {
                                    Text("Remember", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search memories (e.g. \"passport\", \"drawer\")...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) BentoIndigoContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) BentoIndigoLight else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(100.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BentoIndigoLight else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                // Empty State or Memory Cards
                if (filteredMemories.isEmpty()) {
                    item {
                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Your vault is empty.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Start with something small:\n“I keep my important documents in the study desk.”",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMemories, key = { it.id }) { memory ->
                        MemoryItemCard(
                            memory = memory,
                            onTogglePin = { viewModel.toggleMemoryPinned(memory) },
                            onDelete = { viewModel.deleteMemory(memory) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // Save confirmation banner
            SaveSuccessToast(
                visible = showSaveSuccess,
                message = lastSavedSummary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }

    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, raw, obj, loc, cat ->
                viewModel.addManualMemory(title, raw, obj, loc, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MemoryItemCard(
    memory: MemoryEntity,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.createdAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(memory.createdAt))
    }

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = if (memory.pinned) BentoIndigoLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
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
                        text = memory.category,
                        color = when (memory.category) {
                            "Belongings" -> BentoIndigoLight
                            "Purchases" -> BentoAmber
                            "Plans", "Dates" -> BentoEmerald
                            else -> BentoIndigoLight
                        },
                        containerColor = when (memory.category) {
                            "Belongings" -> BentoIndigoContainer
                            "Purchases" -> BentoAmberContainer
                            "Plans", "Dates" -> BentoEmeraldContainer
                            else -> BentoIndigoContainer
                        }
                    )

                    if (memory.pinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = BentoIndigoLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (memory.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (memory.pinned) BentoIndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = memory.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "“${memory.rawText}”",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
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
                        tint = BentoIndigoLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = memory.location,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, raw: String, obj: String, loc: String, cat: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var objectName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Belongings") }
    var rawNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Memory",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = objectName,
                    onValueChange = { objectName = it },
                    label = { Text("Object / Item Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Where did you store it?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Belongings, Dates, Ideas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rawNotes,
                    onValueChange = { rawNotes = it },
                    label = { Text("Optional Notes or Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (objectName.isNotBlank()) {
                        onConfirm(
                            title.ifBlank { objectName },
                            rawNotes.ifBlank { "Stored $objectName in $location" },
                            objectName,
                            location.ifBlank { "Home" },
                            category.ifBlank { "Belongings" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoIndigo,
                    contentColor = Color.White
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
