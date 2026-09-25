package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductComparisonData(
    val product: ProductInfo = ProductInfo(),
    val platforms: List<PlatformPriceInfo> = emptyList(),
    val reviewSummary: ReviewSummary = ReviewSummary(),
    val qualitySignals: QualitySignals = QualitySignals(),
    val comparison: PriceComparison = PriceComparison(),
    val aiRecommendation: AiRecommendation = AiRecommendation(),
    val sources: List<SourceCitation> = emptyList(),
    val isWebVerified: Boolean = false,
    val researchStatus: String = "SUCCESS" // "SUCCESS", "PARTIAL", "NO_RESULT"
)

@JsonClass(generateAdapter = true)
data class ProductInfo(
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val variant: String = "",
    val image: String = "",
    val category: String = "",
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class PlatformPriceInfo(
    val platform: String = "",
    val price: Double? = null,
    val priceFormatted: String? = null,
    val currency: String = "INR",
    val availability: String = "Data unavailable",
    val rating: Double? = null,
    val reviewCount: Long? = null,
    val url: String = "",
    val source: String = "",
    val lastVerified: String = "",
    val originalPrice: Double? = null,
    val originalPriceFormatted: String? = null,
    val discountPercentage: Int? = null,
    val deliveryInfo: String = "",
    val matchType: String = "Exact Match", // "Exact Match", "Variant Difference", "Similar Product"
    val matchExplanation: String = "",
    val variantDetail: String = ""
)

@JsonClass(generateAdapter = true)
data class ReviewSummary(
    val positive: List<String> = emptyList(),
    val negative: List<String> = emptyList(),
    val commonComplaints: List<String> = emptyList(),
    val commonPraise: List<String> = emptyList(),
    val overallRatingScore: Double? = null
)

@JsonClass(generateAdapter = true)
data class QualitySignals(
    val buildQuality: String = "",
    val buildQualityRating: Int = 4,
    val performance: String = "",
    val performanceRating: Int = 4,
    val reliability: String = "",
    val reliabilityRating: Int = 4,
    val features: String = "",
    val featuresRating: Int = 4,
    val warranty: String = "",
    val warrantyRating: Int = 4,
    val afterSalesSupport: String = "",
    val valueForMoney: String = "",
    val valueForMoneyRating: Int = 4
)

@JsonClass(generateAdapter = true)
data class PriceComparison(
    val lowestPrice: String = "",
    val lowestPricePlatform: String = "",
    val highestPrice: String = "",
    val bestRated: String = "",
    val bestValue: String = ""
)

@JsonClass(generateAdapter = true)
data class AiRecommendation(
    val recommendation: String = "",
    val reason: String = "",
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val whoShouldBuy: String = "",
    val whoShouldAvoid: String = "",
    val bestPlatformPick: String = "",
    val valueAnalysis: String = "",
    val priceTrendAdvice: String = "",
    val suggestedNextSteps: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SourceCitation(
    val name: String = "",
    val url: String = "",
    val informationObtained: String = "",
    val timestamp: String = ""
)

@JsonClass(generateAdapter = true)
data class VisualIdentificationResult(
    val productName: String = "",
    val brand: String = "",
    val category: String = "",
    val model: String = "",
    val keyFeatures: List<String> = emptyList(),
    val confidence: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val confidenceExplanation: String = "",
    val suggestedQuery: String = ""
)
