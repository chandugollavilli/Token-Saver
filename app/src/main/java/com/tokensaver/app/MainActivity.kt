package com.tokensaver.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tokensaver.app.data.TokenStatsRepository
import com.tokensaver.app.service.TokenSaverAccessibilityService
import com.tokensaver.app.ui.AppWhitelistScreen
import com.tokensaver.app.ui.DashboardScreen

class MainActivity : ComponentActivity() {
    private lateinit var repository: TokenStatsRepository
    private var serviceEnabled by mutableStateOf(false)
    private var enabled by mutableStateOf(false)
    private var selectedPackages by mutableStateOf(emptySet<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TokenStatsRepository(this)
        refreshSettings()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val stats by repository.stats.collectAsState()
                var selectedPage by androidx.compose.runtime.remember { mutableStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(selectedPage == 0, { selectedPage = 0 }, icon = {}, label = { Text("Dashboard") })
                            NavigationBarItem(selectedPage == 1, { selectedPage = 1 }, icon = {}, label = { Text("Apps") })
                        }
                    }
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        if (selectedPage == 0) {
                            DashboardScreen(
                                enabled = enabled,
                                serviceEnabled = serviceEnabled,
                                stats = stats,
                                onEnabledChange = { value ->
                                    enabled = value
                                    repository.setEnabled(value)
                                    if (value && !serviceEnabled) startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                },
                                onOpenAccessibilitySettings = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                            )
                        } else {
                            AppWhitelistScreen(selectedPackages) { packageName, selected ->
                                repository.setWhitelisted(packageName, selected)
                                selectedPackages = repository.whitelistedPackages()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) refreshSettings()
    }

    private fun refreshSettings() {
        enabled = repository.isEnabled()
        selectedPackages = repository.whitelistedPackages()
        val component = ComponentName(this, TokenSaverAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        serviceEnabled = enabledServices.split(':').any { it.equals(component, ignoreCase = true) }
    }
}
