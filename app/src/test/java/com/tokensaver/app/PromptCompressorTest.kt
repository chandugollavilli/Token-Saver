package com.tokensaver.app

import com.tokensaver.app.engine.PromptCompressor
import com.tokensaver.app.engine.TokenEstimator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptCompressorTest {
    @Test
    fun removesFillerAndNormalizesWhitespace() {
        assertEquals("Summarize this", PromptCompressor.compress("  Please   summarize   this  "))
    }

    @Test
    fun minifiesJsonWithoutChangingStringContents() {
        assertEquals("{\"note\":\"a  b\",\"ok\":true}", PromptCompressor.compress("{ \"note\": \"a  b\", \"ok\": true }"))
    }

    @Test
    fun estimatorHandlesEmptyAndLongerText() {
        assertEquals(0, TokenEstimator.estimate("  "))
        assertTrue(TokenEstimator.estimate("A longer sentence with several words") > TokenEstimator.estimate("Short"))
    }

    @Test
    fun printsBeforeAndAfterComparisonForRealWorldPrompts() {
        val examples = listOf(
            "Chat" to "Please, could you please summarize the meeting transcript and basically list the decisions, owners, and deadlines for each action item?",
            "JSON payload" to "{ \"request\": \"Please analyze this user event\", \"metadata\": { \"source\": \"mobile app\", \"retry\": false }, \"items\": [ 1, 2, 3 ] }",
            "Server logs" to "Please investigate these server logs and, basically, explain the likely cause:\n2025-02-18T10:14:02Z ERROR api timeout   request_id=7f31\n2025-02-18T10:14:03Z WARN retry scheduled   attempt=2",
            "Code block" to "Please explain what this code does and identify any edge cases:\n```kotlin\nfun total(values: List<Int>) = values.sum()\n```",
            "Duplicate check" to "Please, basically, compare this prompt with the previous request and tell me whether the question and expected answer are the same."
        )
        val report = buildString {
            appendLine("\nPrompt compression comparison (estimated tokens)")
            appendLine("Case | Before | After | Saved | Saved % | Compressed text")
            appendLine("-----|--------|-------|-------|---------|----------------")
            examples.forEach { (name, original) ->
                val compressed = PromptCompressor.compress(original)
                val before = TokenEstimator.estimate(original)
                val after = TokenEstimator.estimate(compressed)
                val saved = before - after
                val percent = if (before == 0) 0.0 else saved * 100.0 / before
                appendLine("$name | $before | $after | $saved | ${String.format(Locale.US, "%.2f", percent)}% | ${compressed.replace('\n', ' ')}")
                assertTrue("$name must not increase the estimate", after <= before)
            }
        }
        println(report)
        assertEquals(5, examples.size)
        assertTrue(PromptCompressor.compress(examples[1].second).contains("\"request\":\"Please analyze this user event\""))
        val repeatedPrompt = examples.last().second
        assertEquals(PromptCompressor.compress(repeatedPrompt), PromptCompressor.compress(repeatedPrompt))
    }
}
