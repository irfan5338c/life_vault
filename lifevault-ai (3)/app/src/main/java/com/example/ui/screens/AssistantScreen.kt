package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.assistant.AssistantActionProposal
import com.example.ai.assistant.AssistantMessage
import com.example.ai.assistant.AssistantToolType
import com.example.ai.assistant.ExplainabilityReport
import com.example.ai.assistant.LifeVaultAssistantOrchestrator
import com.example.ai.assistant.MessageSender
import com.example.ai.assistant.SourceRecord
import com.example.ai.assistant.SuggestedPrompt
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoPurpleContainer
import com.example.viewmodel.LifeVaultViewModel

@Composable
fun AssistantScreen(
    viewModel: LifeVaultViewModel,
    onNavigateToVault: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToVaultDrop: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orchestrator = viewModel.assistantOrchestrator
    val messages by orchestrator.messages.collectAsState()
    val isProcessing by orchestrator.isProcessing.collectAsState()
    val currentStatus by orchestrator.currentStatus.collectAsState()

    val context = LocalContext.current
    var inputQuery by remember { mutableStateOf("") }
    var showVoiceDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isProcessing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("assistant_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(BentoIndigoLight, Color(0xFF00E5FF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Assistant AI",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LifeVault Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BentoIndigoContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "All-In-One",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "7 authorized tools • Privacy-first intelligence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { orchestrator.clearConversation() },
                    modifier = Modifier.testTag("assistant_clear_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- Processing Indicator Banner ---
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BentoIndigoContainer.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = BentoIndigoLight
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = currentStatus ?: "Processing...",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoIndigoLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- Message Conversation List ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                AssistantMessageBubble(
                    message = message,
                    onConfirmAction = { proposal ->
                        viewModel.confirmAssistantAction(proposal)
                    },
                    onSourceClick = { source ->
                        if (!source.url.isNullOrBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        } else {
                            when (source.toolType) {
                                AssistantToolType.MEMORY_SEARCH -> onNavigateToVault()
                                AssistantToolType.PRODUCT_COMPARISON -> onNavigateToDecisions()
                                AssistantToolType.LOST_FOUND_SEARCH -> onNavigateToDiscover()
                                AssistantToolType.DOCUMENT_ASSISTANT,
                                AssistantToolType.PRIVACY_SHARING -> onNavigateToVaultDrop()
                                AssistantToolType.PURCHASE_ASSISTANT,
                                AssistantToolType.MULTI_TOOL_WORKFLOW -> onNavigateToDecisions()
                            }
                        }
                    },
                    onSuggestionClick = { suggestion ->
                        viewModel.askAssistant(suggestion)
                    }
                )
            }
        }

        // --- Quick Suggested Prompts Carousel ---
        if (messages.size <= 2) {
            Text(
                text = "Suggested intelligence queries:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orchestrator.suggestedPrompts) { prompt ->
                    SuggestionPromptChip(
                        suggested = prompt,
                        onClick = {
                            viewModel.askAssistant(prompt.prompt)
                        }
                    )
                }
            }
        }

        // --- Bottom Input Area ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice input button
                IconButton(
                    onClick = { showVoiceDialog = true },
                    modifier = Modifier.testTag("assistant_mic_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Voice Input",
                        tint = BentoIndigoLight
                    )
                }

                // Text field input
                TextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = {
                        Text(
                            text = "Ask anything across your life vault...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("assistant_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputQuery.isNotBlank() && !isProcessing) {
                                val q = inputQuery
                                inputQuery = ""
                                viewModel.askAssistant(q)
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank() && !isProcessing) {
                            val q = inputQuery
                            inputQuery = ""
                            viewModel.askAssistant(q)
                        }
                    },
                    enabled = inputQuery.isNotBlank() && !isProcessing,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputQuery.isNotBlank() && !isProcessing)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                        .testTag("assistant_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputQuery.isNotBlank() && !isProcessing) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onSpeechResult = { text ->
                showVoiceDialog = false
                if (text.isNotBlank()) {
                    viewModel.askAssistant(text)
                }
            }
        )
    }
}

@Composable
private fun SuggestionPromptChip(
    suggested: SuggestedPrompt,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.testTag("suggested_chip_${suggested.toolType.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (suggested.toolType) {
                    AssistantToolType.MEMORY_SEARCH -> Icons.Default.Inventory2
                    AssistantToolType.PRODUCT_COMPARISON -> Icons.Default.ShoppingBag
                    AssistantToolType.LOST_FOUND_SEARCH -> Icons.Default.LocationOn
                    AssistantToolType.DOCUMENT_ASSISTANT -> Icons.Default.Description
                    AssistantToolType.PURCHASE_ASSISTANT -> Icons.AutoMirrored.Filled.ReceiptLong
                    AssistantToolType.PRIVACY_SHARING -> Icons.Default.Shield
                    AssistantToolType.MULTI_TOOL_WORKFLOW -> Icons.Default.AutoAwesome
                },
                contentDescription = null,
                tint = BentoIndigoLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = suggested.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = suggested.prompt,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssistantMessageBubble(
    message: AssistantMessage,
    onConfirmAction: (AssistantActionProposal) -> Unit,
    onSourceClick: (SourceRecord) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Tool Badge for Assistant
        if (!isUser && message.toolType != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getToolContainerColor(message.toolType)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getToolIcon(message.toolType),
                            contentDescription = null,
                            tint = getToolAccentColor(message.toolType),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = message.toolType.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = getToolAccentColor(message.toolType)
                        )
                    }
                }
            }
        }

        // Bubble Surface
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else if (message.isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.98f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                // Action Proposal Card (e.g. Save Memory / Revoke Share)
                if (message.actionProposal != null) {
                    val proposal = message.actionProposal
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = proposal.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = proposal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onConfirmAction(proposal) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("proposal_confirm_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirm",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Sources / Citations
                if (message.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verified Sources & References:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.sources.forEach { source ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { onSourceClick(source) }
                                    .testTag("source_${source.title}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = source.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = BentoIndigoContainer
                                    ) {
                                        Text(
                                            text = source.badge,
                                            fontSize = 9.sp,
                                            color = BentoIndigoLight,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    if (!source.url.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open Web Link",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Explainability Card (Findings, Sources, Missing Info, Inferences, Next Steps)
                if (!isUser && message.explainability != null) {
                    val report = message.explainability
                    var isExpanded by remember { mutableStateOf(false) }

                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = BentoIndigoLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Explainable Intelligence & Breakdown",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    // 1. Information Found
                                    if (report.informationFound.isNotEmpty()) {
                                        Text(
                                            text = "Information Found:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoEmerald
                                        )
                                        report.informationFound.forEach { item ->
                                            Text(
                                                text = "✓ $item",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    // 2. Sources Used
                                    if (report.sourcesUsed.isNotEmpty()) {
                                        Text(
                                            text = "Sources Used:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoIndigoLight
                                        )
                                        report.sourcesUsed.forEach { src ->
                                            Text(
                                                text = "• $src",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    // 3. Missing Information
                                    if (report.missingInformation.isNotEmpty()) {
                                        Text(
                                            text = "Information Missing or Unavailable:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoAmber
                                        )
                                        report.missingInformation.forEach { item ->
                                            Text(
                                                text = "⚠ $item",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    // 4. Inferences
                                    if (report.inferences.isNotEmpty()) {
                                        Text(
                                            text = "AI Inferences:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoPurple
                                        )
                                        report.inferences.forEach { inf ->
                                            Text(
                                                text = "🧠 $inf",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
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

        // Smart Suggestions Below Message Bubble
        if (!isUser && message.smartSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp)
            ) {
                message.smartSuggestions.forEach { suggestion ->
                    Surface(
                        onClick = { onSuggestionClick(suggestion) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("suggestion_${suggestion.take(15)}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = BentoIndigoLight,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getToolIcon(type: AssistantToolType) = when (type) {
    AssistantToolType.MEMORY_SEARCH -> Icons.Default.Inventory2
    AssistantToolType.PRODUCT_COMPARISON -> Icons.Default.ShoppingBag
    AssistantToolType.LOST_FOUND_SEARCH -> Icons.Default.LocationOn
    AssistantToolType.DOCUMENT_ASSISTANT -> Icons.Default.Description
    AssistantToolType.PURCHASE_ASSISTANT -> Icons.AutoMirrored.Filled.ReceiptLong
    AssistantToolType.PRIVACY_SHARING -> Icons.Default.Shield
    AssistantToolType.MULTI_TOOL_WORKFLOW -> Icons.Default.AutoAwesome
}

private fun getToolContainerColor(type: AssistantToolType) = when (type) {
    AssistantToolType.MEMORY_SEARCH -> BentoIndigoContainer
    AssistantToolType.PRODUCT_COMPARISON -> BentoAmberContainer
    AssistantToolType.LOST_FOUND_SEARCH -> BentoPurpleContainer
    AssistantToolType.DOCUMENT_ASSISTANT -> Color(0xFF1E293B)
    AssistantToolType.PURCHASE_ASSISTANT -> BentoEmeraldContainer
    AssistantToolType.PRIVACY_SHARING -> Color(0xFF0F3460)
    AssistantToolType.MULTI_TOOL_WORKFLOW -> Color(0xFF2E1065)
}

private fun getToolAccentColor(type: AssistantToolType) = when (type) {
    AssistantToolType.MEMORY_SEARCH -> BentoIndigoLight
    AssistantToolType.PRODUCT_COMPARISON -> BentoAmber
    AssistantToolType.LOST_FOUND_SEARCH -> BentoPurple
    AssistantToolType.DOCUMENT_ASSISTANT -> Color(0xFF38BDF8)
    AssistantToolType.PURCHASE_ASSISTANT -> BentoEmerald
    AssistantToolType.PRIVACY_SHARING -> Color(0xFF00E5FF)
    AssistantToolType.MULTI_TOOL_WORKFLOW -> Color(0xFFC084FC)
}
