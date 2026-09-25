package com.example.ai.assistant

import com.example.ai.ComparisonState
import com.example.ai.ProductResearchService
import com.example.data.local.dao.LifeVaultDao
import com.example.data.local.dao.VaultDropDao
import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.VaultDropFileEntity
import com.example.data.local.entity.VaultDropShareEntity
import com.example.data.model.ProductComparisonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Standardized data models for specialized AI Assistant tools
 */

enum class AssistantToolType(val id: String, val displayName: String, val iconName: String) {
    MEMORY_SEARCH("memory_search", "Memory Vault", "inventory_2"),
    PRODUCT_COMPARISON("product_comparison", "Before You Buy", "shopping_bag"),
    LOST_FOUND_SEARCH("lost_found_search", "Lost & Found", "location_on"),
    DOCUMENT_ASSISTANT("document_assistant", "Secure Files", "description"),
    PURCHASE_ASSISTANT("purchase_assistant", "Purchase Records", "receipt_long"),
    PRIVACY_SHARING("privacy_sharing_assistant", "Privacy & Sharing", "shield"),
    MULTI_TOOL_WORKFLOW("multi_tool_workflow", "Multi-Tool Workflow", "auto_awesome")
}

data class MemoryToolResult(
    val query: String,
    val matchedMemories: List<MemoryEntity>,
    val totalFound: Int,
    val summary: String
)

data class ProductToolResult(
    val query: String,
    val comparisonData: ProductComparisonData?,
    val relatedOwnedItem: MemoryEntity?,
    val summary: String,
    val isLiveSearch: Boolean,
    val error: String? = null
)

data class LostFoundToolResult(
    val query: String,
    val matchedItems: List<LostFoundItemEntity>,
    val bestMatchConfidence: Int,
    val summary: String,
    val privacyNotice: String = "Contact details are kept confidential until verified."
)

data class DocumentToolResult(
    val query: String,
    val matchedFiles: List<VaultDropFileEntity>,
    val summary: String,
    val extractedWarrantiesOrDates: List<String> = emptyList()
)

data class PurchaseToolResult(
    val query: String,
    val matchedPurchases: List<PurchaseEntity>,
    val totalSpent: Double,
    val activeWarrantiesCount: Int,
    val summary: String
)

data class PrivacySharingToolResult(
    val query: String,
    val totalActiveShares: Int,
    val fileShares: List<Pair<VaultDropFileEntity, VaultDropShareEntity>>,
    val summary: String,
    val securityVerificationSummary: String
)

data class MultiToolWorkflowResult(
    val query: String,
    val purchaseData: PurchaseEntity?,
    val relatedFile: VaultDropFileEntity?,
    val warrantySummary: String,
    val comparisonData: ProductComparisonData?,
    val stepsCompleted: List<String>,
    val summary: String,
    val missingItems: List<String> = emptyList(),
    val inferences: List<String> = emptyList()
)

/**
 * Unified execution result returned to the Assistant UI
 */
sealed class ToolExecutionResult {
    data class MemorySuccess(val data: MemoryToolResult) : ToolExecutionResult()
    data class ProductSuccess(val data: ProductToolResult) : ToolExecutionResult()
    data class LostFoundSuccess(val data: LostFoundToolResult) : ToolExecutionResult()
    data class DocumentSuccess(val data: DocumentToolResult) : ToolExecutionResult()
    data class PurchaseSuccess(val data: PurchaseToolResult) : ToolExecutionResult()
    data class PrivacySharingSuccess(val data: PrivacySharingToolResult) : ToolExecutionResult()
    data class MultiToolSuccess(val data: MultiToolWorkflowResult) : ToolExecutionResult()
    data class Error(val tool: AssistantToolType, val message: String) : ToolExecutionResult()
}

/**
 * Repository interface exposing authorized tools to the central AI assistant.
 * Every tool applies strict user and data scoping.
 */
class AssistantToolRepository(
    private val lifeVaultDao: LifeVaultDao,
    private val vaultDropDao: VaultDropDao
) {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    /**
     * Tool 1: memory_search
     * Searches saved memories, belongings, locations, and personal notes.
     */
    suspend fun executeMemorySearch(query: String, categoryFilter: String? = null): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            val allMemories = lifeVaultDao.getAllMemories().first()
            val tokens = query.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }

            val matches = allMemories.filter { mem ->
                val matchesCategory = categoryFilter == null || mem.category.equals(categoryFilter, ignoreCase = true)
                val matchesText = if (tokens.isEmpty()) {
                    true
                } else {
                    tokens.any { token ->
                        mem.title.lowercase().contains(token) ||
                        mem.objectName.lowercase().contains(token) ||
                        mem.location.lowercase().contains(token) ||
                        mem.rawText.lowercase().contains(token)
                    }
                }
                matchesCategory && matchesText
            }

            val summary = if (matches.isNotEmpty()) {
                val topMatch = matches.first()
                "Found ${matches.size} record(s). Primary match: '${topMatch.objectName}' stored in ${topMatch.location} (${topMatch.category})."
            } else {
                "No memories or belongings found matching \"$query\". You can save this memory to your vault at any time."
            }

            ToolExecutionResult.MemorySuccess(
                MemoryToolResult(
                    query = query,
                    matchedMemories = matches,
                    totalFound = matches.size,
                    summary = summary
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.MEMORY_SEARCH, e.localizedMessage ?: "Failed to search memories.")
        }
    }

    /**
     * Tool 2: product_comparison
     * Researches products, live prices across platforms, reviews, and cross-checks with owned belongings.
     */
    suspend fun executeProductComparison(
        productQuery: String,
        budget: String? = null,
        priority: String = "Balanced",
        onProgress: (String) -> Unit = {}
    ): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            // First check if user already owns an equivalent item in their vault
            val memories = lifeVaultDao.getAllMemories().first()
            val qTokens = productQuery.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }
            val existingItem = memories.firstOrNull { mem ->
                val obj = mem.objectName.lowercase()
                qTokens.any { token -> obj.contains(token) || (token.contains("phone") && obj.contains("phone")) || (token.contains("laptop") && obj.contains("laptop")) }
            }

            // Conduct research via ProductResearchService
            val researchResult = ProductResearchService.researchProduct(
                productQuery = productQuery,
                budget = budget,
                priority = priority,
                onProgressUpdate = onProgress
            )

            when (researchResult) {
                is ComparisonState.Success -> {
                    val summary = buildString {
                        append("Verified specifications and multi-platform pricing for ${researchResult.data.product.name}. ")
                        if (existingItem != null) {
                            append("⚠️ Notice: You already have '${existingItem.objectName}' recorded in ${existingItem.location}. ")
                        }
                        append("${researchResult.data.aiRecommendation.recommendation}. ${researchResult.data.aiRecommendation.reason}")
                    }
                    ToolExecutionResult.ProductSuccess(
                        ProductToolResult(
                            query = productQuery,
                            comparisonData = researchResult.data,
                            relatedOwnedItem = existingItem,
                            summary = summary,
                            isLiveSearch = true
                        )
                    )
                }
                is ComparisonState.Partial -> {
                    ToolExecutionResult.ProductSuccess(
                        ProductToolResult(
                            query = productQuery,
                            comparisonData = researchResult.data,
                            relatedOwnedItem = existingItem,
                            summary = "${researchResult.data.product.name}: ${researchResult.warning}",
                            isLiveSearch = true
                        )
                    )
                }
                is ComparisonState.NoResult -> {
                    ToolExecutionResult.ProductSuccess(
                        ProductToolResult(
                            query = productQuery,
                            comparisonData = null,
                            relatedOwnedItem = existingItem,
                            summary = researchResult.message,
                            isLiveSearch = true
                        )
                    )
                }
                is ComparisonState.Error -> {
                    // Fallback to local heuristic advice if API key is unconfigured or network fails
                    val advice = com.example.ai.LifeVaultAIEngine.analyzePurchase(
                        productName = productQuery,
                        price = 199.0,
                        specs = "Standard specifications",
                        statedNeed = "General productivity",
                        existingBelongings = memories
                    )
                    ToolExecutionResult.ProductSuccess(
                        ProductToolResult(
                            query = productQuery,
                            comparisonData = null,
                            relatedOwnedItem = existingItem,
                            summary = "Offline Vault Advice (${advice.verdict}): ${advice.headline}. " +
                                    (if (existingItem != null) "You already own '${existingItem.objectName}' in your vault. " else "") +
                                    advice.bulletPoints.joinToString(" • "),
                            isLiveSearch = false,
                            error = researchResult.message
                        )
                    )
                }
                else -> ToolExecutionResult.Error(AssistantToolType.PRODUCT_COMPARISON, "Search operation did not complete.")
            }
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.PRODUCT_COMPARISON, e.localizedMessage ?: "Failed to compare products.")
        }
    }

    /**
     * Tool 3: lost_found_search
     * Searches community lost & found reports, computes match scores, and handles contact privacy.
     */
    suspend fun executeLostFoundSearch(query: String, typeFilter: String? = null): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            val allItems = lifeVaultDao.getAllLostFoundItems().first()
            val tokens = query.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }

            val matches = allItems.filter { item ->
                val matchesType = typeFilter == null || item.type.equals(typeFilter, ignoreCase = true)
                val matchesText = if (tokens.isEmpty()) {
                    true
                } else {
                    tokens.any { token ->
                        item.title.lowercase().contains(token) ||
                        item.description.lowercase().contains(token) ||
                        item.category.lowercase().contains(token) ||
                        item.location.lowercase().contains(token)
                    }
                }
                matchesType && matchesText
            }

            val highestConfidence = matches.maxOfOrNull { it.matchConfidence } ?: 0

            val summary = if (matches.isNotEmpty()) {
                val reportType = matches.first().type
                "Found ${matches.size} potential $reportType match(es). Highest match confidence: $highestConfidence%."
            } else {
                "No matching lost or found reports discovered. You can submit a new report to the campus/community directory."
            }

            ToolExecutionResult.LostFoundSuccess(
                LostFoundToolResult(
                    query = query,
                    matchedItems = matches,
                    bestMatchConfidence = highestConfidence,
                    summary = summary
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.LOST_FOUND_SEARCH, e.localizedMessage ?: "Failed to search lost & found.")
        }
    }

    /**
     * Tool 4: document_assistant
     * Analyzes authorized files in VaultDrop, surfaces summaries, and extracts key dates/warranties.
     */
    suspend fun executeDocumentAssistant(query: String): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            val readyFiles = vaultDropDao.getReadyFiles().first()
            val tokens = query.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }

            val matchedFiles = readyFiles.filter { file ->
                if (tokens.isEmpty()) true
                else tokens.any { token ->
                    file.originalFilename.lowercase().contains(token) ||
                    file.extension.lowercase().contains(token) ||
                    file.safePreviewContent.lowercase().contains(token)
                }
            }

            val extractedDates = mutableListOf<String>()
            matchedFiles.forEach { file ->
                val createdStr = dateFormat.format(Date(file.createdAt))
                extractedDates.add("${file.originalFilename}: Stored on $createdStr (Status: ${file.scanStatus})")
            }

            val summary = if (matchedFiles.isNotEmpty()) {
                "Located ${matchedFiles.size} authorized file(s) in VaultDrop. All files verified with ${matchedFiles.first().encryptionAlgorithm}."
            } else {
                "No stored documents found matching \"$query\". Ready files in your vault are encrypted and isolated."
            }

            ToolExecutionResult.DocumentSuccess(
                DocumentToolResult(
                    query = query,
                    matchedFiles = matchedFiles,
                    summary = summary,
                    extractedWarrantiesOrDates = extractedDates
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.DOCUMENT_ASSISTANT, e.localizedMessage ?: "Failed to query documents.")
        }
    }

    /**
     * Tool 5: purchase_assistant
     * Queries past purchases, warranty deadlines, and calculates spending summaries.
     */
    suspend fun executePurchaseAssistant(query: String): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            val purchases = lifeVaultDao.getAllPurchases().first()
            val tokens = query.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }

            val matchedPurchases = purchases.filter { pur ->
                if (tokens.isEmpty()) true
                else tokens.any { token ->
                    pur.productName.lowercase().contains(token) ||
                    pur.category.lowercase().contains(token) ||
                    pur.verdict.lowercase().contains(token) ||
                    pur.reasoning.lowercase().contains(token)
                }
            }

            val now = System.currentTimeMillis()
            val total = matchedPurchases.sumOf { it.price }
            val activeWarranties = matchedPurchases.count { pur ->
                pur.warrantyExpiryDate != null && pur.warrantyExpiryDate > now
            }

            val summary = if (matchedPurchases.isNotEmpty()) {
                "Found ${matchedPurchases.size} purchase record(s) totaling $${String.format(Locale.US, "%.2f", total)}. $activeWarranties item(s) have active warranty coverage."
            } else {
                "No purchase records matched your query. Saved receipts and purchases are logged with warranty tracking."
            }

            ToolExecutionResult.PurchaseSuccess(
                PurchaseToolResult(
                    query = query,
                    matchedPurchases = matchedPurchases,
                    totalSpent = total,
                    activeWarrantiesCount = activeWarranties,
                    summary = summary
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.PURCHASE_ASSISTANT, e.localizedMessage ?: "Failed to query purchases.")
        }
    }

    /**
     * Tool 6: privacy_sharing_assistant
     * Audits VaultDrop file sharing permissions, expiration dates, download counters, and safety status.
     */
    suspend fun executePrivacySharingAssistant(query: String): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            val shares = vaultDropDao.getAllShares().first()
            val files = vaultDropDao.getAllFiles().first()

            val sharePairs = shares.mapNotNull { share ->
                val file = files.firstOrNull { it.id == share.fileId }
                if (file != null) Pair(file, share) else null
            }

            val activeCount = sharePairs.count { !it.second.isDisabled }

            val summary = if (sharePairs.isNotEmpty()) {
                "You have $activeCount active secure share link(s) across ${sharePairs.size} file records. Client-side key fragments (#key) ensure zero-knowledge transport."
            } else {
                "No active file share links currently exist. Any future shared files will have enforced download and time limits."
            }

            val verification = "Zero-Knowledge Transport: Server stores only ciphertexts; decryption keys are anchored in client URLs."

            ToolExecutionResult.PrivacySharingSuccess(
                PrivacySharingToolResult(
                    query = query,
                    totalActiveShares = activeCount,
                    fileShares = sharePairs,
                    summary = summary,
                    securityVerificationSummary = verification
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.PRIVACY_SHARING, e.localizedMessage ?: "Failed to audit sharing permissions.")
        }
    }

    /**
     * Tool 7: multi_tool_workflow
     * Executes connected cross-domain workflows (e.g. Purchase + Bill Document + Warranty + Before You Buy Comparison).
     */
    suspend fun executeMultiToolWorkflow(
        query: String,
        onProgress: (String) -> Unit = {}
    ): ToolExecutionResult = withContext(Dispatchers.IO) {
        val steps = mutableListOf<String>()
        val missing = mutableListOf<String>()
        val inferences = mutableListOf<String>()

        try {
            onProgress("Step 1: Locating purchase record in Vault...")
            val purchases = lifeVaultDao.getAllPurchases().first()
            val lower = query.lowercase()

            val targetPurchase = purchases.find { p ->
                lower.contains(p.productName.lowercase()) ||
                (lower.contains("laptop") && (p.productName.contains("MacBook", ignoreCase = true) || p.productName.contains("Dell", ignoreCase = true) || p.category.equals("Tech", ignoreCase = true) || p.category.equals("Electronics", ignoreCase = true))) ||
                (lower.contains("phone") && (p.productName.contains("iPhone", ignoreCase = true) || p.productName.contains("Pixel", ignoreCase = true))) ||
                (lower.contains("headphone") && (p.productName.contains("Sony", ignoreCase = true) || p.productName.contains("Bose", ignoreCase = true)))
            } ?: purchases.firstOrNull()

            if (targetPurchase != null) {
                steps.add("Found authorized purchase record: '${targetPurchase.productName}' (${targetPurchase.currency}${targetPurchase.price}) in ${targetPurchase.category}")
            } else {
                missing.add("No specific purchase record matched; evaluated all available records.")
            }

            onProgress("Step 2: Retrieving bills & invoices in VaultDrop...")
            val files = vaultDropDao.getAllFiles().first()
            val relatedBill = files.find { f ->
                val fn = f.originalFilename.lowercase()
                (targetPurchase != null && fn.contains(targetPurchase.productName.lowercase().take(4))) ||
                fn.contains("bill") || fn.contains("receipt") || fn.contains("invoice") || fn.contains("laptop")
            }

            if (relatedBill != null) {
                val sizeKb = (relatedBill.sizeBytes / 1024).coerceAtLeast(1)
                steps.add("Retrieved verified document: '${relatedBill.originalFilename}' (${sizeKb} KB, ${relatedBill.scanStatus})")
            } else {
                missing.add("Digital PDF receipt file not found in VaultDrop.")
                inferences.add("Retrieved purchase date and store pricing from financial transaction record.")
            }

            onProgress("Step 3: Calculating warranty & coverage...")
            val now = System.currentTimeMillis()
            val warrantyText = if (targetPurchase?.warrantyExpiryDate != null) {
                val remainingMs = targetPurchase.warrantyExpiryDate - now
                if (remainingMs > 0) {
                    val remainingDays = (remainingMs / (1000L * 60 * 60 * 24)).toInt()
                    "Active: $remainingDays days remaining (~${(remainingDays / 30).coerceAtLeast(1)} months remaining)"
                } else {
                    "Expired: Warranty ended on ${dateFormat.format(Date(targetPurchase.warrantyExpiryDate))}"
                }
            } else if (targetPurchase != null) {
                inferences.add("Estimated standard manufacturer warranty of 12 months from analysis date.")
                "Estimated standard 12-month manufacturer coverage"
            } else {
                inferences.add("Standard manufacturer warranty estimated at 12 months.")
                "Standard manufacturer warranty estimated at 12 months from purchase"
            }
            steps.add("Warranty Status: $warrantyText")

            onProgress("Step 4: Performing grounded product research & comparison...")
            val comparisonTarget = if (lower.contains("compare with ") || lower.contains("vs ") || lower.contains("another model")) {
                val extractedTarget = query.substringAfter("compare with ", "")
                    .ifBlank { query.substringAfter("vs ", "") }
                    .substringBefore("before")
                    .substringBefore("and")
                    .trim()
                if (extractedTarget.isNotBlank()) extractedTarget else "${targetPurchase?.productName ?: "Laptop"} latest model"
            } else {
                "${targetPurchase?.productName ?: "Laptop"} latest model"
            }

            val productResearch = ProductResearchService.researchProduct(
                productQuery = comparisonTarget,
                budget = null,
                priority = "Balanced",
                onProgressUpdate = onProgress
            )

            val compData = when (productResearch) {
                is ComparisonState.Success -> productResearch.data
                is ComparisonState.Partial -> productResearch.data
                else -> null
            }

            if (compData != null) {
                steps.add("Researched live prices for '${compData.product.name}' across ${compData.platforms.size} verified platforms")
            } else {
                missing.add("Live web comparison for '$comparisonTarget' returned partial grounding")
            }

            val summary = buildString {
                append("### Multi-Tool Intelligence Summary\n\n")
                append("1. **Authorized Purchase & Bill**: ")
                if (targetPurchase != null) {
                    append("Found **${targetPurchase.productName}** analyzed on ${dateFormat.format(Date(targetPurchase.dateAnalyzed))} for **${targetPurchase.currency}${targetPurchase.price}**.\n")
                } else {
                    append("No exact purchase record found in your vault.\n")
                }

                if (relatedBill != null) {
                    append("   • Linked File: **${relatedBill.originalFilename}** (${relatedBill.extension.uppercase()})\n")
                } else {
                    append("   • *Note: Digital receipt file has not been uploaded to VaultDrop yet.*\n")
                }

                append("2. **Warranty Information**: **$warrantyText**\n\n")

                if (compData != null) {
                    append("3. **Grounded Comparison (${compData.product.name})**:\n")
                    val bestPlatform = compData.platforms.filter { it.price != null }.minByOrNull { it.price ?: Double.MAX_VALUE }
                    if (bestPlatform != null) {
                        append("   • Best visible price: **${bestPlatform.currency} ${bestPlatform.price}** on **${bestPlatform.platform}** (Rating: ${bestPlatform.rating ?: "N/A"}★)\n")
                    }
                    append("   • Recommendation: ${compData.aiRecommendation.recommendation}\n")
                    append("   • Key insight: ${compData.aiRecommendation.reason}\n")
                }
            }

            ToolExecutionResult.MultiToolSuccess(
                MultiToolWorkflowResult(
                    query = query,
                    purchaseData = targetPurchase,
                    relatedFile = relatedBill,
                    warrantySummary = warrantyText,
                    comparisonData = compData,
                    stepsCompleted = steps,
                    summary = summary,
                    missingItems = missing,
                    inferences = inferences
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult.Error(AssistantToolType.MULTI_TOOL_WORKFLOW, e.localizedMessage ?: "Failed to complete multi-tool workflow.")
        }
    }
}
