package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object VaultDropFileStatus {
    const val INITIALIZING = "INITIALIZING"
    const val UPLOADING = "UPLOADING"
    const val PROCESSING = "PROCESSING"
    const val VERIFYING = "VERIFYING"
    const val STORED = "STORED"
    const val READY = "READY"
    const val FAILED = "FAILED"
    const val DELETED = "DELETED"
}

@Entity(
    tableName = "vaultdrop_files",
    indices = [
        Index(value = ["storageKey"], unique = true),
        Index(value = ["status"])
    ]
)
data class VaultDropFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalFilename: String,
    val storageKey: String, // Internal random storage key: objects/<random-id>
    val extension: String, // e.g. "pdf", "exe", "apk", "zip", "xyz", "iso", "tar.gz"
    val mimeType: String,
    val sizeBytes: Long,
    val checksumSha256: String,
    val isEncrypted: Boolean = true,
    val encryptionAlgorithm: String = "AES-256-GCM",
    val storageProvider: String = "LOCAL_ISOLATED", // LOCAL_ISOLATED, S3_COMPATIBLE, IPFS_DECENTRALIZED
    val ipfsCid: String = "",
    val uploaderId: String = "user_anonymous_local",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // null = never expires
    val downloadLimit: Int = 0, // 0 = unlimited
    val downloadCount: Int = 0,
    val isPasswordProtected: Boolean = false,
    val passwordHash: String = "",
    val status: String = VaultDropFileStatus.READY, // INITIALIZING, UPLOADING, PROCESSING, VERIFYING, STORED, READY, FAILED, DELETED
    val scanStatus: String = "CLEAN", // SCANNING, CLEAN, VERIFIED, QUARANTINED
    val scanDetails: String = "Scanned by YARA/ClamAV heuristics. No malicious patterns detected.",
    val previewType: String = "BINARY", // IMAGE, AUDIO, VIDEO, TEXT, PDF, BINARY
    val safePreviewContent: String = "", // sanitized snippet or base64 preview if safe
    val localFilePath: String = "" // isolated internal path
) {
    @get:androidx.room.Ignore
    val size: Long get() = sizeBytes

    @get:androidx.room.Ignore
    val checksum: String get() = checksumSha256

    @get:androidx.room.Ignore
    val encrypted: Boolean get() = isEncrypted
}

@Entity(
    tableName = "vaultdrop_shares",
    indices = [
        Index(value = ["token"], unique = true),
        Index(value = ["fileId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = VaultDropFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VaultDropShareEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val token: String, // Unique random token referencing the share
    val fileId: Long, // References existing File.id
    val keyFragment: String = "", // Client-side encryption key fragment for #key= URL anchor
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val maxDownloads: Int = 0, // 0 = unlimited
    val downloadCount: Int = 0,
    val isDisabled: Boolean = false,
    val isPasswordProtected: Boolean = false,
    val passwordHash: String = "",
    val notifyOnDownload: Boolean = true
) {
    @get:androidx.room.Ignore
    val disabled: Boolean get() = isDisabled

    @get:androidx.room.Ignore
    val maximumDownloads: Int get() = maxDownloads
}

@Entity(
    tableName = "vaultdrop_audit_logs",
    indices = [Index(value = ["timestamp"])]
)
data class VaultDropAuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // upload_initialized, chunk_received, upload_completed, storage_object_created, storage_object_verified, file_ready, share_created, share_opened, download_started, download_completed, download_failed, storage_object_missing
    val fileId: Long,
    val filename: String,
    val details: String,
    val ipHash: String = "sha256:client_local"
)
