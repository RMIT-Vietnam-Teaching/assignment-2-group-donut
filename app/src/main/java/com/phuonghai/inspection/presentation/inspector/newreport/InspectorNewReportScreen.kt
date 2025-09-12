package com.phuonghai.inspection.presentation.home.inspector

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.AssignStatus
import com.phuonghai.inspection.domain.model.InspectionType
import com.phuonghai.inspection.presentation.home.inspector.report.NewReportViewModel
import com.phuonghai.inspection.presentation.home.inspector.report.NewReportUiState
import com.phuonghai.inspection.presentation.theme.*

private object Dimens {
    val ScreenPadding = 16.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Radius12 = 12.dp
    val ButtonHeight = 48.dp
    val AttachmentSize = 80.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorNewReportScreen(
    navController: NavController,
    taskId: String? = null,
    reportId: String? = null,
    viewModel: NewReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideo by remember { mutableStateOf<Uri?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var scoreError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris.take(5)
            uris.firstOrNull()?.let { viewModel.generateDescriptionFromImage(it) }
            if (uris.size > 5) Toast.makeText(context, "Maximum 5 images", Toast.LENGTH_SHORT).show()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedVideo = uri }

    LaunchedEffect(reportId, taskId) {
        when {
            !reportId.isNullOrBlank() -> viewModel.loadDraftReport(reportId)
            !taskId.isNullOrBlank() -> viewModel.loadDraftByTaskId(taskId)
        }
    }

    LaunchedEffect(uiState.message, uiState.shouldNavigateBack) {
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (uiState.shouldNavigateBack) {
                navController.previousBackStackEntry?.savedStateHandle?.set("should_refresh_tasks", true)
                navController.popBackStack()
                viewModel.clearNavigationFlag()
            }
            viewModel.clearMessage()
        }
    }

    fun validateForm(): Boolean {
        titleError = if (uiState.title.isBlank()) "Title is required" else null
        descriptionError = if (uiState.description.isBlank()) "Description is required" else null
        scoreError = when {
            uiState.score.isBlank() -> "Score is required"
            uiState.score.toIntOrNull() == null -> "Score must be a number"
            uiState.score.toInt() !in 0..100 -> "Score must be between 0-100"
            else -> null
        }
        return titleError == null && descriptionError == null && scoreError == null
    }

    Scaffold(
        containerColor = DarkCharcoal,
        topBar = {
            TopAppBar(
                title = { Text("New Inspection Report", color = OffWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OffWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.ScreenPadding)
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16)
        ) {
            InspectorInfoCard(uiState = uiState)

            BasicInformationSection(
                title = uiState.title,
                onTitleChange = viewModel::onTitleChange,
                titleError = titleError,
                description = uiState.description,
                onDescriptionChange = viewModel::onDescriptionChange,
                descriptionError = descriptionError,
                address = uiState.address,
                onAddressChange = viewModel::onAddressChange
            )

            AssessmentSection(
                score = uiState.score,
                onScoreChange = viewModel::onScoreChange,
                scoreError = scoreError,
                selectedType = uiState.type,
                onTypeChange = viewModel::onTypeChange,
                selectedStatus = uiState.assignStatus,
                onStatusChange = viewModel::onStatusChange,
                selectedPriority = uiState.priority,
                onPriorityChange = viewModel::onPriorityChange
            )

            MediaAttachmentsSection(
                selectedImages = selectedImages,
                selectedVideo = selectedVideo,
                onAddImages = { imagePickerLauncher.launch("image/*") },
                onAddVideo = { videoPickerLauncher.launch("video/*") },
                onRemoveImage = { uriToRemove -> selectedImages = selectedImages.filter { it != uriToRemove } },
                onRemoveVideo = { selectedVideo = null }
            )

            ActionButtonsSection(
                isLoading = uiState.isLoading,
                actionType = uiState.actionType,
                onSaveAsDraft = {
                    if (uiState.title.isNotBlank() || uiState.description.isNotBlank()) {
                        viewModel.saveAsDraft(selectedImages, selectedVideo)
                    } else {
                        Toast.makeText(context, "Please enter a title or description", Toast.LENGTH_SHORT).show()
                    }
                },
                onSubmitReport = {
                    if (uiState.assignStatus == AssignStatus.DRAFT) {
                        Toast.makeText(context, "Please choose a status other than DRAFT", Toast.LENGTH_SHORT).show()
                        return@ActionButtonsSection
                    }
                    if (validateForm()) {
                        viewModel.submitReport(selectedImages, selectedVideo)
                    }
                }
            )
        }
    }
}

// ======= CÁC COMPOSABLE BỊ THIẾU ĐÃ ĐƯỢC THÊM VÀO ĐÂY =======

@Composable
private fun InspectorInfoCard(uiState: NewReportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(Dimens.Radius12)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = SafetyYellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.Space12))
            Column {
                Text(
                    text = uiState.inspectorName.ifBlank { "Loading..." },
                    color = OffWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "ID: ${uiState.inspectorId}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun BasicInformationSection(
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String?,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: String?,
    address: String,
    onAddressChange: (String) -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = InputBgDark,
        focusedContainerColor = InputBgDark,
        focusedBorderColor = SafetyYellow,
        unfocusedBorderColor = BorderDark,
        cursorColor = SafetyYellow,
        focusedLabelColor = OffWhite,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = OffWhite,
        unfocusedTextColor = OffWhite,
        errorBorderColor = SafetyRed,
        errorLabelColor = SafetyRed,
        errorTextColor = OffWhite
    )

    Text("Basic Information", color = SafetyYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)

    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Report Title *") },
        placeholder = { Text("Enter inspection title") },
        singleLine = true,
        colors = textFieldColors,
        modifier = Modifier.fillMaxWidth(),
        isError = titleError != null,
        supportingText = titleError?.let { { Text(it, color = SafetyRed) } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )

    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description *") },
        placeholder = { Text("Enter detailed description of inspection findings") },
        minLines = 4,
        maxLines = 6,
        colors = textFieldColors,
        modifier = Modifier.fillMaxWidth(),
        isError = descriptionError != null,
        supportingText = descriptionError?.let { { Text(it, color = SafetyRed) } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )

    OutlinedTextField(
        value = address,
        onValueChange = onAddressChange,
        label = { Text("Location/Address") },
        placeholder = { Text("Enter inspection location") },
        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = TextSecondary) },
        singleLine = true,
        colors = textFieldColors,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssessmentSection(
    score: String,
    onScoreChange: (String) -> Unit,
    scoreError: String?,
    selectedType: InspectionType,
    onTypeChange: (InspectionType) -> Unit,
    selectedStatus: AssignStatus,
    onStatusChange: (AssignStatus) -> Unit,
    selectedPriority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = InputBgDark,
        focusedContainerColor = InputBgDark,
        focusedBorderColor = SafetyYellow,
        unfocusedBorderColor = BorderDark,
        cursorColor = SafetyYellow,
        focusedLabelColor = OffWhite,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = OffWhite,
        unfocusedTextColor = OffWhite,
        errorBorderColor = SafetyRed,
        errorLabelColor = SafetyRed,
        errorTextColor = OffWhite
    )

    Text("Assessment", color = SafetyYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)

    OutlinedTextField(
        value = score,
        onValueChange = onScoreChange,
        label = { Text("Score (0-100) *") },
        placeholder = { Text("Enter score") },
        singleLine = true,
        colors = textFieldColors,
        modifier = Modifier.fillMaxWidth(),
        isError = scoreError != null,
        supportingText = scoreError?.let { { Text(it, color = SafetyRed) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
    )

    var typeExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
        OutlinedTextField(
            value = selectedType.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = { Text("Inspection Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
            colors = textFieldColors,
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
            InspectionType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.replace("_", " "), color = OffWhite) },
                    onClick = {
                        onTypeChange(type)
                        typeExpanded = false
                    }
                )
            }
        }
    }

    Text("Assessment Result", color = OffWhite, fontSize = 16.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8), modifier = Modifier.fillMaxWidth()) {
        listOf(
            AssignStatus.PASSED to StatusGreen,
            AssignStatus.FAILED to SafetyRed,
            AssignStatus.NEEDS_ATTENTION to StatusOrange
        ).forEach { (status, color) ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusChange(status) },
                label = { Text(status.name.replace("_", " "), fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceDarkHigh,
                    labelColor = color
                ),
                border = if (selectedStatus != status) BorderStroke(1.dp, color) else null,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Text("Priority Level", color = OffWhite, fontSize = 16.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8), modifier = Modifier.fillMaxWidth()) {
        listOf(
            Priority.HIGH to SafetyRed,
            Priority.NORMAL to StatusBlue,
            Priority.LOW to StatusGray
        ).forEach { (priority, color) ->
            FilterChip(
                selected = selectedPriority == priority,
                onClick = { onPriorityChange(priority) },
                label = { Text(priority.name, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceDarkHigh,
                    labelColor = color
                ),
                border = if (selectedPriority != priority) BorderStroke(1.dp, color) else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MediaAttachmentsSection(
    selectedImages: List<Uri>,
    selectedVideo: Uri?,
    onAddImages: () -> Unit,
    onAddVideo: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onRemoveVideo: () -> Unit
) {
    Text("Media Attachments", color = SafetyYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space12), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onAddImages,
            border = BorderStroke(1.dp, SafetyYellow),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyYellow, containerColor = Color.Transparent),
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.Radius12)
        ) {
            Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(Dimens.Space8))
            Text(if (selectedImages.isNotEmpty()) "Replace Images" else "Add Images")
        }
        OutlinedButton(
            onClick = onAddVideo,
            border = BorderStroke(1.dp, SafetyYellow),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyYellow, containerColor = Color.Transparent),
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.Radius12)
        ) {
            Icon(Icons.Default.Videocam, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(Dimens.Space8))
            Text(if (selectedVideo != null) "Replace Video" else "Add Video")
        }
    }

    if (selectedImages.isNotEmpty()) {
        Text("Selected Images:", color = TextSecondary, fontSize = 14.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            items(selectedImages) { uri ->
                Box {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.AttachmentSize).clip(RoundedCornerShape(8.dp))
                    )
                    IconButton(
                        onClick = { onRemoveImage(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    selectedVideo?.let {
        Text("Selected Video:", color = TextSecondary, fontSize = 14.sp)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Row(
                modifier = Modifier.padding(Dimens.Space12).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = SafetyYellow)
                    Spacer(modifier = Modifier.width(Dimens.Space8))
                    Text("Video Selected", color = OffWhite, fontSize = 14.sp)
                }
                IconButton(onClick = onRemoveVideo) {
                    Icon(Icons.Default.Delete, "Remove Video", tint = SafetyRed)
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isLoading: Boolean,
    actionType: String?,
    onSaveAsDraft: () -> Unit,
    onSubmitReport: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space12), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSaveAsDraft,
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkHigh, contentColor = OffWhite),
            shape = RoundedCornerShape(Dimens.Radius12),
            enabled = !isLoading
        ) {
            if (isLoading && actionType == "save") {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OffWhite)
            } else {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(Dimens.Space8))
                Text("SAVE DRAFT", fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onSubmitReport,
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow, contentColor = Color.Black),
            shape = RoundedCornerShape(Dimens.Radius12),
            enabled = !isLoading
        ) {
            if (isLoading && actionType == "submit") {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
            } else {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(Dimens.Space8))
                Text("SUBMIT", fontWeight = FontWeight.Bold)
            }
        }
    }
}