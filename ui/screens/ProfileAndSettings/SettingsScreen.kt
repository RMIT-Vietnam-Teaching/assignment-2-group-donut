package com.example.ui_for_assignment2.ui.screens.ProfileAndSettings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val DarkCharcoal = Color(0xFF1E1E1E)
private val OffWhite = Color(0xFFFAFAFA)
private val SafetyYellow = Color(0xFFFFD700)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFE53935)
private val StatusOrange = Color(0xFFFB8C00)

@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Theme Section ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OffWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleSmall
                    )

                    var darkMode by rememberSaveable { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Dark Mode")
                            Text(
                                text = "Toggle app appearance (placeholder)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { darkMode = it } // placeholder only
                        )
                    }
                }
            }

            // --- App Info Section ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "App Info",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Version")
                        Text(text = "1.0.0") // placeholder
                    }

                    Divider()

                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "This is placeholder text for privacy details. Your data usage policy, permissions, and disclosures would appear here.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "Settings are placeholders only",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Preview(name = "Settings - Light", showBackground = true)
@Composable
private fun PreviewSettingsLight() {
    MaterialTheme { SettingsScreen() }
}

@Preview(
    name = "Settings - Dark",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun PreviewSettingsDark() {
    MaterialTheme { SettingsScreen() }
}
