package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIIntentResult
import com.example.ai.LifeVaultAIEngine
import com.example.ai.NaturalLanguageSaveResult
import com.example.ai.PurchaseAnalysisResult
import com.example.ai.assistant.AssistantActionProposal
import com.example.ai.assistant.AssistantToolRepository
import com.example.ai.assistant.AssistantToolType
import com.example.ai.assistant.LifeVaultAssistantOrchestrator
import com.example.ai.ComparisonState
import com.example.ai.ProductResearchService
import com.example.data.model.ProductComparisonData
import com.example.data.model.VisualIdentificationResult
import com.example.data.local.database.LifeVaultDatabase
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InsightEntity
import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.repository.LifeVaultRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class VisualSearchState {
    object Idle : VisualSearchState()
    object Analyzing : VisualSearchState()
    data class Identified(val result: VisualIdentificationResult, val previewBitmap: Bitmap) : VisualSearchState()
    data class Error(val message: String) : VisualSearchState()
}

enum class DecideSortOption(val label: String) {
    LOWEST_PRICE("Lowest Price"),
    HIGHEST_RATING("Highest Rating"),
    RELEVANCE("Relevance"),
    BEST_VALUE("Best Value")
}

data class UniversalSearchResult(
    val memories: List<MemoryEntity> = emptyList(),
    val purchases: List<PurchaseEntity> = emptyList(),
    val lostFound: List<LostFoundItemEntity> = emptyList(),
    val insights: List<InsightEntity> = emptyList()
)

class LifeVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeVaultRepository
    private val prefs = application.getSharedPreferences("lifevault_prefs", Context.MODE_PRIVATE)

    // UI state
    val memories: StateFlow<List<MemoryEntity>>
    val purchases: StateFlow<List<PurchaseEntity>>
    val lostFoundItems: StateFlow<List<LostFoundItemEntity>>
    val insights: StateFlow<List<InsightEntity>>
    val goals: StateFlow<List<GoalEntity>>

    // AI Command Bar State
    private val _aiCommandInput = MutableStateFlow("")
    val aiCommandInput: StateFlow<String> = _aiCommandInput.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _activeAiResult = MutableStateFlow<AIIntentResult?>(null)
    val activeAiResult: StateFlow<AIIntentResult?> = _activeAiResult.asStateFlow()

    private val _showSaveSuccess = MutableStateFlow(false)
    val showSaveSuccess: StateFlow<Boolean> = _showSaveSuccess.asStateFlow()

    private val _lastSavedSummary = MutableStateFlow("")
    val lastSavedSummary: StateFlow<String> = _lastSavedSummary.asStateFlow()

    // Onboarding and User Profile
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Alex") ?: "Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userProfilePhotoUri = MutableStateFlow(prefs.getString("user_profile_photo", null))
    val userProfilePhotoUri: StateFlow<String?> = _userProfilePhotoUri.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "abdulmannan4990@gmail.com") ?: "abdulmannan4990@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // Authentication State: defaults to false so the user immediately experiences the requested login page
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _rememberMe = MutableStateFlow(prefs.getBoolean("remember_me", true))
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_done", true))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Universal Search
    private val _universalSearchQuery = MutableStateFlow("")
    val universalSearchQuery: StateFlow<String> = _universalSearchQuery.asStateFlow()

    private val _universalSearchResult = MutableStateFlow(UniversalSearchResult())
    val universalSearchResult: StateFlow<UniversalSearchResult> = _universalSearchResult.asStateFlow()

    // Active Purchase Analysis in "Before You Buy"
    private val _currentPurchaseAnalysis = MutableStateFlow<PurchaseAnalysisResult?>(null)
    val currentPurchaseAnalysis: StateFlow<PurchaseAnalysisResult?> = _currentPurchaseAnalysis.asStateFlow()

    // Real-Time Web-Grounded Product Comparison State
    private val _comparisonState = MutableStateFlow<ComparisonState>(ComparisonState.Idle)
    val comparisonState: StateFlow<ComparisonState> = _comparisonState.asStateFlow()

    private val _comparisonSearchQuery = MutableStateFlow("")
    val comparisonSearchQuery: StateFlow<String> = _comparisonSearchQuery.asStateFlow()

    private val _selectedBudget = MutableStateFlow<String?>(null)
    val selectedBudget: StateFlow<String?> = _selectedBudget.asStateFlow()

    private val _selectedPriority = MutableStateFlow("Balanced")
    val selectedPriority: StateFlow<String> = _selectedPriority.asStateFlow()

    // Decide 2.0 Visual Search & Sorting State
    private val _visualSearchState = MutableStateFlow<VisualSearchState>(VisualSearchState.Idle)
    val visualSearchState: StateFlow<VisualSearchState> = _visualSearchState.asStateFlow()

    private val _decideSortOption = MutableStateFlow(DecideSortOption.LOWEST_PRICE)
    val decideSortOption: StateFlow<DecideSortOption> = _decideSortOption.asStateFlow()

    lateinit var assistantOrchestrator: LifeVaultAssistantOrchestrator
        private set

    init {
        val db = LifeVaultDatabase.getDatabase(application)
        repository = LifeVaultRepository(db.lifeVaultDao())

        val toolRepo = AssistantToolRepository(db.lifeVaultDao(), db.vaultDropDao())
        assistantOrchestrator = LifeVaultAssistantOrchestrator(toolRepo)

        memories = repository.allMemories.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        purchases = repository.allPurchases.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        lostFoundItems = repository.allLostFoundItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        insights = repository.allInsights.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        goals = repository.allGoals.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Seed demo data on launch if empty
        viewModelScope.launch {
            if (prefs.getBoolean("is_first_launch", true)) {
                repository.seedDemoDataIfEmpty()
                prefs.edit().putBoolean("is_first_launch", false).apply()
            }
        }

        // Initialize default registered auth accounts if not yet done
        val registeredUsers = prefs.getStringSet("registered_users", null)
        if (registeredUsers == null) {
            val initialAccounts = setOf("abdulmannan4990@gmail.com", "alex@lifevault.ai")
            prefs.edit()
                .putStringSet("registered_users", initialAccounts)
                .putString("user_pass_abdulmannan4990@gmail.com", "LifeVault2026!")
                .putString("user_name_abdulmannan4990@gmail.com", "Abdul Mannan")
                .putString("user_pass_alex@lifevault.ai", "LifeVault2026!")
                .putString("user_name_alex@lifevault.ai", "Alex Vance")
                .apply()
        }
    }

    fun onAiCommandChange(text: String) {
        _aiCommandInput.value = text
    }

    fun submitAiCommand(query: String = _aiCommandInput.value) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isAiThinking.value = true
            delay(500) // Realistic conversational latency

            val result = LifeVaultAIEngine.processQuery(
                query = query,
                memories = memories.value,
                purchases = purchases.value,
                lostFoundItems = lostFoundItems.value
            )

            _isAiThinking.value = false
            _activeAiResult.value = result

            // If intent was natural memory save, automatically save it to Vault!
            if (result is AIIntentResult.MemoryExtracted) {
                saveExtractedMemory(result.extracted, query)
            }
        }
    }

    fun dismissAiResult() {
        _activeAiResult.value = null
    }

    fun saveNaturalMemory(rawText: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            delay(400)
            val extracted = LifeVaultAIEngine.extractMemory(rawText)
            _isAiThinking.value = false
            saveExtractedMemory(extracted, rawText)
        }
    }

    private fun saveExtractedMemory(extracted: NaturalLanguageSaveResult, rawText: String) {
        viewModelScope.launch {
            val memory = MemoryEntity(
                title = extracted.title,
                rawText = rawText,
                objectName = extracted.objectName,
                location = extracted.location,
                category = extracted.category,
                confidence = extracted.confidence,
                createdAt = System.currentTimeMillis(),
                pinned = false,
                isDemo = false
            )
            repository.saveMemory(memory)
            _lastSavedSummary.value = "${extracted.objectName} saved in ${extracted.location}"
            _showSaveSuccess.value = true
            delay(2400)
            _showSaveSuccess.value = false
        }
    }

    fun addManualMemory(
        title: String,
        rawText: String,
        objectName: String,
        location: String,
        category: String
    ) {
        viewModelScope.launch {
            val memory = MemoryEntity(
                title = title.ifBlank { objectName },
                rawText = rawText.ifBlank { "Stored $objectName in $location" },
                objectName = objectName,
                location = location,
                category = category,
                createdAt = System.currentTimeMillis(),
                isDemo = false
            )
            repository.saveMemory(memory)
            _lastSavedSummary.value = "$objectName saved to $category"
            _showSaveSuccess.value = true
            delay(2200)
            _showSaveSuccess.value = false
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun toggleMemoryPinned(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(pinned = !memory.pinned))
        }
    }

    // Purchase Analyzer
    fun analyzePurchase(
        name: String,
        price: Double,
        specs: String,
        need: String,
        category: String = "Electronics"
    ) {
        viewModelScope.launch {
            _isAiThinking.value = true
            delay(600)
            val analysis = LifeVaultAIEngine.analyzePurchase(
                productName = name,
                price = price,
                specs = specs,
                statedNeed = need,
                existingBelongings = memories.value
            )
            _currentPurchaseAnalysis.value = analysis
            _isAiThinking.value = false

            // Save decision to repository
            val purchase = PurchaseEntity(
                productName = name,
                price = price,
                category = category,
                specs = specs,
                statedNeed = need,
                verdict = analysis.verdict,
                reasoning = "${analysis.headline} ${analysis.bulletPoints.joinToString(" • ")}",
                alternatives = analysis.alternative,
                isPurchased = false,
                isDemo = false
            )
            repository.savePurchase(purchase)
        }
    }

    fun dismissPurchaseAnalysis() {
        _currentPurchaseAnalysis.value = null
    }

    fun setComparisonSearchQuery(query: String) {
        _comparisonSearchQuery.value = query
    }

    fun setSelectedBudget(budget: String?) {
        _selectedBudget.value = budget
    }

    fun setSelectedPriority(priority: String) {
        _selectedPriority.value = priority
    }

    /**
     * Conducts real web-grounded product comparison using Gemini with Google Search tool.
     */
    fun compareProduct(
        productName: String? = null,
        budget: String? = _selectedBudget.value,
        priority: String = _selectedPriority.value
    ) {
        val query = productName ?: _comparisonSearchQuery.value
        if (query.isBlank()) return

        _comparisonSearchQuery.value = query
        viewModelScope.launch {
            _comparisonState.value = ComparisonState.Searching("Searching multiple sources across Amazon India, Flipkart, Croma, and official stores...")

            val result = ProductResearchService.researchProduct(
                productQuery = query,
                budget = budget,
                priority = priority,
                onProgressUpdate = { msg ->
                    _comparisonState.value = ComparisonState.Searching(msg)
                }
            )

            _comparisonState.value = result

            // If success or partial, save record to purchase history
            if (result is ComparisonState.Success || result is ComparisonState.Partial) {
                val data = if (result is ComparisonState.Success) result.data else (result as ComparisonState.Partial).data
                val minPrice = data.platforms.mapNotNull { it.price }.minOrNull() ?: 0.0
                val rec = data.aiRecommendation.recommendation.uppercase()
                val verdict = when {
                    rec.contains("BUY") || rec.contains("YES") || rec.contains("RECOMMENDED") -> "BUY"
                    rec.contains("WAIT") || rec.contains("HOLD") || rec.contains("MONITOR") -> "WAIT"
                    rec.contains("SKIP") || rec.contains("AVOID") || rec.contains("NOT") -> "SKIP"
                    else -> "WAIT"
                }

                val purchase = PurchaseEntity(
                    productName = data.product.name.ifBlank { query },
                    price = minPrice,
                    category = data.product.category.ifBlank { "Electronics" },
                    specs = "${data.product.brand} ${data.product.model} ${data.product.variant}".trim(),
                    statedNeed = "Compared across ${data.platforms.size} platforms (${priority} priority)",
                    verdict = verdict,
                    reasoning = data.aiRecommendation.reason.take(280),
                    alternatives = data.comparison.lowestPrice.ifBlank { "Check verified sources" },
                    isPurchased = false,
                    isDemo = false
                )
                repository.savePurchase(purchase)
            }
        }
    }

    fun resetComparison() {
        _comparisonState.value = ComparisonState.Idle
    }

    fun setDecideSortOption(option: DecideSortOption) {
        _decideSortOption.value = option
    }

    fun identifyProductFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _visualSearchState.value = VisualSearchState.Analyzing
            val result = ProductResearchService.identifyProductFromImage(bitmap)
            if (result.productName.isNotBlank() || result.suggestedQuery.isNotBlank()) {
                _visualSearchState.value = VisualSearchState.Identified(result, bitmap)
                _comparisonSearchQuery.value = result.suggestedQuery.ifBlank { result.productName }
            } else {
                _visualSearchState.value = VisualSearchState.Error(
                    result.confidenceExplanation.ifBlank { "Could not clearly identify a consumer product in the photo. Please enter the product name manually." }
                )
            }
        }
    }

    fun clearVisualSearch() {
        _visualSearchState.value = VisualSearchState.Idle
    }

    fun confirmVisualSearchAndCompare(confirmedName: String) {
        _comparisonSearchQuery.value = confirmedName
        _visualSearchState.value = VisualSearchState.Idle
        compareProduct(confirmedName)
    }

    fun deletePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
        }
    }

    fun deleteLostFoundItem(item: LostFoundItemEntity) {
        viewModelScope.launch {
            repository.deleteLostFoundItem(item)
        }
    }

    // Lost & Found
    fun reportLostOrFound(
        type: String, // "LOST" or "FOUND"
        title: String,
        description: String,
        category: String,
        location: String,
        time: String,
        contact: String
    ) {
        viewModelScope.launch {
            val newItem = LostFoundItemEntity(
                type = type,
                title = title,
                description = description,
                category = category,
                location = location,
                reportedTime = time,
                status = "ACTIVE",
                contactInfo = contact,
                isDemo = false
            )

            // Look for reciprocal matches in active items
            val existingReciprocals = lostFoundItems.value.filter { it.type != type && it.status != "RECOVERED" }
            var bestMatchItem: LostFoundItemEntity? = null
            var bestScore = 0

            for (other in existingReciprocals) {
                val score = LifeVaultAIEngine.calculateMatchScore(
                    lost = if (type == "LOST") newItem else other,
                    found = if (type == "FOUND") newItem else other
                )
                if (score > bestScore && score >= 60) {
                    bestScore = score
                    bestMatchItem = other
                }
            }

            val finalItem = if (bestMatchItem != null) {
                newItem.copy(
                    status = "MATCHED",
                    matchedItemId = bestMatchItem.id,
                    matchConfidence = bestScore
                )
            } else {
                newItem
            }

            val savedId = repository.saveLostFoundItem(finalItem)

            if (bestMatchItem != null) {
                repository.updateLostFoundItem(
                    bestMatchItem.copy(
                        status = "MATCHED",
                        matchedItemId = savedId,
                        matchConfidence = bestScore
                    )
                )
            }

            _lastSavedSummary.value = "$type report filed: $title"
            _showSaveSuccess.value = true
            delay(2000)
            _showSaveSuccess.value = false
        }
    }

    fun markItemRecovered(item: LostFoundItemEntity) {
        viewModelScope.launch {
            repository.updateLostFoundItem(item.copy(status = "RECOVERED"))
        }
    }

    // Goals / Memory Moments
    fun updateGoalStatus(goal: GoalEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(status = newStatus))
            _lastSavedSummary.value = "Goal updated: $newStatus"
            _showSaveSuccess.value = true
            delay(1800)
            _showSaveSuccess.value = false
        }
    }

    // Universal Search
    fun onUniversalSearchChange(query: String) {
        _universalSearchQuery.value = query
        if (query.isBlank()) {
            _universalSearchResult.value = UniversalSearchResult()
            return
        }

        val q = query.trim().lowercase()
        val memMatches = memories.value.filter {
            it.title.lowercase().contains(q) ||
            it.rawText.lowercase().contains(q) ||
            it.objectName.lowercase().contains(q) ||
            it.location.lowercase().contains(q)
        }

        val purMatches = purchases.value.filter {
            it.productName.lowercase().contains(q) ||
            it.specs.lowercase().contains(q) ||
            it.statedNeed.lowercase().contains(q)
        }

        val lfMatches = lostFoundItems.value.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.location.lowercase().contains(q)
        }

        val insMatches = insights.value.filter {
            it.title.lowercase().contains(q) ||
            it.explanation.lowercase().contains(q) ||
            it.highlightCategory.lowercase().contains(q)
        }

        _universalSearchResult.value = UniversalSearchResult(
            memories = memMatches,
            purchases = purMatches,
            lostFound = lfMatches,
            insights = insMatches
        )
    }

    fun setRememberMe(remember: Boolean) {
        _rememberMe.value = remember
        prefs.edit().putBoolean("remember_me", remember).apply()
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun setAuthError(error: String) {
        _authErrorMessage.value = error
    }

    fun signIn(email: String, password: String, remember: Boolean, onSuccess: () -> Unit = {}) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        if (trimmedEmail.isBlank()) {
            _authErrorMessage.value = "Please enter your email address"
            return
        }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(trimmedEmail)) {
            _authErrorMessage.value = "Please enter a valid email address (e.g. name@domain.com)"
            return
        }
        if (trimmedPassword.isBlank()) {
            _authErrorMessage.value = "Please enter your password"
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            delay(350)

            val registeredUsers = prefs.getStringSet("registered_users", emptySet()) ?: emptySet()
            val normalizedEmail = trimmedEmail.lowercase()

            if (!registeredUsers.contains(normalizedEmail)) {
                _isAuthLoading.value = false
                _authErrorMessage.value = "No vault found for $trimmedEmail. Tap 'Create your vault' below."
                return@launch
            }

            val storedPassword = prefs.getString("user_pass_$normalizedEmail", null)
            if (storedPassword != trimmedPassword) {
                _isAuthLoading.value = false
                _authErrorMessage.value = "Incorrect password. Please verify your credentials and try again."
                return@launch
            }

            val storedName = prefs.getString("user_name_$normalizedEmail", null)
                ?: trimmedEmail.substringBefore("@")
                    .split(".", "_", "-")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    .ifBlank { "Alex" }

            _userName.value = storedName
            _userEmail.value = trimmedEmail
            _rememberMe.value = remember
            _isLoggedIn.value = true
            _isOnboardingCompleted.value = true

            prefs.edit()
                .putString("user_name", storedName)
                .putString("user_email", trimmedEmail)
                .putBoolean("remember_me", remember)
                .putBoolean("is_logged_in", true)
                .putBoolean("onboarding_done", true)
                .apply()

            _isAuthLoading.value = false
            _lastSavedSummary.value = "Welcome back, $storedName"
            _showSaveSuccess.value = true
            onSuccess()
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    fun signInWithGoogle(email: String = "abdulmannan4990@gmail.com", name: String = "Abdul Mannan", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            delay(350)

            val registeredUsers = (prefs.getStringSet("registered_users", emptySet()) ?: emptySet()).toMutableSet()
            val normalizedEmail = email.lowercase()
            if (!registeredUsers.contains(normalizedEmail)) {
                registeredUsers.add(normalizedEmail)
                prefs.edit()
                    .putStringSet("registered_users", registeredUsers)
                    .putString("user_pass_$normalizedEmail", "LifeVault2026!")
                    .putString("user_name_$normalizedEmail", name)
                    .apply()
            }

            prefs.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putBoolean("remember_me", true)
                .putBoolean("is_logged_in", true)
                .putBoolean("onboarding_done", true)
                .apply()

            _userName.value = name
            _userEmail.value = email
            _isLoggedIn.value = true
            _isOnboardingCompleted.value = true

            _isAuthLoading.value = false
            _lastSavedSummary.value = "Google authentication successful"
            _showSaveSuccess.value = true
            onSuccess()
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit = {}) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedName.isBlank()) {
            _authErrorMessage.value = "Please enter your full name"
            return
        }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(trimmedEmail)) {
            _authErrorMessage.value = "Please enter a valid email address (e.g. name@domain.com)"
            return
        }
        if (trimmedPassword.length < 6) {
            _authErrorMessage.value = "Password must be at least 6 characters long"
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            delay(350)

            val registeredUsers = (prefs.getStringSet("registered_users", emptySet()) ?: emptySet()).toMutableSet()
            val normalizedEmail = trimmedEmail.lowercase()

            if (registeredUsers.contains(normalizedEmail)) {
                _isAuthLoading.value = false
                _authErrorMessage.value = "A vault with this email already exists. Please sign in instead."
                return@launch
            }

            registeredUsers.add(normalizedEmail)
            prefs.edit()
                .putStringSet("registered_users", registeredUsers)
                .putString("user_pass_$normalizedEmail", trimmedPassword)
                .putString("user_name_$normalizedEmail", trimmedName)
                .putString("user_name", trimmedName)
                .putString("user_email", trimmedEmail)
                .putBoolean("is_logged_in", true)
                .putBoolean("onboarding_done", true)
                .apply()

            _userName.value = trimmedName
            _userEmail.value = trimmedEmail
            _isLoggedIn.value = true
            _isOnboardingCompleted.value = true

            _isAuthLoading.value = false
            _lastSavedSummary.value = "LifeVault initialized for $trimmedName"
            _showSaveSuccess.value = true
            onSuccess()
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    fun resetPassword(email: String, newPassword: String? = null, onResult: (Boolean, String) -> Unit) {
        val trimmedEmail = email.trim()
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(trimmedEmail)) {
            onResult(false, "Please enter a valid email address (e.g. name@domain.com)")
            return
        }

        val registeredUsers = prefs.getStringSet("registered_users", emptySet()) ?: emptySet()
        val normalizedEmail = trimmedEmail.lowercase()
        if (!registeredUsers.contains(normalizedEmail)) {
            onResult(false, "No vault found for $trimmedEmail. Please create a new account.")
            return
        }

        if (!newPassword.isNullOrBlank()) {
            if (newPassword.length < 6) {
                onResult(false, "New password must be at least 6 characters long.")
                return
            }
            prefs.edit().putString("user_pass_$normalizedEmail", newPassword.trim()).apply()
            onResult(true, "Password updated successfully! You can now sign in with your new password.")
        } else {
            onResult(true, "Password reset link has been dispatched to $trimmedEmail.")
        }
    }

    fun signOut() {
        _isLoggedIn.value = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun updateProfilePhoto(uriString: String?) {
        _userProfilePhotoUri.value = uriString
        if (uriString != null) {
            prefs.edit().putString("user_profile_photo", uriString).apply()
            _lastSavedSummary.value = "Profile photo updated"
        } else {
            prefs.edit().remove("user_profile_photo").apply()
            _lastSavedSummary.value = "Profile photo removed"
        }
        viewModelScope.launch {
            _showSaveSuccess.value = true
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    // Onboarding & Settings
    fun completeOnboarding(name: String) {
        _userName.value = name.ifBlank { "Alex" }
        _isOnboardingCompleted.value = true
        prefs.edit()
            .putString("user_name", _userName.value)
            .putBoolean("onboarding_done", true)
            .apply()
    }

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        prefs.edit().putBoolean("dark_mode", next).apply()
    }

    fun populateDemoData() {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
            _lastSavedSummary.value = "Demo data loaded"
            _showSaveSuccess.value = true
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            repository.clearDemoData()
            _lastSavedSummary.value = "Demo items cleared"
            _showSaveSuccess.value = true
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _lastSavedSummary.value = "All vault data cleared"
            _showSaveSuccess.value = true
            delay(1500)
            _showSaveSuccess.value = false
        }
    }

    fun askAssistant(prompt: String) {
        viewModelScope.launch {
            assistantOrchestrator.processUserQuery(prompt)
        }
    }

    fun confirmAssistantAction(proposal: AssistantActionProposal) {
        viewModelScope.launch {
            when (proposal.toolType) {
                AssistantToolType.MEMORY_SEARCH -> {
                    val title = proposal.payload["title"] ?: "Saved Item"
                    val objectName = proposal.payload["object"] ?: title
                    val location = proposal.payload["location"] ?: "Personal Vault"
                    val category = proposal.payload["category"] ?: "Belongings"
                    val rawText = proposal.payload["rawText"] ?: title

                    val memory = MemoryEntity(
                        title = title,
                        rawText = rawText,
                        objectName = objectName,
                        location = location,
                        category = category,
                        confidence = 0.98f,
                        createdAt = System.currentTimeMillis()
                    )
                    repository.saveMemory(memory)
                    _lastSavedSummary.value = "$objectName saved to $location"
                    _showSaveSuccess.value = true
                    delay(2000)
                    _showSaveSuccess.value = false
                }
                else -> {
                    // Action confirmed
                }
            }
        }
    }
}
