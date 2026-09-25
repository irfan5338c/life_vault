package com.example.service.vaultdrop

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object VaultDropCrypto {

    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12 // 96-bit IV recommended for GCM
    private const val GCM_TAG_LENGTH = 128 // 128-bit authentication tag
    private const val ALGORITHM = "AES/GCM/NoPadding"

    data class EncryptionResult(
        val ciphertext: ByteArray,
        val ivBase64: String,
        val keyBase64: String,
        val sha256Plaintext: String,
        val sha256Ciphertext: String
    )

    /**
     * Generates a fresh 256-bit cryptographically secure AES key.
     */
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE, SecureRandom())
        return keyGen.generateKey()
    }

    /**
     * Converts a raw key to URL-safe Base64 string for embedding in link fragments (#key=...)
     */
    fun keyToBase64(key: SecretKey): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key.encoded)
    }

    /**
     * Reconstructs SecretKey from URL-safe Base64 string.
     */
    fun keyFromBase64(base64Key: String): SecretKey {
        val decoded = Base64.getUrlDecoder().decode(base64Key)
        return SecretKeySpec(decoded, "AES")
    }

    /**
     * Encrypts arbitrary binary data on the client using AES-256-GCM, prepending the 12-byte IV
     * directly to the ciphertext so the stored object is self-contained.
     */
    fun encryptFilePayload(data: ByteArray, secretKey: SecretKey? = null): EncryptionResult {
        val key = secretKey ?: generateKey()
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val rawCiphertext = cipher.doFinal(data)
        val fullPayload = iv + rawCiphertext

        val plainDigest = calculateSha256(data)
        val cipherDigest = calculateSha256(fullPayload)

        return EncryptionResult(
            ciphertext = fullPayload,
            ivBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(iv),
            keyBase64 = keyToBase64(key),
            sha256Plaintext = plainDigest,
            sha256Ciphertext = cipherDigest
        )
    }

    /**
     * Decrypts AES-256-GCM payload with prepended 12-byte IV using the secret key.
     */
    fun decryptFilePayload(payload: ByteArray, keyBase64: String): ByteArray {
        if (payload.size < GCM_IV_LENGTH) {
            throw IllegalArgumentException("Payload too short to contain IV")
        }
        val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
        val key = keyFromBase64(keyBase64)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * Encrypts arbitrary binary data on the client using AES-256-GCM.
     * The returned key can be kept entirely on the client or passed via URL anchor (#).
     */
    fun encryptBytes(data: ByteArray, secretKey: SecretKey? = null): EncryptionResult {
        val key = secretKey ?: generateKey()
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val ciphertext = cipher.doFinal(data)

        val plainDigest = calculateSha256(data)
        val cipherDigest = calculateSha256(ciphertext)

        return EncryptionResult(
            ciphertext = ciphertext,
            ivBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(iv),
            keyBase64 = keyToBase64(key),
            sha256Plaintext = plainDigest,
            sha256Ciphertext = cipherDigest
        )
    }

    /**
     * Decrypts AES-256-GCM ciphertext using the provided key and IV.
     */
    fun decryptBytes(ciphertext: ByteArray, keyBase64: String, ivBase64: String): ByteArray {
        val key = keyFromBase64(keyBase64)
        val iv = Base64.getUrlDecoder().decode(ivBase64)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * Computes a SHA-256 hex digest for arbitrary byte arrays.
     */
    fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hashes an optional share password with SHA-256 and salt.
     */
    fun hashPassword(password: String, salt: String = "vaultdrop_salt_v1"): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$password".toByteArray(Charsets.UTF_8)
        return digest.digest(combined).joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if entered password matches the stored hash.
     */
    fun verifyPassword(password: String, storedHash: String, salt: String = "vaultdrop_salt_v1"): Boolean {
        if (storedHash.isBlank()) return true
        val computed = hashPassword(password, salt)
        return computed.equals(storedHash, ignoreCase = true)
    }
}
