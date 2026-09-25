package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.LifeVaultDao
import com.example.data.local.dao.VaultDropDao
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InsightEntity
import com.example.data.local.entity.LostFoundItemEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.VaultDropAuditEntity
import com.example.data.local.entity.VaultDropFileEntity
import com.example.data.local.entity.VaultDropShareEntity

@Database(
    entities = [
        MemoryEntity::class,
        PurchaseEntity::class,
        LostFoundItemEntity::class,
        InsightEntity::class,
        GoalEntity::class,
        VaultDropFileEntity::class,
        VaultDropShareEntity::class,
        VaultDropAuditEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LifeVaultDatabase : RoomDatabase() {
    abstract fun lifeVaultDao(): LifeVaultDao
    abstract fun vaultDropDao(): VaultDropDao

    companion object {
        @Volatile
        private var INSTANCE: LifeVaultDatabase? = null

        fun getDatabase(context: Context): LifeVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeVaultDatabase::class.java,
                    "lifevault_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
