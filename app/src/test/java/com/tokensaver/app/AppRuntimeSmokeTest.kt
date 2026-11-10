package com.tokensaver.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.tokensaver.app.data.LocalPromptCache
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class AppRuntimeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun activityAndBothComposeScreensInitializeAndNavigate() {
        composeRule.onNodeWithText("Token Saver").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-list").performScrollToNode(hasTestTag("live-test-sandbox"))
        composeRule.onNodeWithText("Test Compression").performClick()
        composeRule.onNodeWithText("Original tokens: 30 (estimated)").assertIsDisplayed()
        composeRule.onNodeWithText("Compressed tokens: 23 (estimated)").assertIsDisplayed()
        composeRule.onNodeWithText("Tokens saved: 7 (23.33%)").assertIsDisplayed()

        composeRule.onNodeWithText("Apps").performClick()
        composeRule.onNodeWithText("Choose apps").assertIsDisplayed()
        composeRule.onNodeWithText("ChatGPT").assertIsDisplayed()

        composeRule.onNodeWithText("Dashboard").performClick()
        composeRule.onNodeWithText("Token Saver").assertIsDisplayed()
    }

    @Test
    fun localDuplicateCacheRecognizesPreviouslyAppliedPrompt() {
        val preferences = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("prompt_fingerprints", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val prompt = "A unique prompt for the local duplicate-cache smoke test."
        val cache = LocalPromptCache(composeRule.activity)

        assertFalse(cache.hasSeen(prompt))
        cache.remember(prompt)
        assertTrue(cache.hasSeen(prompt))
        assertEquals(64, preferences.getStringSet("prompt_sha256", emptySet())!!.single().length)
    }
}
