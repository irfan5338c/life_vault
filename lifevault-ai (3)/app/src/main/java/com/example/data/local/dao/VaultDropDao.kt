package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.VaultDropAuditEntity
import com.example.data.local.entity.VaultDropFileEntity
import com.example.data.local.entity.VaultDropShareEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDropDao {

    // Files
    @Query("SELECT * FROM vaultdrop_files WHERE status != 'DELETED' ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<VaultDropFileEntity>>

    @Query("SELECT * FROM vaultdrop_files WHERE id = :id")
    suspend fun getFileById(id: Long): VaultDropFileEntity?

    @Query("SELECT * FROM vaultdrop_files WHERE storageKey = :key")
    suspend fun getFileByStorageKey(key: String): VaultDropFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultDropFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<VaultDropFileEntity>)

    @Update
    suspend fun updateFile(file: VaultDropFileEntity)

    @Delete
    suspend fun deleteFile(file: VaultDropFileEntity)

    @Query("UPDATE vaultdrop_files SET status = 'DELETED' WHERE id = :id")
    suspend fun markFileDeleted(id: Long)

    @Query("UPDATE vaultdrop_files SET status = :status WHERE id = :id")
    suspend fun updateFileStatus(id: Long, status: String)

    @Query("SELECT * FROM vaultdrop_files WHERE status = 'READY' ORDER BY createdAt DESC")
    fun getReadyFiles(): Flow<List<VaultDropFileEntity>>

    @Query("UPDATE vaultdrop_files SET originalFilename = :newFilename WHERE id = :id")
    suspend fun renameFileMetadata(id: Long, newFilename: String)

    @Query("UPDATE vaultdrop_files SET downloadCount = downloadCount + 1 WHERE id = :id")
    suspend fun incrementFileDownloadCount(id: Long)

    @Query("SELECT SUM(sizeBytes) FROM vaultdrop_files WHERE status != 'DELETED'")
    fun getTotalStorageUsage(): Flow<Long?>

    // Shares
    @Query("SELECT * FROM vaultdrop_shares ORDER BY createdAt DESC")
    fun getAllShares(): Flow<List<VaultDropShareEntity>>

    @Query("SELECT * FROM vaultdrop_shares WHERE token = :token LIMIT 1")
    suspend fun getShareByToken(token: String): VaultDropShareEntity?

    @Query("SELECT * FROM vaultdrop_shares WHERE fileId = :fileId LIMIT 1")
    suspend fun getShareByFileId(fileId: Long): VaultDropShareEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: VaultDropShareEntity): Long

    @Update
    suspend fun updateShare(share: VaultDropShareEntity)

    @Query("UPDATE vaultdrop_shares SET isDisabled = :disabled WHERE token = :token")
    suspend fun setShareDisabled(token: String, disabled: Boolean)

    @Query("UPDATE vaultdrop_shares SET downloadCount = downloadCount + 1 WHERE token = :token")
    suspend fun incrementShareDownloadCount(token: String)

    // Audit Logs
    @Query("SELECT * FROM vaultdrop_audit_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAuditLogs(): Flow<List<VaultDropAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: VaultDropAuditEntity): Long
}
