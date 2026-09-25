package com.example.ai.assistant

import com.example.ai.ComparisonState
import com.example.ai.LifeVaultAIEngine
import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.VaultDropFileEntity
import com.example.data.model.ProductComparisonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class MessageSender {
    USER, ASSISTANT, SYSTEM
}

data class SourceRecord(
    val title: String,
    val subtitle: String,
    val badge: String,
    val toolType: AssistantToolType,
    val referenceId: Long? = null,
    val url: String? = null
)

data class AssistantActionProposal(
    val actionId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val toolType: AssistantToolType,
    val payload: Map<String, String>
)

data class ExplainabilityReport(
    val informationFound: List<String> = emptyList(),
    val sourcesUsed: List<String> = emptyList(),
    val missingInformation: List<String> = emptyList(),
    val inferences: List<String> = emptyList(),
    val nextSteps: List<String> = emptyList()
)

data class AssistantMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolType: AssistantToolType? = null,
    val sources: List<SourceRecord> = emptyList(),
    val toolResult: ToolExecutionResult? = null,
    val actionProposal: AssistantActionProposal? = null,
    val explainability: ExplainabilityReport? = null,
    val smartSuggestions: List<String> = emptyList(),
    val isPending: Boolean = false,
    val isError: Boolean = false
)

data class SuggestedPrompt(
    val title: String,
    val prompt: String,
    val toolType: AssistantToolType
)

data class ActiveConversationContext(
    var lastEntityName: String? = null,
    var lastCategory: String? = null,
    var lastPurchaseItem: PurchaseEntity? = null,
    var lastMemoryItem: MemoryEntity? = null,
    var lastDocumentItem: VaultDropFileEntity? = null,
    var lastProductComparison: ProductComparisonData? = null
)

class LifeVaultAssistantOrchestrator(
    private val toolRepository: AssistantToolRepository
) {
    private val _messages = MutableStateFlow<List<AssistantMessage>>(emptyList())
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentStatus = MutableStateFlow<String?>(null)
    val currentStatus: StateFlow<String?> = _currentStatus.asStateFlow()

    // Active conversation context for pronoun and anaphora resolution
    private val activeContext = ActiveConversationContext()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    val suggestedPrompts = listOf(
        SuggestedPrompt(
            title = "Multi-Tool Laptop Check",
            prompt = "Find my laptop bill, summarize its warranty information, and compare the laptop with another model.",
            toolType = AssistantToolType.MULTI_TOOL_WORKFLOW
        ),
        SuggestedPrompt(
            title = "Locate Belongings",
            prompt = "Where did I store my passport and keys?",
            toolType = AssistantToolType.MEMORY_SEARCH
        ),
        SuggestedPrompt(
            title = "Compare Laptops",
            prompt = "Compare MacBook Air M3 and Dell XPS 13 before I buy",
            toolType = AssistantToolType.PRODUCT_COMPARISON
        ),
        SuggestedPrompt(
            title = "Search Lost Items",
            prompt = "Did anyone find a black leather wallet near the library?",
            toolType = AssistantToolType.LOST_FOUND_SEARCH
        ),
        SuggestedPrompt(
            title = "Find Files & Bills",
            prompt = "Show my stored college documents and receipts",
            toolType = AssistantToolType.DOCUMENT_ASSISTANT
        ),
        SuggestedPrompt(
            title = "Check Warranties",
            prompt = "What purchases did I make this year and what warranties are active?",
            toolType = AssistantToolType.PURCHASE_ASSISTANT
        ),
        SuggestedPrompt(
            title = "Audit File Shares",
            prompt = "Who can access my shared files and are any links expired?",
            toolType = AssistantToolType.PRIVACY_SHARING
        )
    )

    init {
        _messages.value = listOf(
            AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = "Hello! I am your **LifeVault AI Assistant**. I connect your memories, product comparisons, belongings, documents, purchases, and secure file shares.\n\nHow can I help you organize or decide today?"
            )
        )
    }

    /**
     * Dispatches natural language queries to the appropriate authorized tool with contextual awareness.
     */
    suspend fun processUserQuery(userQuery: String) = withContext(Dispatchers.IO) {
        val trimmed = userQuery.trim()
        if (trimmed.isBlank()) return@withContext

        // Append user message
        val userMsg = AssistantMessage(
            sender = MessageSender.USER,
            text = trimmed
        )
        _messages.value = _messages.value + userMsg

        _isProcessing.value = true
        _currentStatus.value = "Understanding your request..."

        try {
            // Contextual resolution: resolve pronouns ('it', 'its', 'the laptop', 'that item')
            val (contextualQuery, resolvedInference) = resolveQueryContext(trimmed)

            // Intelligent intent classification
            val (selectedTool, refinedQuery) = classifyIntent(contextualQuery)

            _currentStatus.value = "Consulting ${selectedTool.displayName}..."

            val toolResult: ToolExecutionResult = when (selectedTool) {
                AssistantToolType.MULTI_TOOL_WORKFLOW -> {
                    _currentStatus.value = "Running multi-tool intelligence workflow..."
                    toolRepository.executeMultiToolWorkflow(
                        query = refinedQuery,
                        onProgress = { step -> _currentStatus.value = step }
                    )
                }

                AssistantToolType.MEMORY_SEARCH -> {
                    if (isSaveIntent(refinedQuery)) {
                        val extracted = LifeVaultAIEngine.extractMemory(refinedQuery)
                        val proposal = AssistantActionProposal(
                            title = "Save Memory to Vault",
                            description = "Add '${extracted.title}' (Category: ${extracted.category}, Location: ${extracted.location})",
                            toolType = AssistantToolType.MEMORY_SEARCH,
                            payload = mapOf(
                                "title" to extracted.title,
                                "object" to extracted.objectName,
                                "location" to extracted.location,
                                "category" to extracted.category,
                                "rawText" to refinedQuery
                            )
                        )
                        _messages.value = _messages.value + AssistantMessage(
                            sender = MessageSender.ASSISTANT,
                            text = "I parsed your note. Would you like me to save this to your Personal Vault?",
                            toolType = AssistantToolType.MEMORY_SEARCH,
                            actionProposal = proposal,
                            smartSuggestions = listOf("Yes, save to Vault", "Change location", "Discard")
                        )
                        _isProcessing.value = false
                        _currentStatus.value = null
                        return@withContext
                    }
                    toolRepository.executeMemorySearch(refinedQuery)
                }

                AssistantToolType.PRODUCT_COMPARISON -> {
                    _currentStatus.value = "Conducting grounded product research..."
                    toolRepository.executeProductComparison(
                        productQuery = refinedQuery,
                        onProgress = { step -> _currentStatus.value = step }
                    )
                }

                AssistantToolType.LOST_FOUND_SEARCH -> {
                    toolRepository.executeLostFoundSearch(refinedQuery)
                }

                AssistantToolType.DOCUMENT_ASSISTANT -> {
                    toolRepository.executeDocumentAssistant(refinedQuery)
                }

                AssistantToolType.PURCHASE_ASSISTANT -> {
                    toolRepository.executePurchaseAssistant(refinedQuery)
                }

                AssistantToolType.PRIVACY_SHARING -> {
                    toolRepository.executePrivacySharingAssistant(refinedQuery)
                }
            }

            // Update conversation context from results
            updateContext(toolResult)

            // Synthesize grounded explanation, explainability report, and smart suggestions
            _currentStatus.value = "Synthesizing answer & explainability..."
            val (responseText, sources, explainability, suggestions) = formatToolResponse(
                selectedTool,
                toolResult,
                contextualQuery,
                resolvedInference
            )

            _messages.value = _messages.value + AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = responseText,
                toolType = selectedTool,
                sources = sources,
                toolResult = toolResult,
                explainability = explainability,
                smartSuggestions = suggestions
            )
        } catch (e: Exception) {
            _messages.value = _messages.value + AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = "I encountered an issue processing your request: ${e.localizedMessage ?: "Unknown error"}. Please try again.",
                isError = true
            )
        } finally {
            _isProcessing.value = false
            _currentStatus.value = null
        }
    }

    /**
     * Resolves pronouns and contextual references against the active conversation context.
     */
    private fun resolveQueryContext(query: String): Pair<String, String?> {
        val lower = query.lowercase()
        val lastEntity = activeContext.lastEntityName ?: activeContext.lastPurchaseItem?.productName

        if (lastEntity != null) {
            val pronouns = listOf("its ", "it ", "it?", "it.", "that item", "that model", "this item", "this model")
            if (pronouns.any { lower.contains(it) } && !lower.contains(lastEntity.lowercase())) {
                val rewritten = query
                    .replace(Regex("\\b(its)\\b", RegexOption.IGNORE_CASE), "$lastEntity's")
                    .replace(Regex("\\b(it|that item|this item|that model|this model)\\b", RegexOption.IGNORE_CASE), lastEntity)
                val inference = "Resolved contextual pronoun to '$lastEntity' from our recent conversation."
                return Pair(rewritten, inference)
            }
        }
        return Pair(query, null)
    }

    /**
     * Updates active conversation state for follow-up turns.
     */
    private fun updateContext(result: ToolExecutionResult) {
        when (result) {
            is ToolExecutionResult.PurchaseSuccess -> {
                val first = result.data.matchedPurchases.firstOrNull()
                if (first != null) {
                    activeContext.lastPurchaseItem = first
                    activeContext.lastEntityName = first.productName
                    activeContext.lastCategory = first.category
                }
            }
            is ToolExecutionResult.ProductSuccess -> {
                val comp = result.data.comparisonData
                if (comp != null) {
                    activeContext.lastProductComparison = comp
                    activeContext.lastEntityName = comp.product.name
                    activeContext.lastCategory = comp.product.category
                }
            }
            is ToolExecutionResult.MemorySuccess -> {
                val mem = result.data.matchedMemories.firstOrNull()
                if (mem != null) {
                    activeContext.lastMemoryItem = mem
                    activeContext.lastEntityName = mem.objectName
                    activeContext.lastCategory = mem.category
                }
            }
            is ToolExecutionResult.DocumentSuccess -> {
                val doc = result.data.matchedFiles.firstOrNull()
                if (doc != null) {
                    activeContext.lastDocumentItem = doc
                    activeContext.lastEntityName = doc.originalFilename
                }
            }
            is ToolExecutionResult.MultiToolSuccess -> {
                val purchase = result.data.purchaseData
                if (purchase != null) {
                    activeContext.lastPurchaseItem = purchase
                    activeContext.lastEntityName = purchase.productName
                }
                val comp = result.data.comparisonData
                if (comp != null) {
                    activeContext.lastProductComparison = comp
                }
            }
            else -> {}
        }
    }

    /**
     * Intent classification heuristic with multi-tool workflow and priority routing
     */
    private fun classifyIntent(query: String): Pair<AssistantToolType, String> {
        val q = query.lowercase()

        // 1. Multi-Tool Workflow: Composite requests (e.g. Find bill + warranty + compare)
        val isMultiTool = (q.contains("bill") || q.contains("receipt") || q.contains("purchase")) &&
                (q.contains("warranty") || q.contains("guarantee")) &&
                (q.contains("compare") || q.contains("vs") || q.contains("another model") || q.contains("newer model") || q.contains("should i buy"))

        if (isMultiTool) {
            return Pair(AssistantToolType.MULTI_TOOL_WORKFLOW, query)
        }

        // 2. File Sharing & Permissions
        if (q.contains("share") || q.contains("shared") || q.contains("link") || q.contains("recipient") ||
            q.contains("who can access") || q.contains("permission") || q.contains("revok") || q.contains("download limit")) {
            return Pair(AssistantToolType.PRIVACY_SHARING, query)
        }

        // 3. Documents & Files in VaultDrop
        if (q.contains("pdf") || q.contains("document") || q.contains("file") || (q.contains("bill") && !q.contains("compare")) ||
            q.contains("receipt") && !q.contains("compare") || q.contains("summarize this") || q.contains("stored files") ||
            q.contains("vaultdrop") || q.contains("scan")) {
            return Pair(AssistantToolType.DOCUMENT_ASSISTANT, query)
        }

        // 4. Purchase Records & Warranties
        if (q.contains("warranty") || q.contains("bought") || q.contains("purchased") || q.contains("spending") ||
            q.contains("purchase history") || q.contains("my expenses") || q.contains("what did i buy")) {
            return Pair(AssistantToolType.PURCHASE_ASSISTANT, query)
        }

        // 5. Before You Buy / Product Comparison
        if (q.contains("compare") || q.contains("should i buy") || q.contains("buy this") || q.contains("vs") ||
            q.contains("which is better") || q.contains("worth buying") || q.contains("best price") || q.contains("specs for")) {
            val cleaned = query.replace("should i buy", "", ignoreCase = true)
                .replace("compare", "", ignoreCase = true)
                .replace("before i buy", "", ignoreCase = true)
                .trim()
            return Pair(AssistantToolType.PRODUCT_COMPARISON, cleaned.ifBlank { query })
        }

        // 6. Lost & Found
        if (q.contains("lost") || q.contains("found") || q.contains("missing") || q.contains("misplaced") ||
            q.contains("did anyone find") || q.contains("report a lost")) {
            return Pair(AssistantToolType.LOST_FOUND_SEARCH, query)
        }

        // 7. Default: Memory Search & Belongings
        return Pair(AssistantToolType.MEMORY_SEARCH, query)
    }

    private fun isSaveIntent(query: String): Boolean {
        val q = query.lowercase().trim()
        return q.startsWith("remember") || q.startsWith("save ") || q.startsWith("i kept ") ||
               q.startsWith("i put my") || q.startsWith("store ")
    }

    /**
     * Formats grounded tool output into user-friendly explanation with source citations, explainability report, and smart suggestions.
     */
    private fun formatToolResponse(
        tool: AssistantToolType,
        result: ToolExecutionResult,
        userQuery: String,
        contextInference: String?
    ): ResponseSynthesis {
        val sources = mutableListOf<SourceRecord>()
        val infoFound = mutableListOf<String>()
        val sourcesUsed = mutableListOf<String>()
        val missingInfo = mutableListOf<String>()
        val inferences = mutableListOf<String>()
        val nextSteps = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        if (contextInference != null) {
            inferences.add(contextInference)
        }

        val text: String = when (result) {
            is ToolExecutionResult.MultiToolSuccess -> {
                val data = result.data
                infoFound.addAll(data.stepsCompleted)
                sourcesUsed.add("LifeVault Database: Authorized Purchases")
                sourcesUsed.add("VaultDrop: Encrypted File Storage")
                sourcesUsed.add("Live Web: Grounded Google Search (Amazon, Flipkart, Brand Stores)")

                missingInfo.addAll(data.missingItems)
                inferences.addAll(data.inferences)

                if (data.purchaseData != null) {
                    sources.add(
                        SourceRecord(
                            title = data.purchaseData.productName,
                            subtitle = "${data.purchaseData.currency}${data.purchaseData.price} • ${data.purchaseData.category}",
                            badge = "Purchase Record",
                            toolType = AssistantToolType.PURCHASE_ASSISTANT,
                            referenceId = data.purchaseData.id
                        )
                    )
                }

                if (data.relatedFile != null) {
                    val sizeKb = (data.relatedFile.sizeBytes / 1024).coerceAtLeast(1)
                    sources.add(
                        SourceRecord(
                            title = data.relatedFile.originalFilename,
                            subtitle = "${sizeKb} KB • ${data.relatedFile.extension.uppercase()}",
                            badge = "VaultDrop File",
                            toolType = AssistantToolType.DOCUMENT_ASSISTANT,
                            referenceId = data.relatedFile.id
                        )
                    )
                }

                if (data.comparisonData != null) {
                    data.comparisonData.platforms.take(3).forEach { p ->
                        if (p.url.isNotBlank()) {
                            sources.add(
                                SourceRecord(
                                    title = p.platform,
                                    subtitle = "${p.currency} ${p.price ?: "N/A"}",
                                    badge = "Live Platform",
                                    toolType = AssistantToolType.PRODUCT_COMPARISON,
                                    url = p.url
                                )
                            )
                        }
                    }
                }

                nextSteps.add("Review full warranty terms before making replacement purchase")
                nextSteps.add("Upload digital invoice PDF to VaultDrop if needed")

                suggestions.add("Would you like to review the warranty details?")
                suggestions.add("Would you like to compare another model?")
                suggestions.add("Would you like to save this comparison summary?")

                data.summary
            }

            is ToolExecutionResult.MemorySuccess -> {
                val data = result.data
                if (data.matchedMemories.isNotEmpty()) {
                    data.matchedMemories.forEach { mem ->
                        infoFound.add("Located '${mem.objectName}' in '${mem.location}' (${mem.category})")
                        sourcesUsed.add("Personal Vault Memory: '${mem.title}'")
                        sources.add(
                            SourceRecord(
                                title = mem.objectName,
                                subtitle = "Location: ${mem.location}",
                                badge = mem.category,
                                toolType = AssistantToolType.MEMORY_SEARCH,
                                referenceId = mem.id
                            )
                        )
                    }
                    suggestions.add("Would you like to update its location?")
                    suggestions.add("Show other items in ${data.matchedMemories.first().location}")
                } else {
                    missingInfo.add("No memory record matching '$userQuery' exists in your vault.")
                    suggestions.add("Remember this location now")
                    suggestions.add("Search all belongings")
                }
                data.summary
            }

            is ToolExecutionResult.ProductSuccess -> {
                val data = result.data
                val comp = data.comparisonData
                if (comp != null) {
                    infoFound.add("Verified product: ${comp.product.name} (${comp.product.brand})")
                    comp.platforms.forEach { p ->
                        if (p.price != null) {
                            infoFound.add("${p.platform}: ${p.currency} ${p.price} (Rating: ${p.rating ?: "N/A"})")
                        }
                        sourcesUsed.add("${p.platform} verified page")
                        sources.add(
                            SourceRecord(
                                title = p.platform,
                                subtitle = "${p.currency} ${p.price ?: "Data unavailable"}",
                                badge = if (p.rating != null) "${p.rating}★" else "Live",
                                toolType = AssistantToolType.PRODUCT_COMPARISON,
                                url = p.url
                            )
                        )
                    }

                    if (data.relatedOwnedItem != null) {
                        inferences.add("Cross-referenced with your vault: You already own '${data.relatedOwnedItem.objectName}' recorded in ${data.relatedOwnedItem.location}.")
                    }

                    suggestions.add("Would you like to compare another model?")
                    suggestions.add("Would you like to check if you already own an alternative?")
                    suggestions.add("Save this product comparison")
                } else {
                    missingInfo.add("Live comparison data unavailable for '$userQuery'")
                    suggestions.add("Try searching with the exact product brand and model name")
                }
                data.summary
            }

            is ToolExecutionResult.LostFoundSuccess -> {
                val data = result.data
                if (data.matchedItems.isNotEmpty()) {
                    data.matchedItems.forEach { item ->
                        infoFound.add("Matched ${item.type} item: '${item.title}' at '${item.location}' (${item.matchConfidence}% confidence)")
                        sourcesUsed.add("Community Registry Post #${item.id}")
                        sources.add(
                            SourceRecord(
                                title = item.title,
                                subtitle = item.location,
                                badge = "${item.matchConfidence}% Match",
                                toolType = AssistantToolType.LOST_FOUND_SEARCH,
                                referenceId = item.id
                            )
                        )
                    }
                    suggestions.add("Would you like to see finder contact instructions?")
                    suggestions.add("Post an official missing item alert")
                } else {
                    missingInfo.add("No nearby community reports matching '$userQuery'")
                    suggestions.add("Post a lost item report to the community")
                }
                data.summary
            }

            is ToolExecutionResult.DocumentSuccess -> {
                val data = result.data
                if (data.matchedFiles.isNotEmpty()) {
                    data.matchedFiles.forEach { file ->
                        val sizeKb = (file.sizeBytes / 1024).coerceAtLeast(1)
                        infoFound.add("File '${file.originalFilename}' (${sizeKb} KB, ${file.extension.uppercase()})")
                        sourcesUsed.add("VaultDrop Encrypted File #${file.id}")
                        sources.add(
                            SourceRecord(
                                title = file.originalFilename,
                                subtitle = "${sizeKb} KB • ${file.extension.uppercase()}",
                                badge = file.scanStatus,
                                toolType = AssistantToolType.DOCUMENT_ASSISTANT,
                                referenceId = file.id
                            )
                        )
                    }
                    suggestions.add("Would you like to save a short summary?")
                    suggestions.add("Would you like to share this file via a secure expiring link?")
                } else {
                    missingInfo.add("No files matching '$userQuery' found in VaultDrop")
                    suggestions.add("Upload a file or bill to VaultDrop")
                }
                data.summary
            }

            is ToolExecutionResult.PurchaseSuccess -> {
                val data = result.data
                if (data.matchedPurchases.isNotEmpty()) {
                    data.matchedPurchases.forEach { p ->
                        infoFound.add("Purchase: ${p.productName} for ${p.currency}${p.price} (${p.category})")
                        sourcesUsed.add("Vault Purchase Record #${p.id}")
                        val expiryStr = if (p.warrantyExpiryDate != null) {
                            "Expires: ${dateFormat.format(Date(p.warrantyExpiryDate))}"
                        } else {
                            "Verdict: ${p.verdict}"
                        }
                        sources.add(
                            SourceRecord(
                                title = p.productName,
                                subtitle = "${p.currency}${p.price} • ${p.category}",
                                badge = expiryStr,
                                toolType = AssistantToolType.PURCHASE_ASSISTANT,
                                referenceId = p.id
                            )
                        )
                    }
                    suggestions.add("Would you like to review the warranty details?")
                    suggestions.add("Would you like to find the receipt bill for this purchase?")
                    suggestions.add("Compare this with a newer model")
                } else {
                    missingInfo.add("No purchase entries matching '$userQuery' in your records")
                    suggestions.add("Record a new purchase")
                }
                data.summary
            }

            is ToolExecutionResult.PrivacySharingSuccess -> {
                val data = result.data
                infoFound.add("${data.totalActiveShares} active share links evaluated")
                sourcesUsed.add("VaultDrop Permissions & Audit Logs")
                data.fileShares.forEach { (file, share) ->
                    val expiryStr = if (share.expiresAt != null) {
                        dateFormat.format(Date(share.expiresAt))
                    } else "Never"
                    sources.add(
                        SourceRecord(
                            title = file.originalFilename,
                            subtitle = if (share.isDisabled) "Expired" else "Active (Expires: $expiryStr)",
                            badge = if (share.isDisabled) "Revoked" else "Shared",
                            toolType = AssistantToolType.PRIVACY_SHARING,
                            referenceId = share.id
                        )
                    )
                }
                suggestions.add("Would you like to revoke expired share links?")
                suggestions.add("Check zero-knowledge transport settings")
                data.summary
            }

            is ToolExecutionResult.Error -> {
                missingInfo.add("Execution failed: ${result.message}")
                "I was unable to complete the query using ${result.tool.displayName}: ${result.message}"
            }
        }

        val report = ExplainabilityReport(
            informationFound = infoFound,
            sourcesUsed = sourcesUsed,
            missingInformation = missingInfo,
            inferences = inferences,
            nextSteps = nextSteps
        )

        return ResponseSynthesis(
            text = text,
            sources = sources,
            explainability = report,
            smartSuggestions = suggestions
        )
    }

    fun clearConversation() {
        activeContext.lastEntityName = null
        activeContext.lastPurchaseItem = null
        activeContext.lastMemoryItem = null
        activeContext.lastDocumentItem = null
        activeContext.lastProductComparison = null

        _messages.value = listOf(
            AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = "Conversation reset. Context cleared. How can I help you today?"
            )
        )
    }
}

private data class ResponseSynthesis(
    val text: String,
    val sources: List<SourceRecord>,
    val explainability: ExplainabilityReport,
    val smartSuggestions: List<String>
)
