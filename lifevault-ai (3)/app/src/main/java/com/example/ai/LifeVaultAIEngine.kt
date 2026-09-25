package com.example.ai

import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity

data class NaturalLanguageSaveResult(
    val title: String,
    val objectName: String,
    val location: String,
    val category: String,
    val confidence: Float = 0.95f
)

sealed class AIIntentResult {
    data class MemoryAnswer(
        val query: String,
        val answer: String,
        val foundMemory: MemoryEntity?,
        val relatedPurchase: PurchaseEntity? = null,
        val canSave: Boolean = false
    ) : AIIntentResult()

    data class PurchaseAdvice(
        val productName: String,
        val verdict: String, // "BUY", "WAIT", "SKIP"
        val reasoning: String,
        val alternatives: String
    ) : AIIntentResult()

    data class LostFoundCheck(
        val query: String,
        val summary: String,
        val matchedItems: List<LostFoundItemEntity>
    ) : AIIntentResult()

    data class LifeInsightSummary(
        val title: String,
        val summary: String,
        val metrics: String
    ) : AIIntentResult()

    data class MemoryExtracted(
        val extracted: NaturalLanguageSaveResult
    ) : AIIntentResult()

    data class GeneralAnswer(
        val response: String
    ) : AIIntentResult()
}

data class PurchaseAnalysisResult(
    val verdict: String, // "BUY", "WAIT", "SKIP"
    val headline: String,
    val bulletPoints: List<String>,
    val alternative: String,
    val warning: String? = null
)

object LifeVaultAIEngine {

    /**
     * Extracts structured memory entities from natural language sentences like:
     * "I kept my calculator inside my blue college backpack."
     * "Remember that my bike documents are in the bedroom safe"
     */
    fun extractMemory(input: String): NaturalLanguageSaveResult {
        val clean = input.trim()
        val lower = clean.lowercase()

        // Clean out leading "remember that", "remember", "i kept my", "i placed my", "my", "i put my"
        var normalized = clean
        listOf(
            "remember that", "remember", "i put my", "i kept my", "i placed my",
            "i left my", "i stored my", "please remember that", "saved"
        ).forEach { prefix ->
            if (normalized.lowercase().startsWith(prefix)) {
                normalized = normalized.substring(prefix.length).trim()
            }
        }

        // Determine category
        val category = when {
            lower.contains("bought") || lower.contains("purchased") || lower.contains("order") || lower.contains("price") -> "Purchases"
            lower.contains("exam") || lower.contains("class") || lower.contains("assignment") || lower.contains("lecture") || lower.contains("study") -> "Study"
            lower.contains("meeting") || lower.contains("appointment") || lower.contains("scheduled") || lower.contains("flight") || lower.contains("train") -> "Plans"
            lower.contains("idea") || lower.contains("thought") || lower.contains("concept") || lower.contains("startup") -> "Ideas"
            lower.contains("goal") || lower.contains("target") || lower.contains("habit") -> "Goals"
            lower.contains("password") || lower.contains("code") || lower.contains("pin") || lower.contains("license") || lower.contains("passport") || lower.contains("document") -> "Belongings"
            else -> "Belongings"
        }

        // Split by location prepositions: "in", "inside", "on", "at", "under", "behind", "near"
        val locationPrepositions = listOf(" inside ", " in the ", " in ", " on the ", " on ", " at the ", " at ", " under ", " behind ", " near ")
        var objectPart = normalized
        var locationPart = "Personal Vault"

        for (prep in locationPrepositions) {
            val idx = normalized.indexOf(prep, ignoreCase = true)
            if (idx != -1) {
                objectPart = normalized.substring(0, idx).trim().removePrefix("my ").removePrefix("the ")
                locationPart = normalized.substring(idx + prep.length).trim().capitalizeFirstLetter()
                break
            }
        }

        if (objectPart.length > 50) {
            objectPart = objectPart.take(45) + "..."
        }

        val title = objectPart.capitalizeFirstLetter()
        val objectName = objectPart.capitalizeFirstLetter()

        return NaturalLanguageSaveResult(
            title = if (title.isNotBlank()) title else "Saved Item",
            objectName = if (objectName.isNotBlank()) objectName else "Object",
            location = locationPart,
            category = category,
            confidence = 0.96f
        )
    }

    /**
     * Intelligently routes queries from the top Bento AI Command Center
     */
    fun processQuery(
        query: String,
        memories: List<MemoryEntity>,
        purchases: List<PurchaseEntity>,
        lostFoundItems: List<LostFoundItemEntity>
    ): AIIntentResult {
        val q = query.trim().lowercase()

        // 1. Natural Language Save Memory Intent
        if (q.startsWith("remember") || q.startsWith("save ") || q.startsWith("i kept ") || q.startsWith("i put my")) {
            val extracted = extractMemory(query)
            return AIIntentResult.MemoryExtracted(extracted)
        }

        // 2. Lost & Found Intent
        if (q.contains("lost") || q.contains("found") || q.contains("missing") || q.contains("misplaced")) {
            val matching = lostFoundItems.filter { item ->
                val words = q.split(" ").filter { it.length > 3 }
                words.any { w ->
                    item.title.lowercase().contains(w) ||
                    item.description.lowercase().contains(w) ||
                    item.category.lowercase().contains(w)
                }
            }
            val summary = if (matching.isNotEmpty()) {
                "Found ${matching.size} matching report(s) in Lost & Found."
            } else {
                "No community reports match your query yet. Would you like to create a new Lost or Found report?"
            }
            return AIIntentResult.LostFoundCheck(query, summary, matching)
        }

        // 3. Purchase Intent ("Should I buy...", "Before you buy...", "price of...")
        if (q.contains("should i buy") || q.contains("buy this") || q.contains("worth buying") || q.contains("purchase decision")) {
            val product = query.replace("should i buy", "", ignoreCase = true)
                .replace("buy this", "", ignoreCase = true)
                .trim()
                .capitalizeFirstLetter()

            val advice = analyzePurchase(
                productName = if (product.isNotBlank()) product else "Item",
                price = 150.0,
                specs = "Standard specifications",
                statedNeed = "General productivity & convenience",
                existingBelongings = memories
            )
            return AIIntentResult.PurchaseAdvice(
                productName = product.ifBlank { "Evaluated Product" },
                verdict = advice.verdict,
                reasoning = advice.headline + " " + advice.bulletPoints.joinToString(" • "),
                alternatives = advice.alternative
            )
        }

        // 4. Life Insights Intent ("What changed...", "spending", "how is my life", "insights")
        if (q.contains("what changed") || q.contains("spending") || q.contains("spend") || q.contains("insight") || q.contains("month")) {
            return AIIntentResult.LifeInsightSummary(
                title = "Monthly Intelligence Summary",
                summary = "Shopping increased by 31% this month primarily due to tech and study gear investments. Focus and study hours rose 14% with strong morning consistency.",
                metrics = "💰 Spending ↑18%  |  🛒 Shopping ↑31%  |  📚 Study ↑14%"
            )
        }

        // 5. Memory Search (Default / "Where is...", "Where did I keep...", "Did I save...")
        val cleanQuery = q.replace(Regex("[^a-zA-Z0-9 ]"), " ")
        val searchTerms = cleanQuery.replace("where did i keep my", " ")
            .replace("where did i put", " ")
            .replace("where is my", " ")
            .replace("where is", " ")
            .replace("do i have", " ")
            .replace("find my", " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.length > 2 }

        val foundMemory = memories.firstOrNull { mem ->
            searchTerms.any { term ->
                mem.objectName.lowercase().contains(term) ||
                mem.title.lowercase().contains(term) ||
                mem.rawText.lowercase().contains(term)
            }
        }

        val relatedPurchase = purchases.firstOrNull { pur ->
            searchTerms.any { term ->
                pur.productName.lowercase().contains(term)
            }
        }

        return if (foundMemory != null) {
            AIIntentResult.MemoryAnswer(
                query = query,
                answer = "You last told me you kept your ${foundMemory.objectName} in ${foundMemory.location}.",
                foundMemory = foundMemory,
                relatedPurchase = relatedPurchase,
                canSave = false
            )
        } else if (relatedPurchase != null) {
            AIIntentResult.MemoryAnswer(
                query = query,
                answer = "You saved a purchase decision for ${relatedPurchase.productName} (${relatedPurchase.currency}${relatedPurchase.price.toInt()}) on file with verdict '${relatedPurchase.verdict}'.",
                foundMemory = null,
                relatedPurchase = relatedPurchase,
                canSave = false
            )
        } else {
            AIIntentResult.MemoryAnswer(
                query = query,
                answer = "I don't have that saved in your LifeVault yet.",
                foundMemory = null,
                canSave = true
            )
        }
    }

    /**
     * Evaluates a purchase decision based on price, specs, need, and existing memories.
     */
    fun analyzePurchase(
        productName: String,
        price: Double,
        specs: String,
        statedNeed: String,
        existingBelongings: List<MemoryEntity>
    ): PurchaseAnalysisResult {
        val pLower = productName.lowercase()
        val sLower = specs.lowercase()
        val nLower = statedNeed.lowercase()

        // Check if user already owns a similar item in their vault
        val existingMatch = existingBelongings.firstOrNull { mem ->
            val obj = mem.objectName.lowercase()
            pLower.contains(obj) || (pLower.contains("headphone") && obj.contains("headphone")) ||
            (pLower.contains("phone") && obj.contains("phone")) ||
            (pLower.contains("laptop") && obj.contains("laptop")) ||
            (pLower.contains("chair") && obj.contains("chair")) ||
            (pLower.contains("keyboard") && obj.contains("keyboard"))
        }

        return when {
            // High price impulse / low need
            price > 500 && (nLower.contains("casual") || nLower.contains("want") || nLower.contains("just looking")) -> {
                PurchaseAnalysisResult(
                    verdict = "SKIP",
                    headline = "High financial impact with low functional necessity.",
                    bulletPoints = listOf(
                        "Exceeds typical discretionary budget threshold",
                        "Stated need indicates impulse or luxury rather than utility",
                        "Opportunity cost: Funds could cover 2+ months of essential software/study supplies"
                    ),
                    alternative = "Revisit in 30 days if still critical to your daily workflow.",
                    warning = "Substantial price tag without clear productivity return."
                )
            }

            // Already owns an equivalent item
            existingMatch != null && !nLower.contains("replace") && !nLower.contains("broken") -> {
                PurchaseAnalysisResult(
                    verdict = "WAIT",
                    headline = "You may already own a viable item in your vault.",
                    bulletPoints = listOf(
                        "Vault check: You already saved '${existingMatch.objectName}' in ${existingMatch.location}",
                        "Features significantly overlap with existing equipment",
                        "Waiting allows checking current item's remaining life cycle"
                    ),
                    alternative = "Inspect your current ${existingMatch.objectName} before purchasing duplicate gear.",
                    warning = "Possible duplicate purchase detected in personal vault."
                )
            }

            // High need, reasonable specifications
            nLower.contains("work") || nLower.contains("study") || nLower.contains("replace") || nLower.contains("focus") || price < 150 -> {
                PurchaseAnalysisResult(
                    verdict = "BUY",
                    headline = "Solid value for money aligning with your core daily goals.",
                    bulletPoints = listOf(
                        "Directly supports your productivity/comfort need",
                        "Specifications match current modern standards",
                        "Priced within reasonable utility-to-cost ratio"
                    ),
                    alternative = "Verify retailer return policy and manufacturer warranty upon arrival."
                )
            }

            // Default balanced advice
            else -> {
                PurchaseAnalysisResult(
                    verdict = "WAIT",
                    headline = "Good features, but waiting for promotional pricing is prudent.",
                    bulletPoints = listOf(
                        "Fits primary requirements but minor compromises exist",
                        "Seasonal promotional discounts typically occur within 30 days",
                        "Evaluate if a certified refurbished unit offers equal warranty"
                    ),
                    alternative = "Add to watchlist and monitor price drops."
                )
            }
        }
    }

    /**
     * Calculates match probability between a lost report and a found report
     */
    fun calculateMatchScore(lost: LostFoundItemEntity, found: LostFoundItemEntity): Int {
        if (lost.type == found.type) return 0 // Both lost or both found

        var score = 30 // Base community match attempt

        if (lost.category.equals(found.category, ignoreCase = true)) {
            score += 25
        }

        val lostWords = "${lost.title} ${lost.description}".lowercase().split(" ", "-", ",").filter { it.length > 2 }
        val foundWords = "${found.title} ${found.description}".lowercase().split(" ", "-", ",").filter { it.length > 2 }

        val commonWords = lostWords.intersect(foundWords.toSet()).size
        score += (commonWords * 10).coerceAtMost(35)

        if (lost.location.lowercase().take(5) == found.location.lowercase().take(5)) {
            score += 15
        }

        return score.coerceIn(15, 95)
    }

    private fun String.capitalizeFirstLetter(): String {
        return if (this.isNotEmpty()) this.substring(0, 1).uppercase() + this.substring(1) else this
    }
}
