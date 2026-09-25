package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ai.ComparisonState
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.PlatformPriceInfo
import com.example.data.model.ProductComparisonData
import com.example.data.model.QualitySignals
import com.example.data.model.ReviewSummary
import com.example.data.model.SourceCitation
import com.example.data.model.VisualIdentificationResult
import com.example.ui.components.BentoBadge
import com.example.ui.components.BentoCard
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldContainer
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoIndigoContainer
import com.example.ui.theme.BentoIndigoLight
import com.example.ui.theme.BentoRose
import com.example.viewmodel.DecideSortOption
import com.example.viewmodel.LifeVaultViewModel
import com.example.viewmodel.VisualSearchState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PurchaseAnalyzerScreen(
    viewModel: LifeVaultViewModel,
    modifier: Modifier = Modifier
) {
    val purchases by viewModel.purchases.collectAsState()
    val comparisonState by viewModel.comparisonState.collectAsState()
    val searchQuery by viewModel.comparisonSearchQuery.collectAsState()
    val selectedBudget by viewModel.selectedBudget.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val visualSearchState by viewModel.visualSearchState.collectAsState()
    val decideSortOption by viewModel.decideSortOption.collectAsState()

    var inputQuery by remember(searchQuery) { mutableStateOf(searchQuery) }
    var showCustomBudgetDialog by remember { mutableStateOf(false) }
    var customBudgetInput by remember { mutableStateOf("") }
    var showVisualActionMenu by remember { mutableStateOf(false) }
    var capturedPhotoPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPhotoConfirmDialog by remember { mutableStateOf(false) }
    var currentCameraTempUri by remember { mutableStateOf<Uri?>(null) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraTempUri != null) {
            val bitmap = uriToBitmap(context, currentCameraTempUri!!)
            if (bitmap != null) {
                capturedPhotoPreviewBitmap = bitmap
                showPhotoConfirmDialog = true
            } else {
                Toast.makeText(context, "Could not load captured photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            currentCameraTempUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to capture product photos. You can also upload a photo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Photo picker launcher (Zero storage permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                capturedPhotoPreviewBitmap = bitmap
                showPhotoConfirmDialog = true
            } else {
                Toast.makeText(context, "Could not read selected photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val quickSearches = listOf(
        "iPhone 16",
        "HP Laptop",
        "Nike Shoes",
        "Samsung TV",
        "Wireless Earbuds"
    )

    val budgetOptions = listOf(
        "₹10,000",
        "₹20,000",
        "₹50,000",
        "₹1,00,000",
        "Custom"
    )

    val priorityOptions = listOf(
        "Lowest Price",
        "Best Rating",
        "Best Value",
        "Relevance",
        "Balanced"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // 1. Header — Decide Feature 2.0
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BentoBadge(
                        text = "AI Shopping & Decision Assistant",
                        color = BentoAmber,
                        containerColor = BentoAmberContainer
                    )

                    // Small indicator of live web grounding
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(BentoEmerald)
                        )
                        Text(
                            text = "Live India Index",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Decide",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Compare smarter. Decide better.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Modern Search Bar with Camera (+) Button
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = BentoAmber.copy(alpha = 0.45f),
                cornerRadius = 22.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "PRODUCT RESEARCH & VISUAL SEARCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoAmber,
                        letterSpacing = 1.sp
                    )

                    // Search input with trailing Plus (+) button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = {
                                inputQuery = it
                                viewModel.setComparisonSearchQuery(it)
                            },
                            placeholder = {
                                Text(
                                    "Search a product to compare...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = BentoAmber
                                )
                            },
                            trailingIcon = {
                                if (inputQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            inputQuery = ""
                                            viewModel.setComparisonSearchQuery("")
                                        },
                                        modifier = Modifier.testTag("clear_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear input",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                if (inputQuery.isNotBlank()) {
                                    viewModel.compareProduct(
                                        productName = inputQuery.trim(),
                                        budget = selectedBudget,
                                        priority = selectedPriority
                                    )
                                }
                            }),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoAmber,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_search_input")
                        )

                        // Clean PLUS (+) BUTTON for Camera & Photo Search (Decide only!)
                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    showVisualActionMenu = true
                                }
                                .testTag("decide_plus_button"),
                            shape = RoundedCornerShape(16.dp),
                            color = BentoIndigoContainer,
                            border = BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.6f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Visual product search options",
                                    tint = BentoIndigoLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // Quick suggestion chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Popular comparisons:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickSearches) { suggestion ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            inputQuery = suggestion
                                            viewModel.setComparisonSearchQuery(suggestion)
                                            focusManager.clearFocus()
                                            viewModel.compareProduct(
                                                productName = suggestion,
                                                budget = selectedBudget,
                                                priority = selectedPriority
                                            )
                                        }
                                        .testTag("quick_search_${suggestion.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Budget & Priority Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Budget Target Filter
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Budget: ${selectedBudget ?: "Flexible"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(budgetOptions) { opt ->
                                    val isSelected = (selectedBudget == opt) || (opt == "Custom" && selectedBudget != null && !budgetOptions.dropLast(1).contains(selectedBudget))
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (opt == "Custom") {
                                                    showCustomBudgetDialog = true
                                                } else {
                                                    val newB = if (selectedBudget == opt) null else opt
                                                    viewModel.setSelectedBudget(newB)
                                                }
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BentoAmberContainer else MaterialTheme.colorScheme.background,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) BentoAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(
                                            text = opt,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) BentoAmber else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Priority Filter
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Focus: $selectedPriority",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(priorityOptions) { priority ->
                                    val isSelected = (selectedPriority == priority)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setSelectedPriority(priority) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BentoIndigoContainer else MaterialTheme.colorScheme.background,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) BentoIndigoLight else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(
                                            text = priority,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) BentoIndigoLight else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Custom budget dialog
                    if (showCustomBudgetDialog) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customBudgetInput,
                                onValueChange = { customBudgetInput = it },
                                placeholder = { Text("e.g. ₹35,000") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (customBudgetInput.isNotBlank()) {
                                        val formatted = if (customBudgetInput.startsWith("₹")) customBudgetInput else "₹$customBudgetInput"
                                        viewModel.setSelectedBudget(formatted)
                                    }
                                    showCustomBudgetDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoAmber)
                            ) {
                                Text("Set", color = Color(0xFF1E1B4B))
                            }
                        }
                    }

                    // Compare Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (inputQuery.isNotBlank()) {
                                viewModel.compareProduct(
                                    productName = inputQuery.trim(),
                                    budget = selectedBudget,
                                    priority = selectedPriority
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoAmber,
                            contentColor = Color(0xFF1E1B4B)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = inputQuery.isNotBlank() && comparisonState !is ComparisonState.Searching,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("compare_product_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compare Across Platforms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 3. Visual Search AI Identification State / Card
        when (val vState = visualSearchState) {
            is VisualSearchState.Analyzing -> {
                item {
                    VisualAnalyzingCard()
                }
            }

            is VisualSearchState.Identified -> {
                item {
                    VisualIdentifiedCard(
                        result = vState.result,
                        previewBitmap = vState.previewBitmap,
                        onConfirmSearch = { confirmedQuery ->
                            inputQuery = confirmedQuery
                            viewModel.confirmVisualSearchAndCompare(confirmedQuery)
                        },
                        onDismiss = { viewModel.clearVisualSearch() }
                    )
                }
            }

            is VisualSearchState.Error -> {
                item {
                    StatusNoticeCard(
                        title = "Visual Search Notice",
                        message = vState.message,
                        isError = false,
                        onDismiss = { viewModel.clearVisualSearch() }
                    )
                }
            }

            is VisualSearchState.Idle -> { /* Idle */ }
        }

        // 4. Comparison Results or Feedback Views
        when (val state = comparisonState) {
            is ComparisonState.Idle -> {
                item {
                    SearchEmptyGuideCard()
                }
            }

            is ComparisonState.Searching -> {
                item {
                    LiveSearchingIndicatorCard(stepMessage = state.stepMessage)
                }
            }

            is ComparisonState.NoResult -> {
                item {
                    StatusNoticeCard(
                        title = "No Verified Listings Found",
                        message = state.message,
                        isError = false,
                        onDismiss = { viewModel.resetComparison() }
                    )
                }
            }

            is ComparisonState.Error -> {
                item {
                    StatusNoticeCard(
                        title = if (state.message.contains("429") || state.message.contains("quota", ignoreCase = true)) "Rate Limit / Quota" else "Research Temporarily Unavailable",
                        message = state.message,
                        isError = true,
                        onDismiss = { viewModel.resetComparison() },
                        onRetry = {
                            if (inputQuery.isNotBlank()) {
                                viewModel.compareProduct(
                                    productName = inputQuery.trim(),
                                    budget = selectedBudget,
                                    priority = selectedPriority
                                )
                            }
                        }
                    )
                }
            }

            is ComparisonState.Partial -> {
                item {
                    WarningBannerCard(message = state.warning)
                }
                item {
                    ComparisonResultView(
                        data = state.data,
                        selectedSort = decideSortOption,
                        onSortChange = { viewModel.setDecideSortOption(it) },
                        onOpenUrl = { url -> openBrowserUrl(context, url) },
                        onSuggestionSelected = { sug ->
                            inputQuery = sug
                            viewModel.setComparisonSearchQuery(sug)
                            viewModel.compareProduct(productName = sug)
                        }
                    )
                }
            }

            is ComparisonState.Success -> {
                item {
                    SuccessBannerCard()
                }
                item {
                    ComparisonResultView(
                        data = state.data,
                        selectedSort = decideSortOption,
                        onSortChange = { viewModel.setDecideSortOption(it) },
                        onOpenUrl = { url -> openBrowserUrl(context, url) },
                        onSuggestionSelected = { sug ->
                            inputQuery = sug
                            viewModel.setComparisonSearchQuery(sug)
                            viewModel.compareProduct(productName = sug)
                        }
                    )
                }
            }
        }

        // 5. Saved Purchase Decisions History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Purchase Decisions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${purchases.size} decisions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (purchases.isEmpty()) {
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    cornerRadius = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No saved decisions yet",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Compare any product above to record a verified recommendation.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(purchases) { purchase ->
                PurchaseHistoryCard(
                    purchase = purchase,
                    onDelete = { viewModel.deletePurchase(purchase) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }

    // Camera (+) Button Bottom Sheet
    if (showVisualActionMenu) {
        ModalBottomSheet(
            onDismissRequest = { showVisualActionMenu = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Visual Product Search",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Google Lens-style camera detection for Decide",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showVisualActionMenu = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 1. Take a Photo
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showVisualActionMenu = false
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCameraPermission) {
                                val uri = createTempImageUri(context)
                                currentCameraTempUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .testTag("action_take_photo"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = BentoIndigoContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Take Photo",
                                    tint = BentoIndigoLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Take a Photo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Capture physical product with camera to identify & compare",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Upload Photo
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showVisualActionMenu = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("action_upload_photo"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, BentoAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = BentoAmberContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Upload Photo",
                                    tint = BentoAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Upload Photo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Select an existing photo from your gallery",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Photo Preview & Confirmation Dialog
    if (showPhotoConfirmDialog && capturedPhotoPreviewBitmap != null) {
        Dialog(onDismissRequest = { showPhotoConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Confirm Product Photo",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Photo preview
                    androidx.compose.foundation.Image(
                        bitmap = capturedPhotoPreviewBitmap!!.asImageBitmap(),
                        contentDescription = "Captured product preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "LifeVault Vision AI will identify the model and search matching prices across Indian platforms.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showPhotoConfirmDialog = false
                                capturedPhotoPreviewBitmap = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retake")
                        }

                        Button(
                            onClick = {
                                val bmp = capturedPhotoPreviewBitmap!!
                                showPhotoConfirmDialog = false
                                viewModel.identifyProductFromBitmap(bmp)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_visual_search_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoAmber)
                        ) {
                            Text("Identify AI", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual Search Analyzing Card
 */
@Composable
private fun VisualAnalyzingCard() {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = BentoIndigoLight,
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                color = BentoIndigoLight,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = "IDENTIFYING PRODUCT FROM IMAGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BentoIndigoLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "Analyzing visual features, branding, and model details via Gemini Vision AI…",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Visual Identification Card with Edit/Confirm option and Confidence Indicator
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisualIdentifiedCard(
    result: VisualIdentificationResult,
    previewBitmap: Bitmap,
    onConfirmSearch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editableName by remember(result.suggestedQuery, result.productName) {
        mutableStateOf(result.suggestedQuery.ifBlank { result.productName })
    }

    val confidenceColor = when (result.confidence.uppercase()) {
        "HIGH" -> BentoEmerald
        "MEDIUM" -> BentoAmber
        else -> BentoRose
    }

    val confidenceContainer = when (result.confidence.uppercase()) {
        "HIGH" -> BentoEmeraldContainer
        "MEDIUM" -> BentoAmberContainer
        else -> BentoRose.copy(alpha = 0.2f)
    }

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = confidenceColor.copy(alpha = 0.6f),
        cornerRadius = 20.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = confidenceColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "VISUAL IDENTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor,
                        letterSpacing = 1.sp
                    )
                }

                // Confidence badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = confidenceContainer
                ) {
                    Text(
                        text = "${result.confidence} CONFIDENCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Thumbnail + Brand/Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Identified item thumbnail",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (result.brand.isNotBlank()) {
                        Text(
                            text = "Brand: ${result.brand}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoIndigoLight
                        )
                    }
                    if (result.category.isNotBlank()) {
                        Text(
                            text = "Category: ${result.category}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (result.confidenceExplanation.isNotBlank()) {
                        Text(
                            text = result.confidenceExplanation,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Visible features
            if (result.keyFeatures.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    result.keyFeatures.take(3).forEach { feature ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "• $feature",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Editable product query field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Confirm or edit identified product name:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = editableName,
                    onValueChange = { editableName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoAmber,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_edit_field")
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (editableName.isNotBlank()) {
                            onConfirmSearch(editableName.trim())
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("confirm_search_from_photo_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF1E1B4B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Search & Compare",
                        color = Color(0xFF1E1B4B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Renders verified product comparison output for Decide 2.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComparisonResultView(
    data: ProductComparisonData,
    selectedSort: DecideSortOption,
    onSortChange: (DecideSortOption) -> Unit,
    onOpenUrl: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 1. PRODUCT OVERVIEW
        BentoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            borderColor = BentoEmerald.copy(alpha = 0.5f),
            cornerRadius = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRODUCT OVERVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmerald,
                        letterSpacing = 1.sp
                    )
                    if (data.isWebVerified) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BentoEmeraldContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = BentoEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Web verified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoEmerald
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (data.product.image.isNotBlank()) {
                        AsyncImage(
                            model = data.product.image,
                            contentDescription = data.product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = data.product.name.ifBlank { "Identified Product" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (data.product.brand.isNotBlank()) {
                                Text(
                                    text = "Brand: ${data.product.brand}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (data.product.model.isNotBlank()) {
                                Text(
                                    text = "Model: ${data.product.model}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (data.product.variant.isNotBlank()) {
                            Text(
                                text = "Variant: ${data.product.variant}",
                                fontSize = 12.sp,
                                color = BentoAmber
                            )
                        }
                    }
                }

                if (data.product.description.isNotBlank()) {
                    Text(
                        text = data.product.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. SORTING CONTROL & MULTI-PLATFORM PRODUCT CARDS (Sorted Ascending By Default)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MULTI-PLATFORM PRICES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoAmber,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Prices listed lowest to highest by default",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (data.comparison.lowestPrice.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BentoEmeraldContainer
                    ) {
                        Text(
                            text = "Best: ${data.comparison.lowestPrice}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Sort Selector Bar (Lowest Price, Highest Rating, Relevance, Best Value)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DecideSortOption.values()) { opt ->
                    val isSelected = selectedSort == opt
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSortChange(opt) }
                            .testTag("sort_option_${opt.name.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) BentoAmberContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) BentoAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = opt.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BentoAmber else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Apply dynamic sorting
            val sortedPlatforms = remember(data.platforms, selectedSort) {
                when (selectedSort) {
                    DecideSortOption.LOWEST_PRICE -> {
                        data.platforms.sortedWith(
                            compareBy(
                                { it.price == null || it.price <= 0.0 },
                                { it.price ?: Double.MAX_VALUE }
                            )
                        )
                    }
                    DecideSortOption.HIGHEST_RATING -> {
                        data.platforms.sortedWith(
                            compareByDescending<PlatformPriceInfo> { it.rating ?: 0.0 }
                                .thenBy { it.price ?: Double.MAX_VALUE }
                        )
                    }
                    DecideSortOption.RELEVANCE -> {
                        data.platforms.sortedBy { p ->
                            when (p.matchType) {
                                "Exact Match" -> 0
                                "Variant Difference" -> 1
                                else -> 2
                            }
                        }
                    }
                    DecideSortOption.BEST_VALUE -> {
                        data.platforms.sortedWith(
                            compareBy(
                                { it.price == null || it.price <= 0.0 },
                                { (it.price ?: Double.MAX_VALUE) / (it.rating?.coerceAtLeast(1.0) ?: 1.0) }
                            )
                        )
                    }
                }
            }

            val lowestPrice = sortedPlatforms.mapNotNull { it.price }.minOrNull()

            // Render each Platform Product Card
            sortedPlatforms.forEach { platform ->
                DecideProductCard(
                    platform = platform,
                    isCheapest = lowestPrice != null && platform.price == lowestPrice && lowestPrice > 0,
                    productName = data.product.name,
                    brandModel = "${data.product.brand} ${data.product.model}".trim(),
                    productImage = data.product.image,
                    onShopNow = { onOpenUrl(platform.url) }
                )
            }
        }

        // 3. AI SUGGESTIONS & SMART DECISION EXPLANATION (Separate Card)
        BentoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            borderColor = BentoEmerald.copy(alpha = 0.6f),
            cornerRadius = 22.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI DECISION EXPLANATION & SUGGESTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmerald,
                        letterSpacing = 1.sp
                    )
                    BentoBadge(
                        text = "Smart Recommendation",
                        color = BentoEmerald,
                        containerColor = BentoEmeraldContainer
                    )
                }

                // Recommendation Summary
                Text(
                    text = data.aiRecommendation.recommendation.ifBlank { "Recommendation based on verified live listings" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoAmber
                )

                if (data.aiRecommendation.reason.isNotBlank()) {
                    Text(
                        text = data.aiRecommendation.reason,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Best Platform Pick
                if (data.aiRecommendation.bestPlatformPick.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoEmeraldContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = BentoEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Top Platform Pick:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoEmerald
                                )
                                Text(
                                    text = data.aiRecommendation.bestPlatformPick,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Value Analysis & Price Trends
                if (data.aiRecommendation.valueAnalysis.isNotBlank() || data.aiRecommendation.priceTrendAdvice.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (data.aiRecommendation.valueAnalysis.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Value Verdict:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoAmber
                                    )
                                    Text(
                                        text = data.aiRecommendation.valueAnalysis,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (data.aiRecommendation.priceTrendAdvice.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                        contentDescription = null,
                                        tint = BentoIndigoLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = data.aiRecommendation.priceTrendAdvice,
                                        fontSize = 11.sp,
                                        color = BentoIndigoLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Pros and Cons
                if (data.aiRecommendation.pros.isNotEmpty() || data.aiRecommendation.cons.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pros
                        if (data.aiRecommendation.pros.isNotEmpty()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Advantages",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoEmerald
                                )
                                data.aiRecommendation.pros.take(3).forEach { pro ->
                                    Text("✓ $pro", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Cons
                        if (data.aiRecommendation.cons.isNotEmpty()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Considerations",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoRose
                                )
                                data.aiRecommendation.cons.take(3).forEach { con ->
                                    Text("✕ $con", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Suggested Next Steps
                val nextSteps = if (data.aiRecommendation.suggestedNextSteps.isNotEmpty()) {
                    data.aiRecommendation.suggestedNextSteps
                } else {
                    listOf(
                        "Would you like to compare another model?",
                        "Would you like to set a target price alert?",
                        "Would you like to review similar alternatives?"
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Suggested Next Steps:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        nextSteps.forEach { step ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSuggestionSelected(step) }
                                    .testTag("suggested_step_chip"),
                                shape = RoundedCornerShape(8.dp),
                                color = BentoIndigoContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, BentoIndigoLight.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = BentoIndigoLight,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = step,
                                        fontSize = 11.sp,
                                        color = BentoIndigoLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. RATING & REVIEW INSIGHTS
        BentoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            borderColor = BentoIndigoLight.copy(alpha = 0.45f),
            cornerRadius = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RATING & REVIEW INSIGHTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoIndigoLight,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Public customer consensus",
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (data.reviewSummary.overallRatingScore != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Overall Rating:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = BentoAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${data.reviewSummary.overallRatingScore} / 5.0",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoAmber
                            )
                        }
                    }
                }

                if (data.reviewSummary.commonPraise.isNotEmpty() || data.reviewSummary.positive.isNotEmpty()) {
                    val praiseList = (data.reviewSummary.commonPraise + data.reviewSummary.positive).distinct().take(3)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = null, tint = BentoEmerald, modifier = Modifier.size(13.dp))
                            Text("User Praise", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoEmerald)
                        }
                        praiseList.forEach { p ->
                            Text("• $p", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }

                if (data.reviewSummary.commonComplaints.isNotEmpty() || data.reviewSummary.negative.isNotEmpty()) {
                    val complaintList = (data.reviewSummary.commonComplaints + data.reviewSummary.negative).distinct().take(3)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Outlined.ThumbDown, contentDescription = null, tint = BentoRose, modifier = Modifier.size(13.dp))
                            Text("Common Concerns", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoRose)
                        }
                        complaintList.forEach { c ->
                            Text("• $c", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        }

        // 5. QUALITY SIGNALS
        BentoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            borderColor = BentoAmber.copy(alpha = 0.45f),
            cornerRadius = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SPECIFICATIONS & QUALITY SIGNALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoAmber,
                    letterSpacing = 1.sp
                )

                QualitySignalRow("Build Quality", data.qualitySignals.buildQuality, data.qualitySignals.buildQualityRating)
                QualitySignalRow("Performance", data.qualitySignals.performance, data.qualitySignals.performanceRating)
                QualitySignalRow("Hardware Features", data.qualitySignals.features, data.qualitySignals.featuresRating)
                QualitySignalRow("Reliability", data.qualitySignals.reliability, data.qualitySignals.reliabilityRating)
                QualitySignalRow("Warranty & Support", "${data.qualitySignals.warranty} • ${data.qualitySignals.afterSalesSupport}".trim().trim('•').trim(), data.qualitySignals.warrantyRating)
                QualitySignalRow("Value for Money", data.qualitySignals.valueForMoney, data.qualitySignals.valueForMoneyRating)
            }
        }

        // 6. SOURCE TRANSPARENCY & CITATIONS
        BentoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            cornerRadius = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "SOURCES & CITATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Every price and specification is traced to verified public shopping portals:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (data.sources.isEmpty()) {
                    Text(
                        text = "Data synthesized through live Google Search index.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    data.sources.forEach { src ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                                .clickable(enabled = src.url.isNotBlank()) {
                                    if (src.url.isNotBlank()) onOpenUrl(src.url)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = src.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (src.url.isNotBlank()) BentoIndigoLight else MaterialTheme.colorScheme.onBackground
                                )
                                if (src.informationObtained.isNotBlank()) {
                                    Text(
                                        text = src.informationObtained,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (src.timestamp.isNotBlank()) {
                                Text(
                                    text = src.timestamp,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
 * Individual modern product comparison card with platform branding, prices, and Shop Now button
 */
@Composable
private fun DecideProductCard(
    platform: PlatformPriceInfo,
    isCheapest: Boolean,
    productName: String,
    brandModel: String,
    productImage: String,
    onShopNow: () -> Unit
) {
    val borderColor = if (isCheapest) BentoEmerald.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val bgColor = if (isCheapest) BentoEmeraldContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${platform.platform.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Platform Name Badge & Lowest Verified indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlatformBadge(platform = platform.platform)

                    if (platform.matchType.isNotBlank() && platform.matchType != "Exact Match") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BentoAmberContainer
                        ) {
                            Text(
                                text = platform.matchType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isCheapest) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BentoEmeraldContainer
                    ) {
                        Text(
                            text = "★ Lowest Price",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Main Product Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (productImage.isNotBlank()) {
                    AsyncImage(
                        model = productImage,
                        contentDescription = productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.background)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = productName.ifBlank { "Product Item" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (brandModel.isNotBlank()) {
                        Text(
                            text = brandModel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (platform.variantDetail.isNotBlank()) {
                        Text(
                            text = "Variant: ${platform.variantDetail}",
                            fontSize = 11.sp,
                            color = BentoIndigoLight
                        )
                    }
                }
            }

            if (platform.matchExplanation.isNotBlank() && platform.matchType != "Exact Match") {
                Text(
                    text = "Note: ${platform.matchExplanation}",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = BentoAmber
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Pricing & Ratings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Price & Discount Column
                Column {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val currentPriceText = when {
                            platform.price != null && platform.price > 0 -> "₹${platform.price.toLong()}"
                            platform.priceFormatted != null && !platform.priceFormatted.contains("unavailable", ignoreCase = true) -> platform.priceFormatted
                            else -> "Data unavailable"
                        }

                        Text(
                            text = currentPriceText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCheapest) BentoEmerald else MaterialTheme.colorScheme.onBackground
                        )

                        if (platform.originalPrice != null && platform.originalPrice > (platform.price ?: 0.0)) {
                            Text(
                                text = "₹${platform.originalPrice.toLong()}",
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        if (platform.discountPercentage != null && platform.discountPercentage > 0) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BentoRose.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${platform.discountPercentage}% OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoRose,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Delivery & Stock Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = BentoIndigoLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${platform.deliveryInfo.ifBlank { "Standard Delivery" }} • ${platform.availability}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Rating & Review Column
                Column(horizontalAlignment = Alignment.End) {
                    if (platform.rating != null && platform.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = BentoAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${platform.rating}★",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (platform.reviewCount != null && platform.reviewCount > 0) {
                            Text(
                                text = "(${platform.reviewCount} reviews)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Rating not available",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // SHOP NOW BUTTON
            val hasValidUrl = platform.url.isNotBlank() && (platform.url.startsWith("http://") || platform.url.startsWith("https://"))

            Button(
                onClick = onShopNow,
                enabled = hasValidUrl,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCheapest) BentoEmerald else BentoIndigoLight,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("shop_now_${platform.platform.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (hasValidUrl) "SHOP NOW ON ${platform.platform.uppercase()} →" else "Link unavailable",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (hasValidUrl) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Platform branding badge with color codes
 */
@Composable
private fun PlatformBadge(platform: String) {
    val (color, container) = when {
        platform.contains("Amazon", ignoreCase = true) -> Color(0xFFFF9900) to Color(0x26FF9900)
        platform.contains("Flipkart", ignoreCase = true) -> Color(0xFF2874F0) to Color(0x262874F0)
        platform.contains("Croma", ignoreCase = true) -> Color(0xFF00B5B8) to Color(0x2600B5B8)
        platform.contains("Reliance", ignoreCase = true) -> Color(0xFFE42529) to Color(0x26E42529)
        platform.contains("Vijay", ignoreCase = true) -> Color(0xFFE91E63) to Color(0x26E91E63)
        platform.contains("Myntra", ignoreCase = true) -> Color(0xFFFF3F6C) to Color(0x26FF3F6C)
        else -> BentoIndigoLight to BentoIndigoContainer
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = platform,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun QualitySignalRow(
    label: String,
    explanation: String,
    rating: Int
) {
    if (explanation.isBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                for (i in 1..5) {
                    Text(
                        text = if (i <= rating) "★" else "☆",
                        fontSize = 11.sp,
                        color = if (i <= rating) BentoAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Text(
            text = "“$explanation”",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchEmptyGuideCard() {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        borderColor = BentoAmber.copy(alpha = 0.3f),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = BentoAmber,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Live India Multi-Platform Search",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Search any consumer product by typing or tapping the + button to photograph an item. Compare verified pricing across Amazon, Flipkart, Croma, Reliance Digital, and official stores in real time.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveSearchingIndicatorCard(stepMessage: String) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = BentoAmber,
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = BentoAmber,
                modifier = Modifier.size(34.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = "RESEARCHING MULTI-PLATFORM STORES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BentoAmber,
                letterSpacing = 1.sp
            )
            Text(
                text = stepMessage,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Querying live retail indexes via Google Search grounding…",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusNoticeCard(
    title: String,
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isError) BentoRose.copy(alpha = 0.15f) else BentoAmberContainer.copy(alpha = 0.5f),
        borderColor = if (isError) BentoRose.copy(alpha = 0.6f) else BentoAmber.copy(alpha = 0.6f),
        cornerRadius = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.Info,
                        contentDescription = null,
                        tint = if (isError) BentoRose else BentoAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) BentoRose else BentoAmber
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (onRetry != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isError) BentoRose else BentoAmber
                        ),
                        border = BorderStroke(1.dp, if (isError) BentoRose.copy(alpha = 0.6f) else BentoAmber.copy(alpha = 0.6f)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningBannerCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = BentoAmberContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, BentoAmber.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = BentoAmber,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = message,
                fontSize = 11.sp,
                color = BentoAmber,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SuccessBannerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = BentoEmeraldContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = BentoEmerald,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Multi-platform comparison verified against live shopping sources",
                fontSize = 11.sp,
                color = BentoEmerald,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PurchaseHistoryCard(
    purchase: PurchaseEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(purchase.dateAnalyzed) {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(purchase.dateAnalyzed))
    }

    val verdictColor = when (purchase.verdict) {
        "BUY" -> BentoEmerald
        "WAIT" -> BentoAmber
        else -> BentoRose
    }
    val verdictContainer = when (purchase.verdict) {
        "BUY" -> BentoEmeraldContainer
        "WAIT" -> BentoAmberContainer
        else -> Color(0x26F43F5E)
    }

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        cornerRadius = 18.dp
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
                        text = purchase.verdict,
                        color = verdictColor,
                        containerColor = verdictContainer
                    )
                    if (purchase.price > 0) {
                        Text(
                            text = "₹${purchase.price.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                text = purchase.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = purchase.reasoning,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (purchase.alternatives.isNotBlank()) {
                Text(
                    text = purchase.alternatives,
                    fontSize = 11.sp,
                    color = BentoIndigoLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Creates temporary image file uri for Camera capture using FileProvider
 */
private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("decide_photo_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempFile
    )
}

/**
 * Safe conversion from Uri to Bitmap
 */
private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}

private fun openBrowserUrl(context: Context, url: String) {
    try {
        val validUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open destination link.", Toast.LENGTH_SHORT).show()
    }
}
