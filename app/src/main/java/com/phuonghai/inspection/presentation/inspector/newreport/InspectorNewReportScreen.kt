package com.phuonghai.inspection.presentation.home.inspector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.*
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
fun InspectorNewReportScreen(navController: NavController) {
    val viewModel: NewReportViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf(uiState.title) }
    var notes by remember { mutableStateOf(uiState.notes) }
    var score by remember { mutableStateOf(uiState.score) }
    var status by remember { mutableStateOf(uiState.status) }
    var inspectionType by remember { mutableStateOf(uiState.inspectionType) }

    val inspectionOptions = InspectionType.entries.map { it.name.replace("_", " ").lowercase().capitalizeWords() }
    val statusOptions = AssignStatus.entries.filter { it != AssignStatus.DRAFT && it != AssignStatus.PENDING_REVIEW }
        .map { it.name.replace("_", " ").lowercase().capitalizeWords() }

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
                .padding(bottom = 70.dp)
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

            // Media attachments section
            Text("Attachments", style = MaterialTheme.typography.titleSmall, color = OffWhite)

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Add Image button
                OutlinedButton(
                    onClick = { /* TODO: open image picker */ },
                    border = BorderStroke(1.dp, SafetyYellow),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SafetyYellow,
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.Radius12)
                ) {
                    Text("Add Image")
                }

                // Add Video button
                OutlinedButton(
                    onClick = { /* TODO: open video picker */ },
                    border = BorderStroke(1.dp, SafetyYellow),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SafetyYellow,
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.Radius12)
                ) {
                    Text("Add Video")
                }
            }

            // TODO: show preview thumbnails
            // Score and Outcome row
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
                modifier = Modifier.fillMaxWidth()
            )

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
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.exposedDropdownSize(true)
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
                statusOptions.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                        rowItems.forEach { item ->
                            StatusChip(item, status == item) {
                                status = item
                                viewModel.updateStatus(item)
                            }
                        }
                    }
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.saveAsDraft() },
                    modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
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
                    modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
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
        "Needs Attention" -> StatusOrange
        else -> StatusOrange
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

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }