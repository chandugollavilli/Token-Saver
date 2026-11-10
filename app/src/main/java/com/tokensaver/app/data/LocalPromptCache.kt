package com.tokensaver.app.data

import android.content.Context
import java.security.MessageDigest

class LocalPromptCache(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("prompt_fingerprints", Context.MODE_PRIVATE)

    fun hasSeen(prompt: String): Boolean = digest(prompt) in fingerprints()

    fun remember(prompt: String) {
        if (prompt.isBlank()) return
        val current = fingerprints().toMutableList()
        current.remove(digest(prompt))
        current.add(digest(prompt))
        preferences.edit().putStringSet(KEY_FINGERPRINTS, current.takeLast(MAX_FINGERPRINTS).toSet()).apply()
    }

    private fun fingerprints(): Set<String> =
        preferences.getStringSet(KEY_FINGERPRINTS, emptySet())?.toSet() ?: emptySet()

    private fun digest(prompt: String): String = MessageDigest.getInstance("SHA-256")
        .digest(prompt.trim().toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private companion object {
        const val KEY_FINGERPRINTS = "prompt_sha256"
        const val MAX_FINGERPRINTS = 500
    }
}
