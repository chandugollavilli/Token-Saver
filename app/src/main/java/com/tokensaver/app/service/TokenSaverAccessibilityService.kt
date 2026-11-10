package com.tokensaver.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect
import com.tokensaver.app.data.LocalPromptCache
import com.tokensaver.app.data.TokenStatsRepository
import com.tokensaver.app.engine.PromptCompressor
import com.tokensaver.app.engine.TokenEstimator

class TokenSaverAccessibilityService : AccessibilityService() {
    private lateinit var stats: TokenStatsRepository
    private lateinit var cache: LocalPromptCache
    private lateinit var badge: FloatingBadgeManager
    private var activePackage: String? = null
    private var originalPrompt: String = ""
    private var compressedPrompt: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        stats = TokenStatsRepository(this)
        cache = LocalPromptCache(this)
        badge = FloatingBadgeManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::badge.isInitialized) return
        event ?: return
        val packageName = event.packageName?.toString()
        if (!stats.isEnabled() || packageName !in stats.whitelistedPackages()) {
            clearTarget()
            return
        }
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_FOCUSED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return

        val root = rootInActiveWindow ?: run { clearTarget(); return }
        val field = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (field == null || !field.isEditable || !field.isFocused || !isLikelyPromptField(field)) {
            clearTarget()
            return
        }
        val text = field.text?.toString().orEmpty()
        val compressed = PromptCompressor.compress(text)
        if (text.isBlank() || compressed.length >= text.length) {
            clearTarget()
            return
        }
        val bounds = Rect().also(field::getBoundsInScreen)
        activePackage = packageName
        originalPrompt = text
        compressedPrompt = compressed
        badge.showBadge(bounds) { showReview() }
    }

    private fun showReview() {
        if (activePackage == null || compressedPrompt.isBlank()) return
        badge.showPreview(originalPrompt, compressedPrompt, cache.hasSeen(originalPrompt)) {
            applyReviewedText()
        }
    }

    private fun applyReviewedText() {
        val packageName = activePackage ?: return
        if (packageName !in stats.whitelistedPackages() || !stats.isEnabled()) return
        val field = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return
        if (!field.isEditable || !field.isFocused || field.text?.toString() != originalPrompt) return
        val saved = TokenEstimator.estimate(originalPrompt) - TokenEstimator.estimate(compressedPrompt)
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, compressedPrompt)
        }
        if (field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
            stats.recordSavings(packageName, saved)
            cache.remember(originalPrompt)
        }
    }

    private fun isLikelyPromptField(field: AccessibilityNodeInfo): Boolean {
        val metadata = listOfNotNull(
            field.hintText?.toString(),
            field.contentDescription?.toString(),
            field.viewIdResourceName
        ).joinToString(" ").lowercase()
        return metadata.contains(Regex("prompt|message|ask|query|chat|composer|assistant|gemini|claude|perplexity|copilot"))
    }

    private fun clearTarget() {
        badge.clear()
        activePackage = null
        originalPrompt = ""
        compressedPrompt = ""
    }

    override fun onInterrupt() { if (::badge.isInitialized) clearTarget() }

    override fun onDestroy() {
        if (::badge.isInitialized) clearTarget()
        super.onDestroy()
    }
}
