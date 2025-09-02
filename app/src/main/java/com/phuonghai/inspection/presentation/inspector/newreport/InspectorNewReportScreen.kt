package com.phuonghai.inspection.presentation.home.inspector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuonghai.inspection.presentation.home.inspector.report.NewReportViewModel
import com.phuonghai.inspection.presentation.theme.*
private object Dimens {
    val ScreenPadding = 16.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Radius12 = 12.dp
    val ButtonHeight = 48.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorNewReportScreen(
    viewModel: NewReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Passed") }
    var inspectionType by remember { mutableStateOf("Electrical") }

    val inspectionOptions = listOf(
        "Electrical", "Fire Safety", "Structural",
        "Food Hygiene", "Environmental", "Machinery"
    )

    LaunchedEffect(Unit) {
        viewModel.loadInspectorInfo()
    }

    Scaffold(
        containerColor = DarkCharcoal,
        topBar = {
            TopAppBar(
                title = { Text("New Inspection", color = OffWhite) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = OffWhite
                ),
                actions = {
                    IconButton(
                        onClick = { /* Navigate to notifications */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        BadgedBox(badge = {
                            if (uiState.unreadNotifications > 0) {
                                Badge(containerColor = SafetyYellow) {
                                    Text(
                                        uiState.unreadNotifications.toString(),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = OffWhite
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(Dimens.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space12)
        ) {
            // Inspector info
            Text(
                "Inspector: ${uiState.inspectorName} / ${uiState.inspectorId}",
                style = MaterialTheme.typography.titleSmall,
                color = OffWhite
            )

            // Text field colors
            val tfColors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = InputBgDark,
                focusedContainerColor = InputBgDark,
                focusedBorderColor = SafetyYellow,
                unfocusedBorderColor = BorderDark,
                cursorColor = SafetyYellow,
                focusedLabelColor = OffWhite,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = OffWhite,
                unfocusedTextColor = OffWhite,
                focusedPlaceholderColor = TextSecondary,
                unfocusedPlaceholderColor = TextSecondary
            )

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    viewModel.updateTitle(it)
                },
                label = { Text("Title") },
                placeholder = { Text("Enter inspection title") },
                singleLine = true,
                colors = tfColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    viewModel.updateNotes(it)
                },
                label = { Text("Notes") },
                placeholder = { Text("Enter detailed notes") },
                minLines = 5,
                colors = tfColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Score and Outcome row
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
                OutlinedTextField(
                    value = score,
                    onValueChange = {
                        score = it
                        viewModel.updateScore(it)
                    },
                    label = { Text("Score") },
                    placeholder = { Text("0-100") },
                    singleLine = true,
                    colors = tfColors,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = outcome,
                    onValueChange = {
                        outcome = it
                        viewModel.updateOutcome(it)
                    },
                    label = { Text("Outcome") },
                    placeholder = { Text("Result") },
                    singleLine = true,
                    colors = tfColors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Inspection Type Dropdown
            var menuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = !menuExpanded }
            ) {
                OutlinedTextField(
                    value = inspectionType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Inspection Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuExpanded) },
                    colors = tfColors,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    inspectionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = OffWhite) },
                            onClick = {
                                inspectionType = option
                                viewModel.updateInspectionType(option)
                                menuExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // Status selection
            Text("Status", style = MaterialTheme.typography.titleSmall, color = OffWhite)

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                    StatusChip("Passed", status == "Passed") {
                        status = "Passed"
                        viewModel.updateStatus("Passed")
                    }
                    StatusChip("Failed", status == "Failed") {
                        status = "Failed"
                        viewModel.updateStatus("Failed")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                    StatusChip("Pending", status == "Pending") {
                        status = "Pending"
                        viewModel.updateStatus("Pending")
                    }
                    StatusChip("Needs Attention", status == "Needs Attention") {
                        status = "Needs Attention"
                        viewModel.updateStatus("Needs Attention")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space16))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.saveAsDraft() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafetyYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(Dimens.Radius12),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading && uiState.actionType == "save") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black
                        )
                    } else {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.submitReport() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceDarkHigh,
                        contentColor = OffWhite
                    ),
                    shape = RoundedCornerShape(Dimens.Radius12),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading && uiState.actionType == "submit") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OffWhite
                        )
                    } else {
                        Text("SUBMIT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Show success/error messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusChip(
    item: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = when (item) {
        "Passed" -> StatusGreen
        "Failed" -> SafetyRed
        "Pending" -> StatusOrange
        else -> StatusOrange // "Needs Attention"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(item, fontWeight = FontWeight.Medium) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = Color.White,
            containerColor = SurfaceDarkHigh,
            labelColor = if (selected) Color.White else color
        ),
        border = if (!selected) BorderStroke(1.dp, color) else null
    )
}