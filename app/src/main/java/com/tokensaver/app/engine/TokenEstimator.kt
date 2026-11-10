package com.tokensaver.app.engine

object TokenEstimator {
    private val pieces = Regex("[\\p{L}\\p{N}_]+|[^\\s\\p{L}\\p{N}_]")

    fun estimate(text: String): Int {
        if (text.isBlank()) return 0
        return pieces.findAll(text).sumOf { match ->
            val piece = match.value
            if (piece.codePoints().anyMatch { Character.UnicodeScript.of(it) in setOf(Character.UnicodeScript.HAN, Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA, Character.UnicodeScript.HANGUL) }) {
                piece.codePointCount(0, piece.length)
            } else if (piece.firstOrNull()?.isLetterOrDigit() == true || piece.startsWith("_")) {
                maxOf(1, (piece.length + 3) / 4)
            } else {
                1
            }
        }
    }
}
