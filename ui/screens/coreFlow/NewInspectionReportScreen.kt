package com.example.ui_for_assignment2.ui.screens.coreFlow


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign

private val DarkCharcoal = Color(0xFF1E1E1E)
private val OffWhite = Color(0xFFFAFAFA)
private val SafetyYellow = Color(0xFFFFD700)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFE53935)
private val StatusOrange = Color(0xFFFB8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionReportScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Report") }
            )
        }
    ) { innerPadding ->
        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Inspection Type (fake dropdown) ---
            var expanded by remember { mutableStateOf(false) }
            var selectedType by remember { mutableStateOf("Select inspection type") }
            val options = listOf("Safety", "Quality", "Fire", "Equipment")

            Text(
                text = "Inspection Type",
                style = MaterialTheme.typography.labelLarge
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedType,
                    onValueChange = { /* read-only for placeholder */ },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedType = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- Inspector info (static placeholder text) ---
            Text(
                text = "Inspector: Jane Doe / INSP-00123",
                style = MaterialTheme.typography.bodyMedium
            )

            Divider()

            // --- Form fields (placeholders) ---
            var title by remember { mutableStateOf("") }
            var notes by remember { mutableStateOf("") }
            var score by remember { mutableStateOf("") }
            var outcome by remember { mutableStateOf("") }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g., Warehouse A – Safety Audit") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Observations, issues found, recommendations…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 4,
                maxLines = 8
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Score") },
                    placeholder = { Text("0–100") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = outcome,
                    onValueChange = { outcome = it },
                    label = { Text("Outcome") },
                    placeholder = { Text("e.g., Pass with notes") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // --- Attach media (placeholder buttons) ---
            Text(
                text = "Attach Media",
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* placeholder */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Camera") }

                OutlinedButton(
                    onClick = { /* placeholder */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Gallery") }

                OutlinedButton(
                    onClick = { /* placeholder */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Video") }
            }

            Text(
                text = "GPS will be captured automatically",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Divider()

            // --- Status selector (FilterChips) ---
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelLarge
            )

            var passed by remember { mutableStateOf(true) }          // default selection (placeholder)
            var failed by remember { mutableStateOf(false) }
            var needsAttention by remember { mutableStateOf(false) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = passed,
                    onClick = {
                        passed = true; failed = false; needsAttention = false
                    },
                    label = { Text("Passed") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = failed,
                    onClick = {
                        passed = false; failed = true; needsAttention = false
                    },
                    label = { Text("Failed") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusRed,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = needsAttention,
                    onClick = {
                        passed = false; failed = false; needsAttention = true
                    },
                    label = { Text("Needs Attention") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // --- Bottom actions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* placeholder */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Draft")
                }
                Button(
                    onClick = { /* placeholder */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafetyYellow,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Submit")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "This screen is a UI placeholder only.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "New Report - Light", showBackground = true)
@Composable
private fun PreviewNewInspectionReportLight() {
    MaterialTheme { NewInspectionReportScreen() }
}

@Preview(
    name = "New Report - Dark",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun PreviewNewInspectionReportDark() {
    MaterialTheme { NewInspectionReportScreen() }
}
