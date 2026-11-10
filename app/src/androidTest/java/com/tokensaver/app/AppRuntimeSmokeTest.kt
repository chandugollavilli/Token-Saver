package com.tokensaver.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tokensaver.app.data.LocalPromptCache
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRuntimeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun activityAndBothComposeScreensInitializeAndNavigate() {
        composeRule.onNodeWithText("Token Saver").assertIsDisplayed()
        composeRule.onNodeWithTag("live-test-sandbox").performScrollTo()
        composeRule.onNodeWithText("Test Compression").assertIsDisplayed()
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
        val prompt = "A unique prompt for the local duplicate-cache smoke test."
        val cache = LocalPromptCache(composeRule.activity)

        assertFalse(cache.hasSeen(prompt))
        cache.remember(prompt)
        assertTrue(cache.hasSeen(prompt))
    }
}
