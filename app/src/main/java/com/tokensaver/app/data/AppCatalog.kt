package com.tokensaver.app.data

data class MonitoredApp(val packageName: String, val label: String)

object AppCatalog {
    val apps = listOf(
        MonitoredApp("com.openai.chatgpt", "ChatGPT"),
        MonitoredApp("com.anthropic.claude", "Claude"),
        MonitoredApp("com.google.android.apps.bard", "Gemini"),
        MonitoredApp("ai.perplexity.app.android", "Perplexity"),
        MonitoredApp("com.android.chrome", "Chrome"),
        MonitoredApp("org.mozilla.firefox", "Firefox"),
        MonitoredApp("com.brave.browser", "Brave")
    )
}
