package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val rawText: String,
    val objectName: String,
    val location: String,
    val category: String, // Belongings, Dates, Plans, Ideas, Notes, Study, Goals
    val confidence: Float = 0.95f,
    val createdAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val isDemo: Boolean = false
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val price: Double,
    val currency: String = "$",
    val category: String,
    val specs: String,
    val statedNeed: String,
    val verdict: String, // "BUY", "WAIT", "SKIP"
    val reasoning: String,
    val alternatives: String = "",
    val dateAnalyzed: Long = System.currentTimeMillis(),
    val isPurchased: Boolean = false,
    val warrantyExpiryDate: Long? = null,
    val isDemo: Boolean = false
)

@Entity(tableName = "lost_found_items")
data class LostFoundItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "LOST", "FOUND"
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val reportedTime: String,
    val status: String = "ACTIVE", // "ACTIVE", "MATCHED", "RECOVERED"
    val contactInfo: String = "Hostel Admin / In-App Message",
    val matchedItemId: Long? = null,
    val matchConfidence: Int = 0, // e.g. 87 for 87% match
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val period: String, // "This Month", "This Week"
    val title: String,
    val explanation: String,
    val spendingChangePercent: Int, // e.g. 18
    val shoppingChangePercent: Int, // e.g. 31
    val studyChangePercent: Int, // e.g. 14
    val goalsChangePercent: Int, // e.g. 22
    val highlightCategory: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalDateSaved: Long = System.currentTimeMillis() - (120L * 24 * 60 * 60 * 1000), // e.g. 4 months ago
    val status: String = "ACTIVE", // ACTIVE, CONTINUED, ARCHIVED, REMIND_LATER
    val isDemo: Boolean = false
)
