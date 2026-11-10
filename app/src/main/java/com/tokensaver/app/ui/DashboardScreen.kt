package com.tokensaver.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokensaver.app.data.AppCatalog
import com.tokensaver.app.data.StatsSnapshot
import com.tokensaver.app.engine.PromptCompressor
import com.tokensaver.app.engine.TokenEstimator
import java.util.Locale

@Composable
fun DashboardScreen(
    enabled: Boolean,
    serviceEnabled: Boolean,
    stats: StatsSnapshot,
    onEnabledChange: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Token Saver", style = MaterialTheme.typography.headlineMedium)
            Text("An optional, on-device prompt helper", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Prompt helper", style = MaterialTheme.typography.titleMedium)
                        Text(if (enabled) "On for selected apps" else "Off")
                    }
                    Switch(checked = enabled, onCheckedChange = onEnabledChange)
                }
            }
        }
        if (enabled && !serviceEnabled) {
            item {
                Card(onClick = onOpenAccessibilitySettings, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Finish setup", style = MaterialTheme.typography.titleMedium)
                        Text("Enable Token Saver in Android Accessibility settings. Android requires you to approve this explicitly.")
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SavingsGauge(saved = stats.dailySavedTokens)
            }
        }
        item { Text("Saved today: ${stats.dailySavedTokens} estimated tokens", style = MaterialTheme.typography.titleMedium) }
        item { Text("All-time: ${stats.totalSavedTokens} estimated tokens", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { LiveTestSandbox() }
        item { Text("By app", style = MaterialTheme.typography.titleLarge) }
        items(AppCatalog.apps) { app ->
            val saved = stats.byApp[app.packageName] ?: 0
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.label)
                Text("$saved tokens")
            }
        }
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                "Text is only inspected in apps you select. Nothing is sent over the network. You review every suggested edit before it changes your text.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveTestSandbox() {
    var prompt by remember {
        mutableStateOf("Please summarize the key points from this report and, basically, explain what actions we should take next.")
    }
    var testedPrompt by remember { mutableStateOf<String?>(null) }
    val compressed = testedPrompt?.let(PromptCompressor::compress)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp).testTag("live-test-sandbox"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Live Test Sandbox", style = MaterialTheme.typography.titleLarge)
            Text("Try prompt compression here without enabling Accessibility.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Sample prompt") }
            )
            Button(onClick = { testedPrompt = prompt }, modifier = Modifier.fillMaxWidth()) {
                Text("Test Compression")
            }
            if (testedPrompt != null && compressed != null) {
                val originalTokens = TokenEstimator.estimate(testedPrompt!!)
                val compressedTokens = TokenEstimator.estimate(compressed)
                val savedTokens = originalTokens - compressedTokens
                val savedPercent = if (originalTokens == 0) 0.0 else savedTokens * 100.0 / originalTokens
                Text("Before — original text", style = MaterialTheme.typography.titleSmall)
                Text(testedPrompt!!, style = MaterialTheme.typography.bodyMedium)
                Text("Original tokens: $originalTokens (estimated)")
                Text("After — compressed text", style = MaterialTheme.typography.titleSmall)
                Text(compressed, style = MaterialTheme.typography.bodyMedium)
                Text("Compressed tokens: $compressedTokens (estimated)")
                Text(
                    "Tokens saved: $savedTokens (${String.format(Locale.US, "%.2f", savedPercent)}%)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SavingsGauge(saved: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(156.dp)) {
            val stroke = 12.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(Color(0xFFE7EAF2), radius = size.minDimension / 2 - stroke, center = center, style = Stroke(stroke))
            drawArc(
                color = Color(0xFF3157D5), startAngle = -90f, sweepAngle = if (saved > 0) 270f else 0f,
                useCenter = false,
                topLeft = Offset(stroke, stroke),
                size = androidx.compose.ui.geometry.Size(size.width - stroke * 2, size.height - stroke * 2),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text("$saved", fontSize = 24.sp)
        Text("tokens saved today", style = MaterialTheme.typography.bodySmall)
    }
}
