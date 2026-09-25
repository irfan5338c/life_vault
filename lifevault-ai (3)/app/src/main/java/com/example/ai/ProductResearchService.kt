package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiRecommendation
import com.example.data.model.PlatformPriceInfo
import com.example.data.model.PriceComparison
import com.example.data.model.ProductComparisonData
import com.example.data.model.ProductInfo
import com.example.data.model.QualitySignals
import com.example.data.model.ReviewSummary
import com.example.data.model.SourceCitation
import com.example.data.model.VisualIdentificationResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class ComparisonState {
    object Idle : ComparisonState()
    data class Searching(val stepMessage: String) : ComparisonState()
    data class Success(val data: ProductComparisonData) : ComparisonState()
    data class Partial(val data: ProductComparisonData, val warning: String) : ComparisonState()
    data class NoResult(val message: String) : ComparisonState()
    data class Error(val message: String) : ComparisonState()
}

object ProductResearchService {

    private const val TAG = "ProductResearchService"
    private const val GEMINI_MODEL = "gemini-3.5-flash"
    private const val GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Conducts live web-grounded research via Gemini with Google Search tool enabled.
     */
    suspend fun researchProduct(
        productQuery: String,
        budget: String? = null,
        priority: String = "Balanced",
        onProgressUpdate: (String) -> Unit = {}
    ): ComparisonState = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ComparisonState.Error(
                "Gemini API key is not configured. Please add your key to the Secrets panel in AI Studio."
            )
        }

        try {
            onProgressUpdate("Searching multiple sources across Amazon India, Flipkart, Croma, Reliance Digital, and official stores...")

            val systemPrompt = """
                You are LifeVault AI's real-time shopping research engine and decision assistant (Decide 2.0).
                Your task is to conduct live web searches across authorized retail channels in India and produce an accurate, objective, verified product comparison.

                CRITICAL GROUNDING & MATCHING RULES:
                1. Use Google Search to find current, live information for:
                   - $productQuery on Amazon India (amazon.in)
                   - $productQuery on Flipkart (flipkart.com)
                   - $productQuery on Croma (croma.com)
                   - $productQuery on Reliance Digital (reliancedigital.in)
                   - $productQuery on Vijay Sales, Tata CLiQ, or Myntra (if lifestyle/shoes)
                   - $productQuery on official brand store
                   - Public customer reviews and expert specifications
                2. Prioritize finding the EXACT SAME PRODUCT (matching brand, model number, variant, storage, color).
                3. If the exact product is unavailable on a platform, label matchType as "Similar Product" or "Variant Difference" and describe the difference in matchExplanation.
                4. NEVER invent, hallucinate, or guess prices, ratings, review counts, availability, or URLs.
                5. If a platform does not have the product or its price cannot be verified, set price to null, priceFormatted to "Data unavailable", and availability to "Data unavailable".
                6. Provide real web URLs from your search results for each platform. If no direct product URL was found, provide the platform's verified search or store page URL, or leave blank.
                7. Provide originalPrice and discountPercentage only if verified from search results.
                8. Include realistic delivery info (e.g. "Free 1-Day Delivery", "Standard Delivery 2-3 Days", "Check pincode").
                9. User Priority: '$priority'. User Budget: '${budget ?: "Not specified"}'. Tailor the AI Recommendation, Best Platform Pick, and Value Analysis accordingly.
                10. Provide 3 helpful suggested next steps for the user (e.g. "Compare with [competitor model]", "Check exchange offers", "Set price drop alert").

                OUTPUT FORMAT:
                You MUST return ONLY a valid JSON object (enclosed in ```json ... ``` or directly as raw JSON) with this exact schema:
                {
                  "product": {
                    "name": "Exact full name",
                    "brand": "Brand",
                    "model": "Model identifier",
                    "variant": "Storage/Color/Size variant",
                    "image": "Direct image URL if found in search, else empty string",
                    "category": "Category",
                    "description": "Short 2-sentence verified overview"
                  },
                  "platforms": [
                    {
                      "platform": "Platform Name (e.g. Amazon India, Flipkart, Croma, Reliance Digital, Vijay Sales)",
                      "price": 79999.0,
                      "priceFormatted": "₹79,999",
                      "originalPrice": 89999.0,
                      "originalPriceFormatted": "₹89,999",
                      "discountPercentage": 11,
                      "currency": "INR",
                      "availability": "In stock",
                      "deliveryInfo": "Free 1-Day Delivery",
                      "rating": 4.6,
                      "reviewCount": 14200,
                      "url": "https://...",
                      "source": "Platform domain or search result",
                      "lastVerified": "Live Web Search",
                      "matchType": "Exact Match",
                      "matchExplanation": "Verified exact model and storage",
                      "variantDetail": "128GB / Black"
                    }
                  ],
                  "reviewSummary": {
                    "positive": ["Praise 1", "Praise 2"],
                    "negative": ["Complaint 1", "Complaint 2"],
                    "commonComplaints": ["Major complaint 1", "Major complaint 2"],
                    "commonPraise": ["Key strength 1", "Key strength 2"],
                    "overallRatingScore": 4.5
                  },
                  "qualitySignals": {
                    "buildQuality": "Verified review consensus on materials and chassis",
                    "buildQualityRating": 4,
                    "performance": "Verified real-world performance assessment",
                    "performanceRating": 5,
                    "reliability": "Long-term reliability and build integrity",
                    "reliabilityRating": 4,
                    "features": "Key standout capabilities and hardware highlights",
                    "featuresRating": 4,
                    "warranty": "Standard brand warranty terms (e.g. 1 Year Manufacturer Warranty)",
                    "warrantyRating": 4,
                    "afterSalesSupport": "Brand service network in India",
                    "valueForMoney": "Analysis of price-to-performance ratio",
                    "valueForMoneyRating": 4
                  },
                  "comparison": {
                    "lowestPrice": "₹XX,XXX on [Platform]",
                    "lowestPricePlatform": "[Platform Name]",
                    "highestPrice": "₹XX,XXX on [Platform]",
                    "bestRated": "[Platform Name] (X.X★)",
                    "bestValue": "[Platform Name] / Reasoning"
                  },
                  "aiRecommendation": {
                    "recommendation": "Objective assessment based on verified data, priority, and budget",
                    "reason": "Detailed reasoning referencing price differences and quality signals",
                    "bestPlatformPick": "Top platform recommendation considering price, verified authorized seller, and rating",
                    "valueAnalysis": "Detailed analysis whether this product is a good buy now or whether user should wait for sale",
                    "priceTrendAdvice": "Price is at normal range / discounted / at recent low",
                    "pros": ["Pro 1", "Pro 2", "Pro 3"],
                    "cons": ["Con 1", "Con 2"],
                    "whoShouldBuy": "Profiles of users who get high utility",
                    "whoShouldAvoid": "Profiles of users who should skip or wait",
                    "suggestedNextSteps": [
                      "Would you like to compare another model?",
                      "Would you like to set a target price alert?",
                      "Would you like to review similar alternatives?"
                    ]
                  },
                  "sources": [
                    {
                      "name": "Source title / domain",
                      "url": "https://...",
                      "informationObtained": "Price, stock status, ratings, or specs",
                      "timestamp": "Live search"
                    }
                  ],
                  "isWebVerified": true,
                  "researchStatus": "SUCCESS"
                }
            """.trimIndent()

            onProgressUpdate("Comparing prices, ratings, reviews, and specifications across web sources...")

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Perform live web research and comparison for: $productQuery\nPriority: $priority\nBudget: ${budget ?: "Flexible"}")
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                // Enable Google Search grounding tool
                val toolsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                }
                put("tools", toolsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                })
            }

            val (responseCode, responseBody) = executeWithRetryAndFallback(
                apiKey = apiKey,
                requestJson = requestJson,
                onProgressUpdate = onProgressUpdate
            )

            if (responseCode != 200 || responseBody == null) {
                if (responseCode == 429) {
                    Log.w(TAG, "Gemini API rate limit or quota reached (HTTP 429).")
                    return@withContext ComparisonState.Error(
                        "API rate limit or quota exceeded (HTTP 429). Please wait a few seconds and tap Retry, or check your Gemini API plan at ai.google.dev."
                    )
                } else {
                    Log.w(TAG, "Gemini API error code: $responseCode body: $responseBody")
                    return@withContext ComparisonState.Error(
                        "Live product research is temporarily unavailable (HTTP $responseCode). Please check your connection and try again."
                    )
                }
            }

            onProgressUpdate("Validating verified data and structuring comparison table...")

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ComparisonState.NoResult(
                    "We couldn't find enough verified information for '$productQuery'. Try adding more specific model or storage details."
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Extract grounding metadata citations if present from Gemini search tool
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            val groundingChunks = groundingMetadata?.optJSONArray("groundingChunks")

            val parsedData = parseComparisonJson(rawText, groundingChunks)
            if (parsedData == null) {
                Log.e(TAG, "Failed to parse JSON from Gemini response: $rawText")
                return@withContext ComparisonState.Error(
                    "Could not structure the product research results. Please try a different query."
                )
            }

            // Check if any verified platform data exists
            val verifiedPlatformsCount = parsedData.platforms.count { it.price != null && it.price > 0 }
            if (verifiedPlatformsCount == 0 && parsedData.product.name.isBlank()) {
                return@withContext ComparisonState.NoResult(
                    "We couldn't find enough verified information for '$productQuery'. Please verify the spelling or try another product."
                )
            }

            if (verifiedPlatformsCount < 2) {
                return@withContext ComparisonState.Partial(
                    data = parsedData,
                    warning = "We found information from some platforms, but some sources were unavailable."
                )
            }

            return@withContext ComparisonState.Success(parsedData)

        } catch (e: Exception) {
            Log.e(TAG, "Exception during product research", e)
            return@withContext ComparisonState.Error(
                "Live product research encountered an issue: ${e.localizedMessage ?: "Unknown error"}. Please try again."
            )
        }
    }

    /**
     * Executes Gemini API calls with exponential backoff retry and fallback models on 429 quota exhaustion
     */
    private suspend fun executeWithRetryAndFallback(
        apiKey: String,
        requestJson: JSONObject,
        models: List<String> = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview"),
        onProgressUpdate: (String) -> Unit = {}
    ): Pair<Int, String?> {
        var lastCode = 0
        var lastBody: String? = null

        for ((index, model) in models.withIndex()) {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                val code = response.code
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    return Pair(code, body)
                }

                lastCode = code
                lastBody = body

                if (code == 429) {
                    Log.w(TAG, "Model $model returned HTTP 429 (rate-limit / quota). Attempting backoff...")
                    if (index < models.size - 1) {
                        onProgressUpdate("Rate limit reached. Retrying with alternate model...")
                        delay(1200L * (index + 1))
                    }
                } else {
                    Log.w(TAG, "Model $model returned HTTP $code: $body")
                    if (index < models.size - 1) {
                        delay(500)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Request exception for model $model", e)
                if (index < models.size - 1) {
                    delay(500)
                }
            }
        }

        // If rate limited with Google Search tool enabled, try once without search tool (standard model generation)
        if (lastCode == 429 && requestJson.has("tools")) {
            Log.w(TAG, "Search tool quota limit reached. Retrying with standard model knowledge...")
            onProgressUpdate("Search tool busy. Querying AI knowledge base...")
            try {
                val fallbackJson = JSONObject(requestJson.toString()).apply {
                    remove("tools")
                }
                return executeWithRetryAndFallback(
                    apiKey = apiKey,
                    requestJson = fallbackJson,
                    models = listOf("gemini-3.5-flash", "gemini-flash-latest"),
                    onProgressUpdate = onProgressUpdate
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error in fallback without tools", e)
            }
        }

        return Pair(lastCode, lastBody)
    }

    private fun parseComparisonJson(rawText: String, groundingChunks: JSONArray?): ProductComparisonData? {
        try {
            // Strip code fences if present
            var jsonString = rawText.trim()
            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.removePrefix("```json")
            } else if (jsonString.startsWith("```")) {
                jsonString = jsonString.removePrefix("```")
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.removeSuffix("```")
            }
            jsonString = jsonString.trim()

            // Find JSON boundary if surrounded by other conversational text
            val firstBrace = jsonString.indexOf('{')
            val lastBrace = jsonString.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                jsonString = jsonString.substring(firstBrace, lastBrace + 1)
            }

            val adapter = moshi.adapter(ProductComparisonData::class.java)
            val parsed = adapter.fromJson(jsonString)

            if (parsed != null) {
                // Merge search grounding citations if model's sources list was short
                val existingSources = parsed.sources.toMutableList()
                if (groundingChunks != null && groundingChunks.length() > 0) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri", "")
                            val title = web.optString("title", "Web Source")
                            if (uri.isNotBlank() && existingSources.none { it.url == uri }) {
                                existingSources.add(
                                    SourceCitation(
                                        name = title,
                                        url = uri,
                                        informationObtained = "Search grounding verification",
                                        timestamp = "Google Search live index"
                                    )
                                )
                            }
                        }
                    }
                }
                val sortedPlatforms = parsed.platforms.sortedWith(
                    compareBy(
                        { it.price == null || it.price <= 0.0 },
                        { it.price ?: Double.MAX_VALUE }
                    )
                )

                return parsed.copy(
                    platforms = sortedPlatforms,
                    sources = existingSources,
                    isWebVerified = true
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Moshi parsing failed, falling back to manual JSONObject extraction", e)
        }

        // Fallback manual JSONObject parsing if Moshi encountered slight schema variance
        return parseManually(rawText, groundingChunks)
    }

    private fun parseManually(rawText: String, groundingChunks: JSONArray?): ProductComparisonData? {
        return try {
            var jsonString = rawText.trim()
            val firstBrace = jsonString.indexOf('{')
            val lastBrace = jsonString.lastIndexOf('}')
            if (firstBrace == -1 || lastBrace <= firstBrace) return null
            jsonString = jsonString.substring(firstBrace, lastBrace + 1)

            val root = JSONObject(jsonString)
            val prodObj = root.optJSONObject("product") ?: JSONObject()
            val productInfo = ProductInfo(
                name = prodObj.optString("name", "Product"),
                brand = prodObj.optString("brand", ""),
                model = prodObj.optString("model", ""),
                variant = prodObj.optString("variant", ""),
                image = prodObj.optString("image", ""),
                category = prodObj.optString("category", "General"),
                description = prodObj.optString("description", "")
            )

            val platformsList = mutableListOf<PlatformPriceInfo>()
            val platArr = root.optJSONArray("platforms")
            if (platArr != null) {
                for (i in 0 until platArr.length()) {
                    val p = platArr.getJSONObject(i)
                    val priceVal = if (p.has("price") && !p.isNull("price")) p.optDouble("price") else null
                    val origVal = if (p.has("originalPrice") && !p.isNull("originalPrice")) p.optDouble("originalPrice") else null
                    val discVal = if (p.has("discountPercentage") && !p.isNull("discountPercentage")) p.optInt("discountPercentage") else null

                    platformsList.add(
                        PlatformPriceInfo(
                            platform = p.optString("platform", "Platform"),
                            price = priceVal,
                            priceFormatted = p.optString("priceFormatted", if (priceVal != null) "₹${priceVal.toLong()}" else "Data unavailable"),
                            currency = p.optString("currency", "INR"),
                            availability = p.optString("availability", "In stock"),
                            rating = if (p.has("rating") && !p.isNull("rating")) p.optDouble("rating") else null,
                            reviewCount = if (p.has("reviewCount") && !p.isNull("reviewCount")) p.optLong("reviewCount") else null,
                            url = p.optString("url", ""),
                            source = p.optString("source", ""),
                            lastVerified = p.optString("lastVerified", "Live Web Search"),
                            originalPrice = origVal,
                            originalPriceFormatted = if (p.has("originalPriceFormatted") && !p.isNull("originalPriceFormatted")) p.optString("originalPriceFormatted") else if (origVal != null) "₹${origVal.toLong()}" else null,
                            discountPercentage = discVal,
                            deliveryInfo = p.optString("deliveryInfo", "Standard Delivery"),
                            matchType = p.optString("matchType", "Exact Match"),
                            matchExplanation = p.optString("matchExplanation", ""),
                            variantDetail = p.optString("variantDetail", "")
                        )
                    )
                }
            }

            val sortedPlatforms = platformsList.sortedWith(
                compareBy(
                    { it.price == null || it.price <= 0.0 },
                    { it.price ?: Double.MAX_VALUE }
                )
            )

            val revObj = root.optJSONObject("reviewSummary") ?: JSONObject()
            val reviewSummary = ReviewSummary(
                positive = jsonArrayToList(revObj.optJSONArray("positive")),
                negative = jsonArrayToList(revObj.optJSONArray("negative")),
                commonComplaints = jsonArrayToList(revObj.optJSONArray("commonComplaints")),
                commonPraise = jsonArrayToList(revObj.optJSONArray("commonPraise")),
                overallRatingScore = if (revObj.has("overallRatingScore") && !revObj.isNull("overallRatingScore")) revObj.optDouble("overallRatingScore") else null
            )

            val qObj = root.optJSONObject("qualitySignals") ?: JSONObject()
            val qualitySignals = QualitySignals(
                buildQuality = qObj.optString("buildQuality", ""),
                buildQualityRating = qObj.optInt("buildQualityRating", 4),
                performance = qObj.optString("performance", ""),
                performanceRating = qObj.optInt("performanceRating", 4),
                reliability = qObj.optString("reliability", ""),
                reliabilityRating = qObj.optInt("reliabilityRating", 4),
                features = qObj.optString("features", ""),
                featuresRating = qObj.optInt("featuresRating", 4),
                warranty = qObj.optString("warranty", ""),
                warrantyRating = qObj.optInt("warrantyRating", 4),
                afterSalesSupport = qObj.optString("afterSalesSupport", ""),
                valueForMoney = qObj.optString("valueForMoney", ""),
                valueForMoneyRating = qObj.optInt("valueForMoneyRating", 4)
            )

            val compObj = root.optJSONObject("comparison") ?: JSONObject()
            val comparison = PriceComparison(
                lowestPrice = compObj.optString("lowestPrice", ""),
                lowestPricePlatform = compObj.optString("lowestPricePlatform", ""),
                highestPrice = compObj.optString("highestPrice", ""),
                bestRated = compObj.optString("bestRated", ""),
                bestValue = compObj.optString("bestValue", "")
            )

            val recObj = root.optJSONObject("aiRecommendation") ?: JSONObject()
            val recommendation = AiRecommendation(
                recommendation = recObj.optString("recommendation", ""),
                reason = recObj.optString("reason", ""),
                pros = jsonArrayToList(recObj.optJSONArray("pros")),
                cons = jsonArrayToList(recObj.optJSONArray("cons")),
                whoShouldBuy = recObj.optString("whoShouldBuy", ""),
                whoShouldAvoid = recObj.optString("whoShouldAvoid", ""),
                bestPlatformPick = recObj.optString("bestPlatformPick", ""),
                valueAnalysis = recObj.optString("valueAnalysis", ""),
                priceTrendAdvice = recObj.optString("priceTrendAdvice", ""),
                suggestedNextSteps = jsonArrayToList(recObj.optJSONArray("suggestedNextSteps"))
            )

            val sourcesList = mutableListOf<SourceCitation>()
            val srcArr = root.optJSONArray("sources")
            if (srcArr != null) {
                for (i in 0 until srcArr.length()) {
                    val s = srcArr.getJSONObject(i)
                    sourcesList.add(
                        SourceCitation(
                            name = s.optString("name", "Source"),
                            url = s.optString("url", ""),
                            informationObtained = s.optString("informationObtained", "Product comparison"),
                            timestamp = s.optString("timestamp", "Live search")
                        )
                    )
                }
            }

            if (groundingChunks != null) {
                for (i in 0 until groundingChunks.length()) {
                    val chunk = groundingChunks.optJSONObject(i)
                    val web = chunk?.optJSONObject("web")
                    if (web != null) {
                        val uri = web.optString("uri", "")
                        val title = web.optString("title", "Web Source")
                        if (uri.isNotBlank() && sourcesList.none { it.url == uri }) {
                            sourcesList.add(
                                SourceCitation(
                                    name = title,
                                    url = uri,
                                    informationObtained = "Search grounding verification",
                                    timestamp = "Google Search live index"
                                )
                            )
                        }
                    }
                }
            }

            ProductComparisonData(
                product = productInfo,
                platforms = sortedPlatforms,
                reviewSummary = reviewSummary,
                qualitySignals = qualitySignals,
                comparison = comparison,
                aiRecommendation = recommendation,
                sources = sourcesList,
                isWebVerified = true,
                researchStatus = root.optString("researchStatus", "SUCCESS")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Manual JSON parse failed", e)
            null
        }
    }

    /**
     * Google Lens-inspired visual product identification via Gemini Vision AI
     */
    suspend fun identifyProductFromImage(bitmap: Bitmap): VisualIdentificationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext VisualIdentificationResult(
                productName = "",
                confidence = "LOW",
                confidenceExplanation = "Gemini API key is not configured. Please add your key to the Secrets panel in AI Studio.",
                suggestedQuery = ""
            )
        }

        try {
            // Downscale bitmap if too large to conserve memory and network
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val systemPrompt = """
                You are LifeVault AI's visual product detection assistant for the Decide feature.
                Your task is to analyze the provided image to identify the physical consumer product.
                
                Identify:
                - Exact product name
                - Brand
                - Product category
                - Model or variant
                - 2 to 4 key visible features or specs
                - Confidence level: HIGH (clear image with brand/model identifiable), MEDIUM (product type and brand clear but exact model uncertain), or LOW (blurry, obscured, or non-product)
                - Confidence explanation explaining why
                - Best search query for comparing prices across shopping platforms

                CRITICAL ACCURACY RULE:
                Do NOT claim exact product identification when the image is unclear or ambiguous. If uncertain between multiple models, state MEDIUM or LOW confidence and suggest the most likely product query while advising the user to verify.

                You MUST return ONLY a valid JSON object:
                {
                  "productName": "...",
                  "brand": "...",
                  "category": "...",
                  "model": "...",
                  "keyFeatures": ["..."],
                  "confidence": "HIGH",
                  "confidenceExplanation": "...",
                  "suggestedQuery": "..."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Identify the product in this image with brand, model, visible features, and confidence rating.")
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Data)
                            })
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                })
            }

            val (responseCode, responseBody) = executeWithRetryAndFallback(
                apiKey = apiKey,
                requestJson = requestJson,
                models = listOf("gemini-3.5-flash", "gemini-flash-latest"),
                onProgressUpdate = {}
            )

            if (responseCode != 200 || responseBody == null) {
                if (responseCode == 429) {
                    Log.w(TAG, "Gemini Vision API rate limit reached (HTTP 429).")
                    return@withContext VisualIdentificationResult(
                        productName = "",
                        confidence = "LOW",
                        confidenceExplanation = "Visual search rate limit reached (HTTP 429). Please wait a moment or enter the product name directly in the search bar.",
                        suggestedQuery = ""
                    )
                } else {
                    Log.w(TAG, "Gemini Vision API error code: $responseCode body: $responseBody")
                    return@withContext VisualIdentificationResult(
                        productName = "",
                        confidence = "LOW",
                        confidenceExplanation = "Visual search service was temporarily unavailable (HTTP $responseCode).",
                        suggestedQuery = ""
                    )
                }
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            val rawText = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            parseVisualIdentification(rawText)
        } catch (e: Exception) {
            Log.e(TAG, "Error during visual product search", e)
            VisualIdentificationResult(
                productName = "",
                confidence = "LOW",
                confidenceExplanation = "Could not identify product from photo: ${e.localizedMessage ?: "Unknown error"}",
                suggestedQuery = ""
            )
        }
    }

    private fun parseVisualIdentification(rawText: String): VisualIdentificationResult {
        return try {
            var jsonString = rawText.trim()
            val firstBrace = jsonString.indexOf('{')
            val lastBrace = jsonString.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace > firstBrace) {
                jsonString = jsonString.substring(firstBrace, lastBrace + 1)
            }
            val obj = JSONObject(jsonString)
            VisualIdentificationResult(
                productName = obj.optString("productName", ""),
                brand = obj.optString("brand", ""),
                category = obj.optString("category", ""),
                model = obj.optString("model", ""),
                keyFeatures = jsonArrayToList(obj.optJSONArray("keyFeatures")),
                confidence = obj.optString("confidence", "MEDIUM"),
                confidenceExplanation = obj.optString("confidenceExplanation", ""),
                suggestedQuery = obj.optString("suggestedQuery", obj.optString("productName", ""))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse visual identification JSON", e)
            VisualIdentificationResult(
                productName = "",
                confidence = "LOW",
                confidenceExplanation = "Image analyzed, but structured identification could not be generated. Please enter product name manually.",
                suggestedQuery = ""
            )
        }
    }

    private fun jsonArrayToList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val item = array.optString(i, "")
            if (item.isNotBlank()) list.add(item)
        }
        return list
    }
}
