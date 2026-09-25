package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.LifeVaultDatabase
import com.example.data.local.entity.VaultDropAuditEntity
import com.example.data.local.entity.VaultDropFileEntity
import com.example.data.local.entity.VaultDropFileStatus
import com.example.data.local.entity.VaultDropShareEntity
import com.example.data.repository.DownloadedFileResult
import com.example.data.repository.VaultDropRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VaultDropTab(val label: String) {
    HERO_UPLOAD("Drop & Share"),
    DASHBOARD("My Files"),
    ACTIVE_SHARES("Links"),
    RECIPIENT_VIEW("Download"),
    ADMIN_PANEL("Security & Admin")
}

class VaultDropViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultDropRepository
    val allFiles: StateFlow<List<VaultDropFileEntity>>
    val allShares: StateFlow<List<VaultDropShareEntity>>
    val auditLogs: StateFlow<List<VaultDropAuditEntity>>
    val totalStorageUsage: StateFlow<Long?>

    // Active platform tab
    private val _activeTab = MutableStateFlow(VaultDropTab.HERO_UPLOAD)
    val activeTab: StateFlow<VaultDropTab> = _activeTab.asStateFlow()

    // Upload configuration
    private val _uploadExpiryHours = MutableStateFlow(24) // 1, 24, 168 (7d), 720 (30d), 0 (Never)
    val uploadExpiryHours: StateFlow<Int> = _uploadExpiryHours.asStateFlow()

    private val _uploadDownloadLimit = MutableStateFlow(0) // 0 = unlimited, 1 = burn after reading, 5, 10
    val uploadDownloadLimit: StateFlow<Int> = _uploadDownloadLimit.asStateFlow()

    private val _uploadPassword = MutableStateFlow("")
    val uploadPassword: StateFlow<String> = _uploadPassword.asStateFlow()

    private val _selectedStorageProvider = MutableStateFlow("LOCAL_ISOLATED")
    val selectedStorageProvider: StateFlow<String> = _selectedStorageProvider.asStateFlow()

    // Upload progress state
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _isUploadPaused = MutableStateFlow(false)
    val isUploadPaused: StateFlow<Boolean> = _isUploadPaused.asStateFlow()

    private val _uploadLifecycleStatus = MutableStateFlow(VaultDropFileStatus.INITIALIZING)
    val uploadLifecycleStatus: StateFlow<String> = _uploadLifecycleStatus.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _uploadSpeedMbps = MutableStateFlow(0.0)
    val uploadSpeedMbps: StateFlow<Double> = _uploadSpeedMbps.asStateFlow()

    private val _uploadEtaSeconds = MutableStateFlow(0)
    val uploadEtaSeconds: StateFlow<Int> = _uploadEtaSeconds.asStateFlow()

    private val _uploadingFilename = MutableStateFlow("")
    val uploadingFilename: StateFlow<String> = _uploadingFilename.asStateFlow()

    private val _uploadingFilesize = MutableStateFlow(0L)
    val uploadingFilesize: StateFlow<Long> = _uploadingFilesize.asStateFlow()

    private var activeUploadJob: Job? = null

    // Newly generated share modal
    private val _newlySharedPair = MutableStateFlow<Pair<VaultDropFileEntity, VaultDropShareEntity>?>(null)
    val newlySharedPair: StateFlow<Pair<VaultDropFileEntity, VaultDropShareEntity>?> = _newlySharedPair.asStateFlow()

    // Active share inspection dialog
    private val _inspectedShare = MutableStateFlow<Pair<VaultDropFileEntity, VaultDropShareEntity>?>(null)
    val inspectedShare: StateFlow<Pair<VaultDropFileEntity, VaultDropShareEntity>?> = _inspectedShare.asStateFlow()

    // Recipient Download View state
    private val _recipientTokenInput = MutableStateFlow("")
    val recipientTokenInput: StateFlow<String> = _recipientTokenInput.asStateFlow()

    private val _resolvedRecipientFile = MutableStateFlow<VaultDropFileEntity?>(null)
    val resolvedRecipientFile: StateFlow<VaultDropFileEntity?> = _resolvedRecipientFile.asStateFlow()

    private val _resolvedRecipientShare = MutableStateFlow<VaultDropShareEntity?>(null)
    val resolvedRecipientShare: StateFlow<VaultDropShareEntity?> = _resolvedRecipientShare.asStateFlow()

    private val _recipientPasswordInput = MutableStateFlow("")
    val recipientPasswordInput: StateFlow<String> = _recipientPasswordInput.asStateFlow()

    private val _recipientErrorMessage = MutableStateFlow<String?>(null)
    val recipientErrorMessage: StateFlow<String?> = _recipientErrorMessage.asStateFlow()

    private val _isDecryptingDownload = MutableStateFlow(false)
    val isDecryptingDownload: StateFlow<Boolean> = _isDecryptingDownload.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadSuccessNotice = MutableStateFlow<String?>(null)
    val downloadSuccessNotice: StateFlow<String?> = _downloadSuccessNotice.asStateFlow()

    private val _lastDownloadedFile = MutableStateFlow<DownloadedFileResult?>(null)
    val lastDownloadedFile: StateFlow<DownloadedFileResult?> = _lastDownloadedFile.asStateFlow()

    // Notification toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val db = LifeVaultDatabase.getDatabase(application)
        repository = VaultDropRepository(application, db.vaultDropDao())

        allFiles = repository.allFiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allShares = repository.allShares.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        auditLogs = repository.auditLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        totalStorageUsage = repository.totalStorageUsage.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L
        )

        viewModelScope.launch {
            repository.seedInitialFilesIfEmpty()
        }
    }

    fun selectTab(tab: VaultDropTab) {
        _activeTab.value = tab
    }

    fun setUploadExpiryHours(hours: Int) {
        _uploadExpiryHours.value = hours
    }

    fun setUploadDownloadLimit(limit: Int) {
        _uploadDownloadLimit.value = limit
    }

    fun setUploadPassword(pwd: String) {
        _uploadPassword.value = pwd
    }

    fun setSelectedStorageProvider(provider: String) {
        _selectedStorageProvider.value = provider
    }

    fun setRecipientTokenInput(token: String) {
        _recipientTokenInput.value = token
    }

    fun setRecipientPasswordInput(pwd: String) {
        _recipientPasswordInput.value = pwd
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.value = message
            delay(2500)
            _toastMessage.value = null
        }
    }

    /**
     * Handles real file picked from Android document provider (wildcard MIME: all types - no restriction)
     */
    fun onFilePickedFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                var displayName = "upload_${System.currentTimeMillis()}.bin"
                var fileSize = 0L

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) displayName = cursor.getString(nameIdx) ?: displayName
                        if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx)
                    }
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: ByteArray(1024)

                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

                startChunkedUpload(
                    filename = displayName,
                    bytes = bytes,
                    mime = mime
                )
            } catch (e: Exception) {
                showToast("Failed to read file: ${e.localizedMessage}")
            }
        }
    }

    /**
     * One-tap quick drop for testing any file type:
     * PDF, ZIP, EXE, APK, DOCX, MP4, MP3, PNG, TAR.GZ, ISO, XYZ
     */
    fun dropQuickSample(filename: String, sampleBytes: ByteArray, mime: String = "application/octet-stream") {
        startChunkedUpload(filename, sampleBytes, mime)
    }

    /**
     * Executes chunked upload with client-side AES-256-GCM encryption,
     * storage verification, and database record creation.
     */
    fun startChunkedUpload(
        filename: String,
        bytes: ByteArray,
        mime: String = "application/octet-stream"
    ) {
        if (_isUploading.value) {
            showToast("Upload already in progress")
            return
        }

        _uploadingFilename.value = filename
        _uploadingFilesize.value = bytes.size.toLong()
        _isUploading.value = true
        _isUploadPaused.value = false
        _uploadProgress.value = 0.05f
        _uploadLifecycleStatus.value = VaultDropFileStatus.INITIALIZING

        activeUploadJob = viewModelScope.launch {
            try {
                val resultPair = repository.uploadFile(
                    filename = filename,
                    rawBytes = bytes,
                    mimeType = mime,
                    passwordProtection = _uploadPassword.value,
                    expiryHours = _uploadExpiryHours.value,
                    downloadLimit = _uploadDownloadLimit.value,
                    storageProviderChoice = _selectedStorageProvider.value,
                    onProgress = { progress, speed, eta, status ->
                        if (!_isUploadPaused.value) {
                            _uploadProgress.value = progress
                            _uploadSpeedMbps.value = speed
                            _uploadEtaSeconds.value = eta
                            _uploadLifecycleStatus.value = status
                        }
                    }
                )

                _uploadProgress.value = 1f
                _uploadLifecycleStatus.value = VaultDropFileStatus.READY
                delay(200)
                _isUploading.value = false
                _newlySharedPair.value = resultPair
                showToast("Encrypted, stored and share link ready!")
            } catch (e: Exception) {
                _isUploading.value = false
                _uploadLifecycleStatus.value = VaultDropFileStatus.FAILED
                showToast("Upload could not be completed. Your file has not been shared.")
            }
        }
    }

    fun pauseUpload() {
        _isUploadPaused.value = true
        showToast("Upload paused")
    }

    fun resumeUpload() {
        _isUploadPaused.value = false
        showToast("Upload resumed")
    }

    fun cancelUpload() {
        activeUploadJob?.cancel()
        _isUploading.value = false
        _isUploadPaused.value = false
        _uploadProgress.value = 0f
        _uploadLifecycleStatus.value = VaultDropFileStatus.FAILED
        showToast("Upload cancelled")
    }

    fun dismissNewlySharedModal() {
        _newlySharedPair.value = null
    }

    fun inspectShare(file: VaultDropFileEntity) {
        viewModelScope.launch {
            val share = repository.allShares.stateIn(viewModelScope).value.firstOrNull { it.fileId == file.id }
            if (share != null) {
                _inspectedShare.value = Pair(file, share)
            } else {
                showToast("No active share link found for this file")
            }
        }
    }

    fun dismissInspectedShare() {
        _inspectedShare.value = null
    }

    fun renameFile(fileId: Long, newName: String) {
        viewModelScope.launch {
            repository.renameFile(fileId, newName)
            showToast("Renamed file to $newName")
        }
    }

    fun toggleShareDisabled(token: String, disable: Boolean) {
        viewModelScope.launch {
            repository.toggleShareDisabled(token, disable)
            showToast(if (disable) "Share link disabled" else "Share link enabled")
        }
    }

    fun deleteFile(file: VaultDropFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
            showToast("File deleted from encrypted vault")
        }
    }

    /**
     * Resolves recipient download page by token or full URL
     */
    fun lookupRecipientShare(input: String) {
        val cleanToken = input.trim()
            .substringAfterLast("/s/")
            .substringBefore("#")
            .substringBefore("?")
            .trim()

        if (cleanToken.isBlank()) {
            _recipientErrorMessage.value = "Please enter a valid share link or token"
            return
        }

        viewModelScope.launch {
            _recipientErrorMessage.value = null
            _resolvedRecipientFile.value = null
            _resolvedRecipientShare.value = null
            _lastDownloadedFile.value = null

            val share = repository.getShareByToken(cleanToken)
            if (share == null) {
                _recipientErrorMessage.value = "Link not found or has been revoked."
                return@launch
            }

            if (share.isDisabled) {
                _recipientErrorMessage.value = "This sharing link has been disabled by the owner."
                return@launch
            }

            if (share.expiresAt != null && System.currentTimeMillis() > share.expiresAt) {
                _recipientErrorMessage.value = "This link has expired."
                return@launch
            }

            if (share.maxDownloads > 0 && share.downloadCount >= share.maxDownloads) {
                _recipientErrorMessage.value = "This link has reached its download limit."
                return@launch
            }

            val file = repository.getFileById(share.fileId)
            if (file == null || file.status == VaultDropFileStatus.DELETED) {
                _recipientErrorMessage.value = "The shared file is no longer available."
                return@launch
            }

            if (file.status != VaultDropFileStatus.READY) {
                _recipientErrorMessage.value = "The file is still processing. Please try again."
                return@launch
            }

            _resolvedRecipientShare.value = share
            _resolvedRecipientFile.value = file
        }
    }

    /**
     * Executes true end-to-end download pipeline:
     * Reads actual encrypted object from storage, decrypts client-side,
     * verifies checksum, increments download count, and saves to device.
     */
    fun downloadAndDecryptResolvedFile() {
        val file = _resolvedRecipientFile.value ?: return
        val share = _resolvedRecipientShare.value ?: return

        viewModelScope.launch {
            _isDecryptingDownload.value = true
            _downloadProgress.value = 0.15f
            _recipientErrorMessage.value = null

            val result = repository.retrieveAndDownloadFile(
                token = share.token,
                clientPassword = _recipientPasswordInput.value
            )

            result.onSuccess { downloaded ->
                _downloadProgress.value = 1f
                _isDecryptingDownload.value = false
                _lastDownloadedFile.value = downloaded
                _downloadSuccessNotice.value = "File decrypted & downloaded: ${downloaded.file.originalFilename} (${formatSize(downloaded.plainBytes.size.toLong())})"

                // Refresh share and file state in view model
                _resolvedRecipientFile.value = downloaded.file
                _resolvedRecipientShare.value = downloaded.share
            }.onFailure { err ->
                _isDecryptingDownload.value = false
                _recipientErrorMessage.value = err.message ?: "Failed to retrieve and download file."
            }
        }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
