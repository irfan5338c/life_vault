package com.example.data.repository

import com.example.data.local.dao.LifeVaultDao
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InsightEntity
import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

class LifeVaultRepository(private val dao: LifeVaultDao) {

    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    val allPurchases: Flow<List<PurchaseEntity>> = dao.getAllPurchases()
    val allLostFoundItems: Flow<List<LostFoundItemEntity>> = dao.getAllLostFoundItems()
    val allInsights: Flow<List<InsightEntity>> = dao.getAllInsights()
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()

    fun searchMemories(query: String): Flow<List<MemoryEntity>> = dao.searchMemories(query)

    suspend fun saveMemory(memory: MemoryEntity): Long = dao.insertMemory(memory)
    suspend fun updateMemory(memory: MemoryEntity) = dao.updateMemory(memory)
    suspend fun deleteMemory(memory: MemoryEntity) = dao.deleteMemory(memory)

    suspend fun savePurchase(purchase: PurchaseEntity): Long = dao.insertPurchase(purchase)
    suspend fun updatePurchase(purchase: PurchaseEntity) = dao.updatePurchase(purchase)
    suspend fun deletePurchase(purchase: PurchaseEntity) = dao.deletePurchase(purchase)

    suspend fun saveLostFoundItem(item: LostFoundItemEntity): Long = dao.insertLostFoundItem(item)
    suspend fun updateLostFoundItem(item: LostFoundItemEntity) = dao.updateLostFoundItem(item)
    suspend fun deleteLostFoundItem(item: LostFoundItemEntity) = dao.deleteLostFoundItem(item)

    suspend fun saveInsight(insight: InsightEntity): Long = dao.insertInsight(insight)

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    suspend fun saveGoal(goal: GoalEntity): Long = dao.insertGoal(goal)

    suspend fun clearDemoData() {
        dao.deleteDemoMemories()
        dao.deleteDemoPurchases()
        dao.deleteDemoLostFound()
        dao.deleteDemoInsights()
        dao.deleteDemoGoals()
    }

    suspend fun clearAllData() {
        dao.clearAllMemories()
        dao.clearAllPurchases()
        dao.clearAllLostFound()
        dao.clearAllInsights()
        dao.clearAllGoals()
    }

    suspend fun seedDemoDataIfEmpty() {
        // Preload rich sample data to bring LifeVault AI to life
        val initialMemories = listOf(
            MemoryEntity(
                title = "Passport & Travel Documents",
                rawText = "My passport is in the top-right drawer of the oak cabinet.",
                objectName = "Passport",
                location = "Top-right drawer of oak cabinet",
                category = "Belongings",
                confidence = 0.98f,
                pinned = true,
                isDemo = true
            ),
            MemoryEntity(
                title = "College Calculator",
                rawText = "I kept my calculator inside my blue college backpack front pouch.",
                objectName = "Calculator",
                location = "Blue college backpack front pouch",
                category = "Belongings",
                confidence = 0.96f,
                pinned = true,
                isDemo = true
            ),
            MemoryEntity(
                title = "Bike Registration & Spare Key",
                rawText = "Bike documents and duplicate key stored in bedroom safe (code hint: graduation year).",
                objectName = "Bike Documents & Key",
                location = "Bedroom safe",
                category = "Belongings",
                confidence = 0.95f,
                pinned = false,
                isDemo = true
            ),
            MemoryEntity(
                title = "Sony WH-1000XM5 Headphones",
                rawText = "Purchased Sony noise cancelling headphones for $348 with 2-year warranty.",
                objectName = "Sony Headphones",
                location = "Tech organizer case / Desk",
                category = "Purchases",
                confidence = 0.99f,
                pinned = false,
                isDemo = true
            ),
            MemoryEntity(
                title = "Semester Project Presentation",
                rawText = "Final mobile architecture presentation on October 28 at 10:00 AM in Room 402.",
                objectName = "Presentation",
                location = "Room 402",
                category = "Plans",
                confidence = 0.92f,
                pinned = false,
                isDemo = true
            ),
            MemoryEntity(
                title = "Startup Idea: Local Mesh Vault",
                rawText = "Concept for decentralized personal intelligence with zero cloud telemetry.",
                objectName = "Startup Idea",
                location = "Digital Notes",
                category = "Ideas",
                confidence = 0.90f,
                pinned = false,
                isDemo = true
            )
        )

        val initialPurchases = listOf(
            PurchaseEntity(
                productName = "Sony WH-1000XM5 Headphones",
                price = 348.0,
                currency = "$",
                category = "Electronics",
                specs = "30hr battery, Industry-leading ANC, Multi-device pairing",
                statedNeed = "High focus while studying in campus libraries and cafes",
                verdict = "BUY",
                reasoning = "Excellent active noise cancellation for high-distraction environments. Long battery longevity and fits your designated tech budget.",
                alternatives = "Bose QuietComfort 45 ($279)",
                isPurchased = true,
                warrantyExpiryDate = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000), // Expires in 14 days!
                isDemo = true
            ),
            PurchaseEntity(
                productName = "Ergonomic Mesh Chair",
                price = 220.0,
                currency = "$",
                category = "Home Office",
                specs = "Adjustable lumbar support, 3D armrests, breathable mesh",
                statedNeed = "Replace stiff dining chair causing lower back strain",
                verdict = "BUY",
                reasoning = "High health utility for 8+ hour coding sessions. Direct upgrade from current uncomfortable setup.",
                alternatives = "Sihoo Doro C300 ($280)",
                isPurchased = true,
                isDemo = true
            ),
            PurchaseEntity(
                productName = "Keychron K2 Mechanical Keyboard",
                price = 89.0,
                currency = "$",
                category = "Electronics",
                specs = "75% layout, wireless Bluetooth, Gateron Brown switches",
                statedNeed = "Want tactile mechanical typing feel for coding",
                verdict = "WAIT",
                reasoning = "Your current keyboard is fully operational. A holiday sale is expected in 3 weeks with 20% discount.",
                alternatives = "Royal Kludge RK84 ($65)",
                isPurchased = false,
                isDemo = true
            ),
            PurchaseEntity(
                productName = "Ultra Titanium Smartwatch",
                price = 799.0,
                currency = "$",
                category = "Wearables",
                specs = "Dual GPS, sapphire glass, diving sensor",
                statedNeed = "Check notifications and track gym workouts",
                verdict = "SKIP",
                reasoning = "Exceeds monthly discretionary budget by 65%. 90% of your stated workout needs are already met by your existing fitness band.",
                alternatives = "Amazfit Balance ($199) or keep current band",
                isPurchased = false,
                isDemo = true
            )
        )

        val initialLostFound = listOf(
            LostFoundItemEntity(
                type = "LOST",
                title = "Blue College Backpack",
                description = "Navy blue Jansport backpack containing spiral notebook and scientific calculator.",
                category = "Bags",
                location = "Central Library - 2nd Floor Study Area",
                reportedTime = "Yesterday at 4:30 PM",
                status = "MATCHED",
                contactInfo = "Student ID #4829 / Hostel B Room 204",
                matchedItemId = 2,
                matchConfidence = 87,
                isDemo = true
            ),
            LostFoundItemEntity(
                type = "FOUND",
                title = "Navy Jansport Backpack with Tech Pouch",
                description = "Found near study cubicle row 4. Has a front pouch with calculator and notebooks.",
                category = "Bags",
                location = "Library 2nd Floor Cubicles",
                reportedTime = "Yesterday at 6:00 PM",
                status = "MATCHED",
                contactInfo = "Campus Security Desk (Ext. 201)",
                matchedItemId = 1,
                matchConfidence = 87,
                isDemo = true
            ),
            LostFoundItemEntity(
                type = "FOUND",
                title = "Black Wireless Earbud Case",
                description = "Anker Soundcore charging case found on bench outside Cafeteria.",
                category = "Electronics",
                location = "Campus Cafeteria Garden",
                reportedTime = "Today at 11:15 AM",
                status = "ACTIVE",
                contactInfo = "Student Union Reception",
                matchConfidence = 0,
                isDemo = true
            )
        )

        val initialInsights = listOf(
            InsightEntity(
                period = "This Month",
                title = "Spending & Activity Shifts",
                explanation = "Shopping increased significantly this month. Your saved purchases show that most of the increase came from electronics and ergonomics upgrades (Sony ANC headphones & mesh chair).",
                spendingChangePercent = 18,
                shoppingChangePercent = 31,
                studyChangePercent = 14,
                goalsChangePercent = 22,
                highlightCategory = "Electronics & Study Gear",
                isDemo = true
            ),
            InsightEntity(
                period = "This Week",
                title = "Focus & Organization Momentum",
                explanation = "You added 6 new memories and organized your key belongings. Focus time tracked up 14% this week with fewer misplaced item distractions.",
                spendingChangePercent = -5,
                shoppingChangePercent = -12,
                studyChangePercent = 14,
                goalsChangePercent = 15,
                highlightCategory = "Study Habits",
                isDemo = true
            )
        )

        val initialGoals = listOf(
            GoalEntity(
                title = "I want to learn Python properly.",
                originalDateSaved = System.currentTimeMillis() - (120L * 24 * 60 * 60 * 1000), // 4 months ago
                status = "ACTIVE",
                isDemo = true
            ),
            GoalEntity(
                title = "Complete 5km morning run 3x per week",
                originalDateSaved = System.currentTimeMillis() - (45L * 24 * 60 * 60 * 1000),
                status = "CONTINUED",
                isDemo = true
            )
        )

        dao.insertMemories(initialMemories)
        dao.insertPurchases(initialPurchases)
        dao.insertLostFoundItems(initialLostFound)
        dao.insertInsights(initialInsights)
        dao.insertGoals(initialGoals)
    }
}
