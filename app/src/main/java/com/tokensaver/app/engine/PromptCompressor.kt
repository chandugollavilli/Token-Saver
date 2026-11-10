package com.tokensaver.app.engine

object PromptCompressor {
    private val fillerPhrases = listOf(
        Regex("\\b(?:could you please|would you please|I think that|I think|in my opinion|basically|actually|just|kindly|please)\\b[,:]?\\s*", RegexOption.IGNORE_CASE)
    )

    fun compress(input: String): String {
        if (input.isBlank()) return input
        val isJson = looksLikeJson(input)
        val structured = if (isJson) minifyJsonWhitespace(input) else input
        if (isJson) return structured
        var result = fillerPhrases.fold(structured) { text, pattern -> pattern.replace(text, "") }
        result = result.replace(Regex("[ \\t]+"), " ")
            .replace(Regex(" *\\n *"), "\n")
            .trim()
        if (input.trimStart().firstOrNull()?.isUpperCase() == true && result.firstOrNull()?.isLowerCase() == true) {
            result = result.replaceFirstChar { it.uppercase() }
        }
        return result.ifBlank { input }
    }

    private fun looksLikeJson(text: String): Boolean {
        val trimmed = text.trimStart()
        return trimmed.startsWith("{") || trimmed.startsWith("[")
    }

    private fun minifyJsonWhitespace(text: String): String {
        val output = StringBuilder(text.length)
        var inString = false
        var escaped = false
        for (char in text) {
            if (inString) {
                output.append(char)
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
            } else {
                when {
                    char == '"' -> { inString = true; output.append(char) }
                    char.isWhitespace() -> Unit
                    else -> output.append(char)
                }
            }
        }
        return output.toString()
    }
}
