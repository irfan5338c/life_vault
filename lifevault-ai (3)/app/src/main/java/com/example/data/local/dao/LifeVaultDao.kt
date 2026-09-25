package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InsightEntity
import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeVaultDao {

    // --- Memories ---
    @Query("SELECT * FROM memories ORDER BY pinned DESC, createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE rawText LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR objectName LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchMemories(query: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    // --- Purchases ---
    @Query("SELECT * FROM purchases ORDER BY dateAnalyzed DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<PurchaseEntity>)

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    // --- Lost & Found ---
    @Query("SELECT * FROM lost_found_items ORDER BY createdAt DESC")
    fun getAllLostFoundItems(): Flow<List<LostFoundItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLostFoundItem(item: LostFoundItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLostFoundItems(items: List<LostFoundItemEntity>)

    @Update
    suspend fun updateLostFoundItem(item: LostFoundItemEntity)

    @Delete
    suspend fun deleteLostFoundItem(item: LostFoundItemEntity)

    // --- Insights ---
    @Query("SELECT * FROM insights ORDER BY dateCreated DESC")
    fun getAllInsights(): Flow<List<InsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsights(insights: List<InsightEntity>)

    // --- Goals / Memory Moments ---
    @Query("SELECT * FROM goals ORDER BY originalDateSaved ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    // --- Data Management ---
    @Query("DELETE FROM memories WHERE isDemo = 1")
    suspend fun deleteDemoMemories()

    @Query("DELETE FROM purchases WHERE isDemo = 1")
    suspend fun deleteDemoPurchases()

    @Query("DELETE FROM lost_found_items WHERE isDemo = 1")
    suspend fun deleteDemoLostFound()

    @Query("DELETE FROM insights WHERE isDemo = 1")
    suspend fun deleteDemoInsights()

    @Query("DELETE FROM goals WHERE isDemo = 1")
    suspend fun deleteDemoGoals()

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    @Query("DELETE FROM purchases")
    suspend fun clearAllPurchases()

    @Query("DELETE FROM lost_found_items")
    suspend fun clearAllLostFound()

    @Query("DELETE FROM insights")
    suspend fun clearAllInsights()

    @Query("DELETE FROM goals")
    suspend fun clearAllGoals()
}
