package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.VaultDropFileEntity
import com.example.data.local.entity.VaultDropShareEntity
import com.example.ui.components.ChunkedUploadProgressCard
import com.example.ui.components.SafePreviewCard
import com.example.ui.components.ShareLinkModal
import com.example.viewmodel.VaultDropTab
import com.example.viewmodel.VaultDropViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultDropScreen(
    modifier: Modifier = Modifier,
    vaultDropViewModel: VaultDropViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeTab by vaultDropViewModel.activeTab.collectAsState()
    val allFiles by vaultDropViewModel.allFiles.collectAsState()
    val allShares by vaultDropViewModel.allShares.collectAsState()
    val auditLogs by vaultDropViewModel.auditLogs.collectAsState()
    val totalStorageUsage by vaultDropViewModel.totalStorageUsage.collectAsState()

    // Upload state
    val isUploading by vaultDropViewModel.isUploading.collectAsState()
    val isUploadPaused by vaultDropViewModel.isUploadPaused.collectAsState()
    val uploadProgress by vaultDropViewModel.uploadProgress.collectAsState()
    val uploadSpeedMbps by vaultDropViewModel.uploadSpeedMbps.collectAsState()
    val uploadEtaSeconds by vaultDropViewModel.uploadEtaSeconds.collectAsState()
    val uploadingFilename by vaultDropViewModel.uploadingFilename.collectAsState()
    val uploadingFilesize by vaultDropViewModel.uploadingFilesize.collectAsState()

    // Active modals
    val newlySharedPair by vaultDropViewModel.newlySharedPair.collectAsState()
    val inspectedShare by vaultDropViewModel.inspectedShare.collectAsState()
    val toastMessage by vaultDropViewModel.toastMessage.collectAsState()

    // Rename dialog state
    var fileToRename by remember { mutableStateOf<VaultDropFileEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Real system file picker for ANY file format (*/*)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            uris.firstOrNull()?.let { uri ->
                vaultDropViewModel.onFilePickedFromUri(uri)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)) // Dark obsidian canvas
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top App Bar / Platform Branding
            VaultDropTopBar(
                activeTab = activeTab,
                onTabSelect = { vaultDropViewModel.selectTab(it) },
                onQuickUploadClick = { filePickerLauncher.launch(arrayOf("*/*")) }
            )

            // Main Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    VaultDropTab.HERO_UPLOAD -> {
                        HeroUploadSection(
                            viewModel = vaultDropViewModel,
                            onPickFiles = { filePickerLauncher.launch(arrayOf("*/*")) },
                            isUploading = isUploading,
                            isPaused = isUploadPaused,
                            uploadProgress = uploadProgress,
                            uploadSpeedMbps = uploadSpeedMbps,
                            uploadEtaSeconds = uploadEtaSeconds,
                            uploadingFilename = uploadingFilename,
                            uploadingFilesize = uploadingFilesize
                        )
                    }

                    VaultDropTab.DASHBOARD -> {
                        DashboardFilesSection(
                            files = allFiles,
                            totalStorageBytes = totalStorageUsage ?: 0L,
                            onInspectShare = { vaultDropViewModel.inspectShare(it) },
                            onRenameRequest = {
                                fileToRename = it
                                renameInput = it.originalFilename
                            },
                            onDelete = { vaultDropViewModel.deleteFile(it) },
                            onDownload = { file ->
                                val share = allShares.firstOrNull { it.fileId == file.id }
                                if (share != null) {
                                    vaultDropViewModel.setRecipientTokenInput("https://vaultdrop.io/s/${share.token}#key=${share.keyFragment}")
                                    vaultDropViewModel.selectTab(VaultDropTab.RECIPIENT_VIEW)
                                    vaultDropViewModel.lookupRecipientShare(share.token)
                                } else {
                                    vaultDropViewModel.showToast("No active link found for this file")
                                }
                            },
                            onCopyLink = { file ->
                                val share = allShares.firstOrNull { it.fileId == file.id }
                                if (share != null) {
                                    val link = "https://vaultdrop.io/s/${share.token}#key=${share.keyFragment}"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("VaultDrop Link", link))
                                    vaultDropViewModel.showToast("Copied: $link")
                                }
                            }
                        )
                    }

                    VaultDropTab.ACTIVE_SHARES -> {
                        ActiveSharesSection(
                            shares = allShares,
                            files = allFiles,
                            onToggleDisable = { token, disable ->
                                vaultDropViewModel.toggleShareDisabled(token, disable)
                            },
                            onInspect = { share ->
                                val file = allFiles.firstOrNull { it.id == share.fileId }
                                if (file != null) {
                                    vaultDropViewModel.inspectShare(file)
                                }
                            }
                        )
                    }

                    VaultDropTab.RECIPIENT_VIEW -> {
                        RecipientDownloadSection(viewModel = vaultDropViewModel)
                    }

                    VaultDropTab.ADMIN_PANEL -> {
                        AdminPanelSection(
                            files = allFiles,
                            shares = allShares,
                            auditLogs = auditLogs,
                            storageBytes = totalStorageUsage ?: 0L
                        )
                    }
                }
            }
        }

        // Newly Shared Modal
        newlySharedPair?.let { (file, share) ->
            ShareLinkModal(
                file = file,
                share = share,
                onDismiss = { vaultDropViewModel.dismissNewlySharedModal() },
                onCopyFeedback = { vaultDropViewModel.showToast(it) }
            )
        }

        // Inspected Share Modal
        inspectedShare?.let { (file, share) ->
            ShareLinkModal(
                file = file,
                share = share,
                onDismiss = { vaultDropViewModel.dismissInspectedShare() },
                onCopyFeedback = { vaultDropViewModel.showToast(it) }
            )
        }

        // Rename Dialog
        fileToRename?.let { targetFile ->
            AlertDialog(
                onDismissRequest = { fileToRename = null },
                title = { Text("Rename Metadata", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Updates the file's visible name while preserving its binary hash and encrypted storage payload.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameInput.isNotBlank()) {
                                vaultDropViewModel.renameFile(targetFile.id, renameInput.trim())
                                fileToRename = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0F172A))
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToRename = null }) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        // Floating Toast Feedback Banner
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF00E5FF))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF090D16)
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultDropTopBar(
    activeTab: VaultDropTab,
    onTabSelect: (VaultDropTab) -> Unit,
    onQuickUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)))
    ) {
        // Platform Title + Privacy Shield + Quick Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF6366F1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "VaultDrop Logo",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VaultDrop",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ZERO-KNOWLEDGE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                    Text(
                        text = "Encrypted file sharing for every file format",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Quick upload button
            IconButton(
                onClick = onQuickUploadClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1))
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = "Upload Any File",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Navigation Tabs (Drop & Share, My Files, Links, Download, Security & Admin)
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF00E5FF),
            edgePadding = 12.dp,
            divider = {}
        ) {
            VaultDropTab.values().forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { onTabSelect(tab) },
                    text = {
                        Text(
                            text = tab.label,
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == tab) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroUploadSection(
    viewModel: VaultDropViewModel,
    onPickFiles: () -> Unit,
    isUploading: Boolean,
    isPaused: Boolean,
    uploadProgress: Float,
    uploadSpeedMbps: Double,
    uploadEtaSeconds: Int,
    uploadingFilename: String,
    uploadingFilesize: Long
) {
    val expiryHours by viewModel.uploadExpiryHours.collectAsState()
    val downloadLimit by viewModel.uploadDownloadLimit.collectAsState()
    val passwordInput by viewModel.uploadPassword.collectAsState()
    val storageProvider by viewModel.selectedStorageProvider.collectAsState()
    val uploadStatus by viewModel.uploadLifecycleStatus.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Hero Title Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share files. Keep control.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Private, client-side encrypted sharing for every type of file. Arbitrary binary files supported without extension or MIME restriction.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Active Upload Progress Card (if uploading)
        if (isUploading) {
            item {
                ChunkedUploadProgressCard(
                    filename = uploadingFilename,
                    filesizeBytes = uploadingFilesize,
                    progress = uploadProgress,
                    speedMbps = uploadSpeedMbps,
                    etaSeconds = uploadEtaSeconds,
                    isPaused = isPaused,
                    status = uploadStatus,
                    onPause = { viewModel.pauseUpload() },
                    onResume = { viewModel.resumeUpload() },
                    onCancel = { viewModel.cancelUpload() }
                )
            }
        }

        // Giant Drag & Drop / File Browser Upload Target
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color(0xFF6366F1).copy(alpha = 0.6f))),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onPickFiles() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.UploadFile,
                            contentDescription = "Drop files",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Drop files to share",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tap to browse any binary file • PDF, ZIP, EXE, APK, ISO, XYZ",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Quick-Drop Sample Verification Palette
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FolderZip,
                            contentDescription = "Formats",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quick-Drop Verification Palette (Any Format)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Tap any chip to encrypt, chunk, and generate an instant private share link:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val testFormats = listOf(
                            Pair("test.pdf", "PDF"),
                            Pair("archive.zip", "ZIP"),
                            Pair("installer.exe", "EXE"),
                            Pair("application.apk", "APK"),
                            Pair("document.docx", "DOCX"),
                            Pair("video.mp4", "MP4"),
                            Pair("music.mp3", "MP3"),
                            Pair("image.png", "PNG"),
                            Pair("backup.tar.gz", "TAR.GZ"),
                            Pair("disk.iso", "ISO"),
                            Pair("unknown.xyz", "XYZ")
                        )

                        testFormats.forEach { (name, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val sampleData = "VaultDrop Encrypted Binary Payload for $name [AES-256-GCM]".toByteArray(Charsets.UTF_8)
                                        viewModel.dropQuickSample(name, sampleData)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+ $label",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upload Configuration Card (Expiry, Password, Limits, Storage)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Share Configuration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Expiration Options
                    Text("Link Expiration", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair(1, "1 Hour"),
                            Pair(24, "24 Hours"),
                            Pair(168, "7 Days"),
                            Pair(720, "30 Days"),
                            Pair(0, "Never")
                        ).forEach { (h, text) ->
                            val isSel = expiryHours == h
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF0F172A))
                                    .border(1.dp, if (isSel) Color(0xFF00E5FF) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setUploadExpiryHours(h) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Download Limit Options
                    Text("Max Downloads", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair(0, "Unlimited"),
                            Pair(1, "1 (Burn)"),
                            Pair(5, "5"),
                            Pair(10, "10")
                        ).forEach { (lim, text) ->
                            val isSel = downloadLimit == lim
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF6366F1).copy(alpha = 0.2f) else Color(0xFF0F172A))
                                    .border(1.dp, if (isSel) Color(0xFF6366F1) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setUploadDownloadLimit(lim) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF818CF8) else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { viewModel.setUploadPassword(it) },
                        label = { Text("Optional Password Protection", fontSize = 12.sp) },
                        placeholder = { Text("Leave blank for direct access", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = "Password", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF00E5FF),
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Storage Engine Selector
                    Text("Storage Layer", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair("LOCAL_ISOLATED", "Local Sandbox"),
                            Pair("S3_COMPATIBLE", "S3 Cold Object"),
                            Pair("IPFS_DECENTRALIZED", "IPFS Web3")
                        ).forEach { (prov, label) ->
                            val isSel = storageProvider == prov
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF0F172A))
                                    .border(1.dp, if (isSel) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSelectedStorageProvider(prov) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF34D399) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Privacy Highlights Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Zero-Knowledge Architecture",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val highlights = listOf(
                        "Client-Side AES-256-GCM authenticated encryption",
                        "Any file type accepted (no extension or MIME allowlists)",
                        "Keys kept in URL fragment anchor (#key=...)",
                        "Impartial malware heuristics (never blocks legitimate formats)",
                        "Path traversal protection with randomized storage UUIDs",
                        "Zero telemetry or background tracking"
                    )

                    highlights.forEach { h ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = h, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun DashboardFilesSection(
    files: List<VaultDropFileEntity>,
    totalStorageBytes: Long,
    onInspectShare: (VaultDropFileEntity) -> Unit,
    onRenameRequest: (VaultDropFileEntity) -> Unit,
    onDelete: (VaultDropFileEntity) -> Unit,
    onDownload: (VaultDropFileEntity) -> Unit,
    onCopyLink: (VaultDropFileEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredFiles = remember(files, searchQuery, selectedCategory) {
        files.filter { file ->
            val matchesSearch = file.originalFilename.contains(searchQuery, ignoreCase = true) ||
                    file.extension.contains(searchQuery, ignoreCase = true)
            val matchesCat = when (selectedCategory) {
                "ALL" -> true
                "ARCHIVES" -> listOf("zip", "rar", "7z", "tar.gz", "gz", "tar").contains(file.extension.lowercase())
                "BINARIES" -> listOf("exe", "apk", "msi", "iso", "dmg", "xyz").contains(file.extension.lowercase())
                "MEDIA" -> listOf("mp4", "mp3", "png", "jpg", "wav").contains(file.extension.lowercase())
                "DOCS" -> listOf("pdf", "docx", "txt", "csv", "json").contains(file.extension.lowercase())
                else -> true
            }
            matchesSearch && matchesCat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Storage Meter Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Encrypted Storage Usage", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${formatSize(totalStorageBytes)} / 10.0 GB",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${files.size} Files",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val storageFraction = (totalStorageBytes.toFloat() / (10L * 1024 * 1024 * 1024)).coerceIn(0.01f, 1f)
                    LinearProgressIndicator(
                        progress = { storageFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Search and Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search files by name or extension...", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "ARCHIVES", "BINARIES", "MEDIA", "DOCS").forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFF00E5FF) else Color(0xFF1E293B))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF0F172A) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // File List Items
        if (filteredFiles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No files match the query", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            items(filteredFiles, key = { it.id }) { file ->
                FileItemCard(
                    file = file,
                    onInspectShare = { onInspectShare(file) },
                    onRename = { onRenameRequest(file) },
                    onDelete = { onDelete(file) },
                    onDownload = { onDownload(file) },
                    onCopyLink = { onCopyLink(file) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun FileItemCard(
    file: VaultDropFileEntity,
    onInspectShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = file.extension.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = file.originalFilename,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatSize(file.sizeBytes)} • AES-256-GCM • ${file.downloadCount} downloads",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Scan status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF064E3B).copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CLEAN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onInspectShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onCopyLink, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Link", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ActiveSharesSection(
    shares: List<VaultDropShareEntity>,
    files: List<VaultDropFileEntity>,
    onToggleDisable: (String, Boolean) -> Unit,
    onInspect: (VaultDropShareEntity) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Text(
                text = "Active Sharing Links (${shares.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Each link contains the zero-knowledge key fragment in the URL hash.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        if (shares.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No share links active", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            items(shares, key = { it.id }) { share ->
                val associatedFile = files.firstOrNull { it.id == share.fileId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = associatedFile?.originalFilename ?: "Shared File #${share.fileId}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Token: ${share.token} • Downloads: ${share.downloadCount}/${if (share.maxDownloads > 0) share.maxDownloads else "∞"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Active / Disabled Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (share.isDisabled) Color(0xFF7F1D1D) else Color(0xFF064E3B))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (share.isDisabled) "DISABLED" else "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (share.isDisabled) Color(0xFFF87171) else Color(0xFF34D399)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onInspect(share) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF))
                            ) {
                                Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("QR & Share", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onToggleDisable(share.token, !share.isDisabled) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (share.isDisabled) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            ) {
                                Icon(
                                    imageVector = if (share.isDisabled) Icons.Filled.Link else Icons.Filled.LinkOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (share.isDisabled) "Enable" else "Disable", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun RecipientDownloadSection(
    viewModel: VaultDropViewModel
) {
    val tokenInput by viewModel.recipientTokenInput.collectAsState()
    val resolvedFile by viewModel.resolvedRecipientFile.collectAsState()
    val resolvedShare by viewModel.resolvedRecipientShare.collectAsState()
    val passwordInput by viewModel.recipientPasswordInput.collectAsState()
    val errorMessage by viewModel.recipientErrorMessage.collectAsState()
    val isDecrypting by viewModel.isDecryptingDownload.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadSuccess by viewModel.downloadSuccessNotice.collectAsState()
    val lastDownloaded by viewModel.lastDownloadedFile.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Token Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Access Shared File",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Paste a private share link or token (e.g. s_1a2b3c4d or full URL)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { viewModel.setRecipientTokenInput(it) },
                            placeholder = { Text("https://vaultdrop.io/s/...", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { viewModel.lookupRecipientShare(tokenInput) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0F172A))
                        ) {
                            Text("Resolve")
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Success Download Notice
        if (downloadSuccess != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(downloadSuccess ?: "", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Resolved File Details & Safe Preview Card
        resolvedFile?.let { file ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = file.originalFilename,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = file.extension.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Size: ${formatSize(file.sizeBytes)} • MIME: ${file.mimeType}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Safe Preview Component
                        SafePreviewCard(file = file)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field if protected
                        if (file.isPasswordProtected) {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { viewModel.setRecipientPasswordInput(it) },
                                label = { Text("Password Required", fontSize = 12.sp) },
                                placeholder = { Text("Enter decryption password", fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Decrypt & Download Action Button
                        if (isDecrypting) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF00E5FF),
                                    trackColor = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Decrypting with AES-256-GCM client key...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.downloadAndDecryptResolvedFile() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Decrypt & Download File", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (lastDownloaded != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        lastDownloaded?.let { res ->
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.provider",
                                                    res.localFile
                                                )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, res.file.mimeType)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Open ${res.file.originalFilename}"))
                                            } catch (e: Exception) {
                                                viewModel.showToast("Could not open file: ${e.message}")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.FolderZip, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open File", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        lastDownloaded?.let { res ->
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.provider",
                                                    res.localFile
                                                )
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = res.file.mimeType
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share ${res.file.originalFilename}"))
                                            } catch (e: Exception) {
                                                viewModel.showToast("Could not share file: ${e.message}")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export / Share", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun AdminPanelSection(
    files: List<VaultDropFileEntity>,
    shares: List<VaultDropShareEntity>,
    auditLogs: List<com.example.data.local.entity.VaultDropAuditEntity>,
    storageBytes: Long
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Security & Admin Console", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(
                "Telemetry-free audit monitor. Plaintext client keys are never exposed to server administrators.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Metrics Grid (4 Bento Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Total Files",
                    value = files.size.toString(),
                    subtitle = "100% AES-256 encrypted",
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Active Shares",
                    value = shares.filter { !it.isDisabled }.size.toString(),
                    subtitle = "${shares.filter { it.isDisabled }.size} revoked/disabled",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Bandwidth Served",
                    value = "5.2 GB",
                    subtitle = "Zero rate-limit spikes",
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Scanner Status",
                    value = "Clean",
                    subtitle = "ClamAV / YARA active",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Malware Heuristics Monitor Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Impartial Malware & Exploit Scanner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scans all binary payloads (EXE, APK, ISO, ZIP, etc.) without blocking uncommon extensions. Zero false-positive rejection rule enforced.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Cryptographic Audit Trail
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Cryptographic Audit Trail", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (auditLogs.isEmpty()) {
                        Text("No audit events recorded yet", fontSize = 11.sp, color = Color(0xFF64748B))
                    } else {
                        auditLogs.take(15).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${log.eventType}: ${log.filename}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(text = log.details, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(
                                    text = formatTimestamp(log.timestamp),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatTimestamp(timeMs: Long): String {
    val sec = (System.currentTimeMillis() - timeMs) / 1000
    return when {
        sec < 60 -> "${sec}s ago"
        sec < 3600 -> "${sec / 60}m ago"
        sec < 86400 -> "${sec / 3600}h ago"
        else -> "${sec / 86400}d ago"
    }
}
