package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WhereToVote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AIIntentResult
import com.example.ui.components.AIThinkingIndicator
import com.example.ui.components.AssistantFloatingButton
import com.example.ui.components.BentoBadge
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoMicroBars
import com.example.ui.components.SaveSuccessToast
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoAmberLight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: LifeVaultViewModel,
    onNavigateToVault: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenSearch: () -> Unit,
    onNavigateToVaultDrop: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val userProfilePhotoUri by viewModel.userProfilePhotoUri.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val aiInput by viewModel.aiCommandInput.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val activeAiResult by viewModel.activeAiResult.collectAsState()
    val showSaveSuccess by viewModel.showSaveSuccess.collectAsState()
    val lastSavedSummary by viewModel.lastSavedSummary.collectAsState()

    val memories by viewModel.memories.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val lostFoundItems by viewModel.lostFoundItems.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val todayFormatted = remember {
        SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date())
    }

    var showProactivePill by remember { mutableStateOf(true) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // 1. Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lifevault_logo),
                            contentDescription = "LifeVault AI Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, BentoIndigoLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        )
                        Column {
                            Text(
                                text = todayFormatted.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Good morning, $userName",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .testTag("universal_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = BentoAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfilePhotoUri != null) {
                                AsyncImage(
                                    model = userProfilePhotoUri,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, BentoIndigoLight, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(BentoIndigo, BentoPurple))
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName.take(2).uppercase(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. AI Command Center Bento Card
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, BentoIndigoLight.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = BentoIndigoLight,
                                modifier = Modifier.size(22.dp)
                            )

                            TextField(
                                value = aiInput,
                                onValueChange = { viewModel.onAiCommandChange(it) },
                                placeholder = {
                                    Text(
                                        text = "Ask LifeVault anything or tap mic...",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { viewModel.submitAiCommand() }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_command_input")
                            )

                            // Voice Mic Action Button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoIndigoContainer)
                                    .border(1.dp, BentoIndigoLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { showVoiceDialog = true }
                                    .testTag("command_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Voice command",
                                    tint = BentoIndigoLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Send Button (active when input has text)
                            if (aiInput.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BentoIndigoLight)
                                        .clickable { viewModel.submitAiCommand() }
                                        .testTag("command_send_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick AI Suggestions Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suggestions = listOf(
                            "Where is my passport?",
                            "Should I buy headphones?",
                            "I lost my backpack",
                            "What changed this month?",
                            "I kept calculator in blue backpack"
                        )
                        items(suggestions) { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(100.dp))
                                    .clickable {
                                        viewModel.onAiCommandChange(prompt)
                                        viewModel.submitAiCommand(prompt)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // AI Thinking status
                    if (isAiThinking) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AIThinkingIndicator()
                    }

                    // Active AI Result Card
                    activeAiResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ActiveAiResultCard(
                            result = result,
                            onDismiss = { viewModel.dismissAiResult() },
                            onGoToVault = onNavigateToVault,
                            onGoToDecisions = onNavigateToDecisions
                        )
                    }
                }
            }

            // 3. Proactive Intelligence Alert Pill
            if (showProactivePill) {
                item {
                    BentoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BentoAmberContainer,
                        borderColor = BentoAmber.copy(alpha = 0.35f),
                        cornerRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    tint = BentoAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "YOU MIGHT WANT TO KNOW...",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoAmber
                                    )
                                    Text(
                                        text = "Warranty for Sony Headphones expires in 14 days.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showProactivePill = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = BentoAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Bento Grid Main Content
            // 4-AI. Central All-In-One AI Assistant Card
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = BentoIndigoLight.copy(alpha = 0.5f),
                    cornerRadius = 26.dp,
                    onClick = onNavigateToAssistant
                ) {
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
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(BentoIndigoLight, Color(0xFF00E5FF))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "All-In-One AI Assistant",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "All-In-One AI Assistant",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BentoIndigoContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "6 TOOLS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoLight
                                        )
                                    }
                                }
                                Text(
                                    text = "Unified intelligence: memories, products, files & shares",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(BentoIndigo.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Open →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoIndigoLight
                            )
                        }
                    }
                }
            }

            // 4-Zero. VaultDrop: Zero-Knowledge File Sharing Card
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF0F172A),
                    borderColor = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    cornerRadius = 26.dp,
                    onClick = onNavigateToVaultDrop
                ) {
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = "VaultDrop",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VaultDrop File Sharing",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AES-256", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                    }
                                }
                                Text(
                                    text = "Zero-knowledge encrypted drop • Any file type",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFF6366F1))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // 4A. Memory Vault (Large 2-column card)
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    cornerRadius = 26.dp,
                    onClick = onNavigateToVault
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                BentoBadge(
                                    text = "Memory Vault",
                                    color = BentoIndigoLight,
                                    containerColor = BentoIndigoContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${memories.size} Stored",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Highlighting last saved or sample quote
                        val sampleQuote = memories.firstOrNull()?.rawText
                            ?: "I kept my passport inside the top-right drawer of the oak cabinet."
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "“$sampleQuote”",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 4B & 4C. Row with Smart Purchase (Decisions) & Lost & Found Bento Boxes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Smart Purchase / Decisions Bento Box
                    BentoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        cornerRadius = 24.dp,
                        onClick = onNavigateToDecisions
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.height(120.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoAmberContainer)
                                    .border(1.dp, BentoAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = null,
                                    tint = BentoAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Before You Buy",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Live Web Compare",
                                    fontSize = 12.sp,
                                    color = BentoAmber
                                )
                            }
                        }
                    }

                    // Lost & Found Bento Box
                    BentoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        cornerRadius = 24.dp,
                        onClick = onNavigateToDiscover
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.height(120.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoEmeraldContainer)
                                    .border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WhereToVote,
                                    contentDescription = null,
                                    tint = BentoEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Lost & Found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "1 Match nearby (87%)",
                                    fontSize = 12.sp,
                                    color = BentoEmeraldLight
                                )
                            }
                        }
                    }
                }
            }

            // 4D. Weekly / Monthly Insights (Horizontal Stretch with Micro Bars)
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    cornerRadius = 24.dp,
                    onClick = onNavigateToInsights
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEKLY INSIGHTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Focus up ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "14%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoEmeraldLight
                                )
                                Text(
                                    text = " this week",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        BentoMicroBars()
                    }
                }
            }

            // 5. Memory Moments Card
            val activeGoal = goals.firstOrNull { it.status == "ACTIVE" }
            if (activeGoal != null) {
                item {
                    BentoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        borderColor = BentoIndigoLight.copy(alpha = 0.25f),
                        cornerRadius = 24.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = BentoIndigoLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "MEMORY MOMENT • 4 MONTHS AGO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoLight
                                )
                            }

                            Text(
                                text = "“${activeGoal.title}”",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "Still interested?",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.updateGoalStatus(activeGoal, "CONTINUED") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BentoIndigo,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Continue goal →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.updateGoalStatus(activeGoal, "ARCHIVED") },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Archive", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.updateGoalStatus(activeGoal, "REMIND_LATER") },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Remind later", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Floating "Saved to your memory ✓" banner
        SaveSuccessToast(
            visible = showSaveSuccess,
            message = lastSavedSummary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Voice Command Listening Dialog
        if (showVoiceDialog) {
            VoiceInputDialog(
                onDismiss = { showVoiceDialog = false },
                onSpeechResult = { spokenText ->
                    viewModel.onAiCommandChange(spokenText)
                    viewModel.submitAiCommand(spokenText)
                }
            )
        }

        // Floating All-In-One AI Assistant Button
        AssistantFloatingButton(
            onClick = onNavigateToAssistant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun ActiveAiResultCard(
    result: AIIntentResult,
    onDismiss: () -> Unit,
    onGoToVault: () -> Unit,
    onGoToDecisions: () -> Unit
) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = BentoIndigoLight.copy(alpha = 0.5f),
        cornerRadius = 20.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoBadge(text = "LifeVault Intelligence", color = BentoIndigoLight)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            when (result) {
                is AIIntentResult.MemoryAnswer -> {
                    Text(
                        text = result.answer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (result.foundMemory != null) {
                        Text(
                            text = "Category: ${result.foundMemory.category} • Location: ${result.foundMemory.location}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is AIIntentResult.PurchaseAdvice -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val badgeColor = when (result.verdict) {
                            "BUY" -> BentoEmerald
                            "WAIT" -> BentoAmber
                            else -> BentoRose
                        }
                        BentoBadge(text = result.verdict, color = badgeColor)
                        Text(
                            text = result.productName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = result.reasoning,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is AIIntentResult.LostFoundCheck -> {
                    Text(
                        text = result.summary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                is AIIntentResult.LifeInsightSummary -> {
                    Text(
                        text = result.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = result.summary,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.metrics,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoIndigoLight
                    )
                }

                is AIIntentResult.MemoryExtracted -> {
                    Text(
                        text = "Saved to your memory ✓",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmeraldLight
                    )
                    Text(
                        text = "Object: ${result.extracted.objectName} • Location: ${result.extracted.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is AIIntentResult.GeneralAnswer -> {
                    Text(
                        text = result.response,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
