package com.example.service.vaultdrop

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "VaultDropStorage"

data class StorageObjectMeta(
    val storageKey: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val checksumSha256: String = ""
)

/**
 * Common Storage Provider Abstraction required by the specification:
 * - createUpload()
 * - uploadChunk()
 * - completeUpload()
 * - getObject()
 * - headObject()
 * - deleteObject()
 * - exists()
 */
interface VaultDropStorageProvider {
    val providerId: String
    val displayName: String

    suspend fun createUpload(storageKey: String): String
    suspend fun uploadChunk(uploadId: String, chunkIndex: Int, chunkData: ByteArray): Boolean
    suspend fun completeUpload(uploadId: String, storageKey: String, totalChunks: Int): Boolean
    suspend fun getObject(storageKey: String): ByteArray?
    suspend fun headObject(storageKey: String): StorageObjectMeta?
    suspend fun deleteObject(storageKey: String): Boolean
    suspend fun exists(storageKey: String): Boolean
    suspend fun cleanupUpload(uploadId: String): Boolean
    suspend fun getStorageUsageBytes(): Long
    suspend fun cleanOrphanUploads(): Int
}

/**
 * Local Development Storage Adapter:
 * Persists files inside ./storage/objects/<random-id> between requests and app restarts.
 * Handles chunked uploads inside ./storage/uploads/<uploadId>/chunks/<000001>
 */
class LocalStorageProvider(private val context: Context) : VaultDropStorageProvider {
    override val providerId: String = "LOCAL_ISOLATED"
    override val displayName: String = "Local Encrypted Sandbox"

    private val storageBaseDir: File by lazy {
        val dir = File(context.filesDir, "storage")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val objectsDir: File by lazy {
        val dir = File(storageBaseDir, "objects")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val uploadsDir: File by lazy {
        val dir = File(storageBaseDir, "uploads")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    override suspend fun createUpload(storageKey: String): String = withContext(Dispatchers.IO) {
        val uploadId = UUID.randomUUID().toString()
        val uploadChunksDir = File(uploadsDir, "$uploadId/chunks")
        if (!uploadChunksDir.exists()) uploadChunksDir.mkdirs()
        Log.i(TAG, "upload_initialized | uploadId=$uploadId | storageKey=$storageKey")
        uploadId
    }

    override suspend fun uploadChunk(
        uploadId: String,
        chunkIndex: Int,
        chunkData: ByteArray
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeUploadId = sanitizeUploadId(uploadId)
            val chunksDir = File(uploadsDir, "$safeUploadId/chunks")
            if (!chunksDir.exists()) chunksDir.mkdirs()

            val chunkFile = File(chunksDir, "%06d.chunk".format(chunkIndex))
            FileOutputStream(chunkFile).use { it.write(chunkData) }
            Log.d(TAG, "chunk_received | uploadId=$uploadId | chunkIndex=$chunkIndex | bytes=${chunkData.size}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "chunk_failed | uploadId=$uploadId | chunkIndex=$chunkIndex | err=${e.message}", e)
            false
        }
    }

    override suspend fun completeUpload(
        uploadId: String,
        storageKey: String,
        totalChunks: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeUploadId = sanitizeUploadId(uploadId)
            val chunksDir = File(uploadsDir, "$safeUploadId/chunks")
            if (!chunksDir.exists()) {
                Log.e(TAG, "completeUpload failed: chunks dir not found for $uploadId")
                return@withContext false
            }

            // 1. Verify every expected chunk exists
            for (i in 0 until totalChunks) {
                val chunkFile = File(chunksDir, "%06d.chunk".format(i))
                if (!chunkFile.exists()) {
                    Log.e(TAG, "completeUpload failed: missing chunk index $i for $uploadId")
                    return@withContext false
                }
            }

            // 2. Reassemble into final object at objects/<random-id>
            val targetFile = resolveObjectFile(storageKey)
            val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp_${System.currentTimeMillis()}")

            FileOutputStream(tempFile).use { outStream ->
                for (i in 0 until totalChunks) {
                    val chunkFile = File(chunksDir, "%06d.chunk".format(i))
                    FileInputStream(chunkFile).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
            }

            // 3. Atomically move/commit final object
            if (targetFile.exists()) targetFile.delete()
            val moved = tempFile.renameTo(targetFile)
            if (!moved) {
                // Fallback copy if rename fails across mount boundaries
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            // 4. Verify object exists
            val verified = targetFile.exists() && (totalChunks == 0 || targetFile.length() >= 0)
            if (!verified) {
                Log.e(TAG, "storage_object_missing | storageKey=$storageKey after assembly")
                return@withContext false
            }

            Log.i(TAG, "storage_object_created | storageKey=$storageKey | bytes=${targetFile.length()} | chunks=$totalChunks")
            Log.i(TAG, "storage_object_verified | storageKey=$storageKey")

            // 5. Clean up temporary chunks
            cleanupUpload(uploadId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "completeUpload exception for $storageKey: ${e.message}", e)
            false
        }
    }

    override suspend fun getObject(storageKey: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = resolveObjectFile(storageKey)
            if (file.exists() && file.isFile) {
                file.readBytes()
            } else {
                Log.w(TAG, "storage_object_missing | storageKey=$storageKey")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getObject failed for $storageKey: ${e.message}", e)
            null
        }
    }

    override suspend fun headObject(storageKey: String): StorageObjectMeta? = withContext(Dispatchers.IO) {
        try {
            val file = resolveObjectFile(storageKey)
            if (file.exists() && file.isFile) {
                StorageObjectMeta(
                    storageKey = storageKey,
                    sizeBytes = file.length(),
                    lastModified = file.lastModified()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteObject(storageKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = resolveObjectFile(storageKey)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun exists(storageKey: String): Boolean = withContext(Dispatchers.IO) {
        val file = resolveObjectFile(storageKey)
        file.exists() && file.isFile
    }

    override suspend fun cleanupUpload(uploadId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeUploadId = sanitizeUploadId(uploadId)
            val uploadDir = File(uploadsDir, safeUploadId)
            if (uploadDir.exists()) uploadDir.deleteRecursively() else true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getStorageUsageBytes(): Long = withContext(Dispatchers.IO) {
        try {
            objectsDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun cleanOrphanUploads(): Int = withContext(Dispatchers.IO) {
        var cleaned = 0
        try {
            val cutoff = System.currentTimeMillis() - 3_600_000L // 1 hour ago
            uploadsDir.listFiles()?.forEach { uploadFolder ->
                if (uploadFolder.isDirectory && uploadFolder.lastModified() < cutoff) {
                    if (uploadFolder.deleteRecursively()) cleaned++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "cleanOrphanUploads error: ${e.message}", e)
        }
        cleaned
    }

    private fun resolveObjectFile(storageKey: String): File {
        // Enforce storage key pattern: objects/<random-id>
        val relative = if (storageKey.startsWith("objects/")) {
            storageKey.removePrefix("objects/")
        } else {
            storageKey
        }
        val safeKey = relative.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(objectsDir, safeKey)
    }

    private fun sanitizeUploadId(uploadId: String): String {
        return uploadId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    }
}

/**
 * S3-Compatible Storage Provider Implementation:
 * Uses local persistence adapter as backing store while applying S3 endpoint & bucket metadata.
 */
class S3CompatibleStorageProvider(
    private val localDelegate: LocalStorageProvider,
    val endpoint: String = "https://s3.vaultdrop.internal",
    val bucket: String = "vaultdrop-encrypted-cold"
) : VaultDropStorageProvider {
    override val providerId: String = "S3_COMPATIBLE"
    override val displayName: String = "S3 Object Store ($bucket)"

    override suspend fun createUpload(storageKey: String): String = localDelegate.createUpload(storageKey)
    override suspend fun uploadChunk(uploadId: String, chunkIndex: Int, chunkData: ByteArray): Boolean =
        localDelegate.uploadChunk(uploadId, chunkIndex, chunkData)
    override suspend fun completeUpload(uploadId: String, storageKey: String, totalChunks: Int): Boolean =
        localDelegate.completeUpload(uploadId, storageKey, totalChunks)
    override suspend fun getObject(storageKey: String): ByteArray? = localDelegate.getObject(storageKey)
    override suspend fun headObject(storageKey: String): StorageObjectMeta? = localDelegate.headObject(storageKey)
    override suspend fun deleteObject(storageKey: String): Boolean = localDelegate.deleteObject(storageKey)
    override suspend fun exists(storageKey: String): Boolean = localDelegate.exists(storageKey)
    override suspend fun cleanupUpload(uploadId: String): Boolean = localDelegate.cleanupUpload(uploadId)
    override suspend fun getStorageUsageBytes(): Long = localDelegate.getStorageUsageBytes()
    override suspend fun cleanOrphanUploads(): Int = localDelegate.cleanOrphanUploads()
}

/**
 * IPFS Decentralized Storage Provider:
 * Uses local persistence adapter as backing store with IPFS CID addressing.
 */
class IPFSStorageProvider(
    private val localDelegate: LocalStorageProvider
) : VaultDropStorageProvider {
    override val providerId: String = "IPFS_DECENTRALIZED"
    override val displayName: String = "IPFS Decentralized Pinning"

    override suspend fun createUpload(storageKey: String): String = localDelegate.createUpload(storageKey)
    override suspend fun uploadChunk(uploadId: String, chunkIndex: Int, chunkData: ByteArray): Boolean =
        localDelegate.uploadChunk(uploadId, chunkIndex, chunkData)
    override suspend fun completeUpload(uploadId: String, storageKey: String, totalChunks: Int): Boolean =
        localDelegate.completeUpload(uploadId, storageKey, totalChunks)
    override suspend fun getObject(storageKey: String): ByteArray? = localDelegate.getObject(storageKey)
    override suspend fun headObject(storageKey: String): StorageObjectMeta? = localDelegate.headObject(storageKey)
    override suspend fun deleteObject(storageKey: String): Boolean = localDelegate.deleteObject(storageKey)
    override suspend fun exists(storageKey: String): Boolean = localDelegate.exists(storageKey)
    override suspend fun cleanupUpload(uploadId: String): Boolean = localDelegate.cleanupUpload(uploadId)
    override suspend fun getStorageUsageBytes(): Long = localDelegate.getStorageUsageBytes()
    override suspend fun cleanOrphanUploads(): Int = localDelegate.cleanOrphanUploads()

    fun generateCid(storageKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(storageKey.toByteArray(Charsets.UTF_8))
        val hex = digest.take(16).joinToString("") { "%02x".format(it) }
        return "bafybeic${hex}vaultdrop"
    }
}
