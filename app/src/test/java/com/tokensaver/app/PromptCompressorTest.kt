package com.tokensaver.app

import com.tokensaver.app.engine.PromptCompressor
import com.tokensaver.app.engine.TokenEstimator
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
}
