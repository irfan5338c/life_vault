package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import coil.compose.AsyncImage
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
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoPurpleContainer
import com.example.ui.theme.BentoRose
import com.example.viewmodel.LifeVaultViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LifeVaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userProfilePhotoUri by viewModel.userProfilePhotoUri.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val showSaveSuccess by viewModel.showSaveSuccess.collectAsState()
    val lastSavedSummary by viewModel.lastSavedSummary.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val purchases by viewModel.purchases.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Temp file for camera capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Helper to persist a copy of the selected image into internal app storage
    fun copyUriToInternalStorage(sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val profileFile = File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(profileFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(profileFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Camera launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val localPath = copyUriToInternalStorage(tempCameraUri!!) ?: tempCameraUri.toString()
            viewModel.updateProfilePhoto(localPath)
            showPhotoOptionsSheet = false
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val tempFile = File.createTempFile("camera_avatar_", ".jpg", context.cacheDir).apply {
                    createNewFile()
                    deleteOnExit()
                }
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    tempFile
                )
                tempCameraUri = photoUri
                takePhotoLauncher.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture a profile photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery picker (Standard zero-permission Photo Picker)
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val localPath = copyUriToInternalStorage(uri) ?: uri.toString()
            viewModel.updateProfilePhoto(localPath)
            showPhotoOptionsSheet = false
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val tempFile = File.createTempFile("camera_avatar_", ".jpg", context.cacheDir).apply {
                    createNewFile()
                    deleteOnExit()
                }
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    tempFile
                )
                tempCameraUri = photoUri
                takePhotoLauncher.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not launch camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // User Identity Card
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = BentoIndigoLight.copy(alpha = 0.35f),
                    cornerRadius = 24.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar with Camera & Gallery Edit Badge
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clickable { showPhotoOptionsSheet = true }
                                    .testTag("profile_avatar_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userProfilePhotoUri != null) {
                                    AsyncImage(
                                        model = userProfilePhotoUri,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape)
                                            .border(2.5.dp, BentoIndigoLight, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(BentoIndigo, BentoPurple))
                                            )
                                            .border(2.5.dp, BentoIndigoLight.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName.take(2).uppercase(),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Overlay edit badge
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(BentoIndigo)
                                        .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = userName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BentoIndigoLight.copy(alpha = 0.15f))
                                        .clickable { showPhotoOptionsSheet = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = BentoIndigoLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (userProfilePhotoUri != null) "Change Photo" else "Add Photo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoIndigoLight
                                    )
                                }
                            }
                        }

                        // Memory Strength Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Memory Strength",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "78% (Healthy)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoEmerald
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.78f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(BentoIndigoLight, BentoEmerald)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Gamification Trophies
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    cornerRadius = 22.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = BentoAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "VAULT TROPHIES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoAmber,
                                letterSpacing = 1.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TrophyItem("🏆", "First Memory Unlocked", "Stored first item safely in the vault")
                            TrophyItem("🛒", "Smart Shopper", "Saved over $350 via 'Before You Buy' analysis")
                            TrophyItem("📅", "Organized Month", "Recorded 20+ life entries seamlessly")
                            TrophyItem("🤝", "Community Helper", "Assisted community item recovery")
                        }
                    }
                }
            }

            // Privacy Guarantee Status
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = BentoEmerald.copy(alpha = 0.35f),
                    cornerRadius = 22.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BentoEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "PRIVACY GUARANTEE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoEmerald,
                                    letterSpacing = 1.sp
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.img_lifevault_logo),
                                contentDescription = "LifeVault AI Shield",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        }

                        Text(
                            text = "“You control what LifeVault remembers.”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "All memory entities and purchase decisions are stored locally in your private Room database. No third-party ad tracking or audio harvesting.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Controls & Settings Actions
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    cornerRadius = 22.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "VAULT CONTROLS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        // Theme toggle
                        SettingRow(
                            icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            label = "Display Theme",
                            value = if (isDarkMode) "Dark (Bento Obsidian)" else "Light",
                            onClick = { viewModel.toggleDarkMode() }
                        )

                        // Profile Photo option
                        SettingRow(
                            icon = Icons.Default.CameraAlt,
                            label = "Profile Photo",
                            value = if (userProfilePhotoUri != null) "Change or remove" else "Add from gallery / camera",
                            onClick = { showPhotoOptionsSheet = true }
                        )

                        // Populate Demo Data
                        SettingRow(
                            icon = Icons.Default.Refresh,
                            label = "Seed Demo Data",
                            value = "Reset sample data",
                            onClick = { viewModel.populateDemoData() }
                        )

                        // Clear Demo Data
                        SettingRow(
                            icon = Icons.Default.Delete,
                            label = "Clear Demo Items",
                            value = "Keep only personal items",
                            onClick = { viewModel.clearDemoData() }
                        )

                        // Clear All Data
                        SettingRow(
                            icon = Icons.Default.Delete,
                            label = "Reset All Vault Data",
                            value = "Delete all records",
                            color = BentoRose,
                            onClick = { showClearConfirmDialog = true }
                        )

                        // Sign Out to Login Screen
                        SettingRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Sign Out",
                            value = "Return to Life Vault AI login",
                            color = BentoIndigoLight,
                            onClick = { viewModel.signOut() }
                        )
                    }
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

    if (showPhotoOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoOptionsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Profile Photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Choose a photo from your gallery or capture a new one using your camera.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option: Take photo with camera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            launchCamera()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BentoIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = BentoIndigoLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Take Photo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Use your device camera to take a selfie",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option: Choose from gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            pickPhotoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BentoEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = BentoEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Choose from Gallery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Select an existing picture from your photo library",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option: Remove photo (if currently set)
                if (userProfilePhotoUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BentoRose.copy(alpha = 0.1f))
                            .clickable {
                                viewModel.updateProfilePhoto(null)
                                showPhotoOptionsSheet = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BentoRose.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove photo",
                                tint = BentoRose,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Remove Photo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoRose
                            )
                            Text(
                                text = "Restore your initials avatar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { showPhotoOptionsSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Vault Data?") },
            text = { Text("This will permanently remove all stored memories, decisions, and reports from the local database.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoRose)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TrophyItem(emoji: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (color == BentoRose) BentoRose else BentoIndigoLight,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
