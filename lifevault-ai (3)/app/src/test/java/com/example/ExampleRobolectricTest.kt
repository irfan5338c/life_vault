package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AIIntentResult
import com.example.ai.LifeVaultAIEngine
import com.example.data.local.entity.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LifeVault AI", appName)
    }

    @Test
    fun `ai engine extracts object and location from natural sentence`() {
        val input = "I kept my calculator inside my blue college backpack."
        val result = LifeVaultAIEngine.extractMemory(input)

        assertEquals("Calculator", result.objectName)
        assertTrue(result.location.contains("blue college backpack", ignoreCase = true))
        assertEquals("Belongings", result.category)
    }

    @Test
    fun `ai engine processes memory question accurately`() {
        val memories = listOf(
            MemoryEntity(
                id = 1,
                title = "Passport",
                rawText = "My passport is in the top-right drawer of the oak cabinet.",
                objectName = "Passport",
                location = "Top-right drawer of the oak cabinet",
                category = "Belongings"
            )
        )

        val result = LifeVaultAIEngine.processQuery(
            query = "Where is my passport?",
            memories = memories,
            purchases = emptyList(),
            lostFoundItems = emptyList()
        )

        assertTrue(result is AIIntentResult.MemoryAnswer)
        val answer = result as AIIntentResult.MemoryAnswer
        assertNotNull(answer.foundMemory)
        assertEquals("Passport", answer.foundMemory?.objectName)
        assertTrue(answer.answer.contains("top-right drawer", ignoreCase = true))
    }

    @Test
    fun `ai engine evaluates purchase before buying`() {
        val result = LifeVaultAIEngine.analyzePurchase(
            productName = "Budget Headphones",
            price = 45.0,
            specs = "Standard wireless audio",
            statedNeed = "Study and library work",
            existingBelongings = emptyList()
        )

        assertEquals("BUY", result.verdict)
    }

    @Test
    fun `voice command strings are correctly configured`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val voiceTitle = context.getString(R.string.voice_input_title)
        val voiceListening = context.getString(R.string.voice_listening)
        assertEquals("Voice Command", voiceTitle)
        assertTrue(voiceListening.contains("Listening", ignoreCase = true))
    }
}
