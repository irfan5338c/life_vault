package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.database.LifeVaultDatabase
import com.example.data.local.entity.VaultDropFileStatus
import com.example.data.repository.VaultDropRepository
import com.example.service.vaultdrop.VaultDropCrypto
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VaultDropLifecycleTest {

    private lateinit var context: Context
    private lateinit var database: LifeVaultDatabase
    private lateinit var repository: VaultDropRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            LifeVaultDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = VaultDropRepository(context, database.vaultDropDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test all required file types upload, persist, verify, and download matching original bytes`() = runBlocking {
        val testFiles = listOf(
            Pair("test.pdf", "%PDF-1.4\n1 0 obj\n<< /Title (Integration Test PDF) >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF\n".toByteArray(Charsets.ISO_8859_1)),
            Pair("archive.zip", "PK\u0003\u0004\u0014\u0000\u0000\u0000\u0008\u0000RealEncryptedArchiveZipBytes1234567890".toByteArray(Charsets.ISO_8859_1)),
            Pair("installer.exe", "MZ\u0090\u0000\u0003\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u00ff\u00ffRealExecutableInstallerBytes".toByteArray(Charsets.ISO_8859_1)),
            Pair("application.apk", "PK\u0003\u0004\u0014\u0000\u0008\u0000AndroidApkPackageBytes987654321".toByteArray(Charsets.ISO_8859_1)),
            Pair("document.docx", "PK\u0003\u0004WordDocxStandardXMLPayloadBytesDataContent".toByteArray(Charsets.ISO_8859_1)),
            Pair("video.mp4", "\u0000\u0000\u0000 ftypisom\u0000\u0000\u0002\u0000isomiso2mp41VideoPayloadTrackBytes".toByteArray(Charsets.ISO_8859_1)),
            Pair("music.mp3", "ID3\u0003\u0000\u0000\u0000\u0000#TIT2\u0000\u0000\u0015AudioSongMusicStreamPayloadBytes".toByteArray(Charsets.ISO_8859_1)),
            Pair("image.png", "\u0089PNG\r\n\u001a\n\u0000\u0000\u0000\rIHDR\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0001\u0008\u0006\u0000\u0000\u0000\u001f\u0015c4\u0000\u0000\u0000\nIDATx\u009cc\u0000\u0001\u0000\u0000\u0005\u0000\u0001\r\n-\u00b4\u0000\u0000\u0000\u0000IEND\u00aeB`\u0082".toByteArray(Charsets.ISO_8859_1)),
            Pair("backup.tar.gz", "\u001f\u008b\u0008\u0000\u0000\u0000\u0000\u0000\u0000\u0000GzipTarballCompressedPayloadBytes".toByteArray(Charsets.ISO_8859_1)),
            Pair("disk.iso", "CD001\u0001ISO9660OpticalDiskSectorPayloadBytesData".toByteArray(Charsets.ISO_8859_1)),
            Pair("unknown.xyz", "UNKNOWN_CUSTOM_BINARY_EXTENSION_STRUCTURED_DATA_PAYLOAD_TEST_2026".toByteArray(Charsets.UTF_8))
        )

        for ((filename, originalBytes) in testFiles) {
            // 1. Upload & Encrypt & Persist
            val (file, share) = repository.uploadFile(
                filename = filename,
                rawBytes = originalBytes,
                mimeType = "application/octet-stream",
                storageProviderChoice = "LOCAL_ISOLATED"
            )

            // 2. Verify File Entity & Status
            assertEquals(VaultDropFileStatus.READY, file.status)
            assertTrue(file.storageKey.startsWith("objects/"))
            assertEquals(share.fileId, file.id)
            assertNotNull(share.token)

            // 3. Verify object exists in storage
            assertTrue(repository.localStorage.exists(file.storageKey))

            // 4. Download and client-side decrypt via share token
            val downloadResult = repository.retrieveAndDownloadFile(share.token)
            assertTrue("Download failed for $filename: ${downloadResult.exceptionOrNull()?.message}", downloadResult.isSuccess)

            val downloaded = downloadResult.getOrThrow()
            assertEquals(file.id, downloaded.file.id)

            // 5. Verify byte-for-byte exact equality after decryption
            assertArrayEquals("Decrypted bytes mismatch for $filename", originalBytes, downloaded.plainBytes)

            // 6. Verify checksum equality
            val expectedSha256 = VaultDropCrypto.calculateSha256(originalBytes)
            val downloadedSha256 = VaultDropCrypto.calculateSha256(downloaded.plainBytes)
            assertEquals(expectedSha256, downloadedSha256)
        }
    }

    @Test
    fun `test zero-byte file handles cleanly`() = runBlocking {
        val emptyBytes = ByteArray(0)
        val (file, share) = repository.uploadFile("empty.bin", emptyBytes)

        assertEquals(VaultDropFileStatus.READY, file.status)
        assertTrue(repository.localStorage.exists(file.storageKey))

        val downloadResult = repository.retrieveAndDownloadFile(share.token)
        assertTrue(downloadResult.isSuccess)
        assertEquals(0, downloadResult.getOrThrow().plainBytes.size)
    }

    @Test
    fun `test multi-chunk large file reassembly and verification`() = runBlocking {
        // 200KB exceeds 64KB chunk size, requiring 4 chunks
        val largeBytes = ByteArray(200_000) { (it % 256).toByte() }
        val (file, share) = repository.uploadFile("large_archive.bin", largeBytes)

        assertEquals(VaultDropFileStatus.READY, file.status)
        assertTrue(repository.localStorage.exists(file.storageKey))

        val downloadResult = repository.retrieveAndDownloadFile(share.token)
        assertTrue(downloadResult.isSuccess)
        assertArrayEquals(largeBytes, downloadResult.getOrThrow().plainBytes)
    }

    @Test
    fun `test unknown extension and missing MIME type works without error`() = runBlocking {
        val bytes = "Content with unusual extension and blank mime".toByteArray(Charsets.UTF_8)
        val (file, share) = repository.uploadFile(
            filename = "sample.obscureformat",
            rawBytes = bytes,
            mimeType = "" // Blank MIME type
        )

        assertEquals(VaultDropFileStatus.READY, file.status)
        assertEquals("obscureformat", file.extension)
        val downloadResult = repository.retrieveAndDownloadFile(share.token)
        assertTrue(downloadResult.isSuccess)
        assertArrayEquals(bytes, downloadResult.getOrThrow().plainBytes)
    }

    @Test
    fun `test expired share returns error`() = runBlocking {
        val bytes = "Sensitive content".toByteArray(Charsets.UTF_8)
        val (file, share) = repository.uploadFile("temp.txt", bytes)

        // Force expired timestamp in database
        val expiredShare = share.copy(expiresAt = System.currentTimeMillis() - 10_000L)
        database.vaultDropDao().updateShare(expiredShare)

        val result = repository.retrieveAndDownloadFile(share.token)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("expired", ignoreCase = true) == true)
    }

    @Test
    fun `test disabled share returns error`() = runBlocking {
        val bytes = "Sensitive content".toByteArray(Charsets.UTF_8)
        val (file, share) = repository.uploadFile("revocable.txt", bytes)

        repository.toggleShareDisabled(share.token, true)

        val result = repository.retrieveAndDownloadFile(share.token)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("disabled", ignoreCase = true) == true)
    }

    @Test
    fun `test download limit enforcement`() = runBlocking {
        val bytes = "Burn after reading content".toByteArray(Charsets.UTF_8)
        val (file, share) = repository.uploadFile(
            filename = "burn.txt",
            rawBytes = bytes,
            downloadLimit = 1 // Only 1 download allowed
        )

        // 1st download should succeed
        val firstResult = repository.retrieveAndDownloadFile(share.token)
        assertTrue(firstResult.isSuccess)

        // 2nd download must fail because limit = 1
        val secondResult = repository.retrieveAndDownloadFile(share.token)
        assertFalse(secondResult.isSuccess)
        assertTrue(secondResult.exceptionOrNull()?.message?.contains("limit", ignoreCase = true) == true)
    }

    @Test
    fun `test password protected share verifies password`() = runBlocking {
        val bytes = "Confidential Top Secret".toByteArray(Charsets.UTF_8)
        val password = "SuperSecretPassword#2026"
        val (file, share) = repository.uploadFile(
            filename = "classified.pdf",
            rawBytes = bytes,
            passwordProtection = password
        )

        // Wrong password fails
        val wrongResult = repository.retrieveAndDownloadFile(share.token, clientPassword = "WrongPassword")
        assertFalse(wrongResult.isSuccess)
        assertTrue(wrongResult.exceptionOrNull() is SecurityException)

        // Correct password succeeds
        val correctResult = repository.retrieveAndDownloadFile(share.token, clientPassword = password)
        assertTrue(correctResult.isSuccess)
        assertArrayEquals(bytes, correctResult.getOrThrow().plainBytes)
    }

    @Test
    fun `test missing storage object returns error instead of crashing or returning corrupt file`() = runBlocking {
        val bytes = "File to be deleted from physical disk".toByteArray(Charsets.UTF_8)
        val (file, share) = repository.uploadFile("disappearing.txt", bytes)

        // Deliberately delete the physical file from storage to simulate missing object
        repository.localStorage.deleteObject(file.storageKey)
        assertFalse(repository.localStorage.exists(file.storageKey))

        // Retrieve must fail cleanly with "missing in storage" message
        val result = repository.retrieveAndDownloadFile(share.token)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("storage", ignoreCase = true) == true)
    }
}
