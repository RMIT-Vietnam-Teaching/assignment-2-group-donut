package com.phuonghai.inspection.presentation.home.inspector

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.phuonghai.inspection.presentation.theme.BorderDark
import com.phuonghai.inspection.presentation.theme.DarkCharcoal
import com.phuonghai.inspection.presentation.theme.InputBgDark
import com.phuonghai.inspection.presentation.theme.OffWhite
import com.phuonghai.inspection.presentation.theme.SafetyRed
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import com.phuonghai.inspection.presentation.theme.StatusBlue
import com.phuonghai.inspection.presentation.theme.StatusGray
import com.phuonghai.inspection.presentation.theme.StatusGreen
import com.phuonghai.inspection.presentation.theme.StatusOrange
import com.phuonghai.inspection.presentation.theme.SurfaceDark
import com.phuonghai.inspection.presentation.theme.SurfaceDarkHigh
import com.phuonghai.inspection.presentation.theme.TextSecondary

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

    // Form states
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InspectionType.ELECTRICAL) }
    var selectedStatus by remember { mutableStateOf(AssignStatus.PASSED) }
    var selectedPriority by remember { mutableStateOf(Priority.NORMAL) }
    var address by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideo by remember { mutableStateOf<Uri?>(null) }

    // Validation states
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var scoreError by remember { mutableStateOf<String?>(null) }

    // Load draft data nếu có reportId
    LaunchedEffect(reportId) {
        if (!reportId.isNullOrBlank()) {
            viewModel.loadDraftReport(reportId)
        }
    }

    // Set taskId nếu có
    LaunchedEffect(taskId) {
        if (!taskId.isNullOrBlank()) {
            viewModel.setTaskId(taskId)
        }
    }

    // Media pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris.take(5) // Limit to 5 images
            if (uris.size > 5) {
                Toast.makeText(context, "Maximum 5 images allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedVideo = it }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadInspectorInfo()
    }
    // Populate form fields when draft data is loaded
    LaunchedEffect(reportId, taskId) {
        Log.d("NewReportScreen", "LaunchedEffect: reportId=$reportId, taskId=$taskId")

        when {
            !reportId.isNullOrBlank() -> {
                // Có reportId → load draft report
                Log.d("NewReportScreen", "Loading draft report: $reportId")
                viewModel.loadDraftReport(reportId)
            }
            !taskId.isNullOrBlank() -> {
                // Có taskId → set taskId và check xem có draft không
                Log.d("NewReportScreen", "Setting taskId and checking for draft: $taskId")
                viewModel.setTaskId(taskId)
                viewModel.loadDraftByTaskId(taskId) // ✅ SỬ DỤNG PHƯƠNG THỨC MỚI
            }
            else -> {
                Log.d("NewReportScreen", "No reportId or taskId provided")
            }
        }
    }

    //POPULATE FORM FIELDS
    LaunchedEffect(uiState.draftData) {
        uiState.draftData?.let { report ->
            Log.d("NewReportScreen", "Populating form with draft data: ${report.title}")

            title = report.title
            description = report.description
            score = report.score?.toString() ?: ""
            selectedType = report.type
            selectedStatus = report.assignStatus
            selectedPriority = report.priority
            address = report.address

            // ✅ LOAD IMAGES VÀ VIDEO NẾU CÓ URL
            if (report.imageUrl.isNotBlank()) {
                // TODO: Convert URL back to Uri if needed for display
                Log.d("NewReportScreen", "Draft has image: ${report.imageUrl}")
            }
            if (report.videoUrl.isNotBlank()) {
                // TODO: Convert URL back to Uri if needed for display
                Log.d("NewReportScreen", "Draft has video: ${report.videoUrl}")
            }
        }
    }
    // ✅ HANDLE SUCCESS MESSAGE VÀ NAVIGATION
    LaunchedEffect(uiState.message, uiState.shouldNavigateBack) {
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

            if (message.contains("successfully", ignoreCase = true) && uiState.shouldNavigateBack) {
                kotlinx.coroutines.delay(1000)
                viewModel.clearNavigationFlag()

                // ✅ SET FLAG để TaskScreen refresh
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("should_refresh_tasks", true)

                navController.popBackStack()
            }

            viewModel.clearMessage()
        }
    }
    // Form validation
    fun validateForm(): Boolean {
        titleError = when {
            title.isBlank() -> "Title is required"
            title.length < 3 -> "Title must be at least 3 characters"
            else -> null
        }

        descriptionError = when {
            description.isBlank() -> "Description is required"
            description.length < 10 -> "Description must be at least 10 characters"
            else -> null
        }

        scoreError = when {
            score.isBlank() -> "Score is required"
            score.toIntOrNull() == null -> "Score must be a number"
            score.toInt() !in 0..100 -> "Score must be between 0-100"
            else -> null
        }

        return titleError == null && descriptionError == null && scoreError == null
    }

    Scaffold(
        containerColor = DarkCharcoal,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Inspection Report",
                        color = OffWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OffWhite
                        )
                    }
                },
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
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.ScreenPadding)
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16)
        ) {
            // Inspector info card
            InspectorInfoCard(uiState = uiState)

            // Basic Information Section
            BasicInformationSection(
                title = title,
                onTitleChange = {
                    title = it
                    titleError = null
                },
                titleError = titleError,
                description = description,
                onDescriptionChange = {
                    description = it
                    descriptionError = null
                },
                descriptionError = descriptionError,
                address = address,
                onAddressChange = { address = it }
            )

            // Assessment Section
            AssessmentSection(
                score = score,
                onScoreChange = {
                    score = it
                    scoreError = null
                },
                scoreError = scoreError,
                selectedType = selectedType,
                onTypeChange = { selectedType = it },
                selectedStatus = selectedStatus,
                onStatusChange = { selectedStatus = it },
                selectedPriority = selectedPriority,
                onPriorityChange = { selectedPriority = it }
            )

            // Media Attachments Section
            MediaAttachmentsSection(
                selectedImages = selectedImages,
                selectedVideo = selectedVideo,
                onAddImages = { imagePickerLauncher.launch("image/*") },
                onAddVideo = { videoPickerLauncher.launch("video/*") },
                onRemoveImage = { uriToRemove ->
                    selectedImages = selectedImages.filter { it != uriToRemove }
                },
                onRemoveVideo = { selectedVideo = null }
            )

            // Action Buttons
            ActionButtonsSection(
                isLoading = uiState.isLoading,
                actionType = uiState.actionType,
                onSaveAsDraft = {
                    if (title.isNotBlank() || description.isNotBlank()) {
                        viewModel.saveAsDraft(
                            title = title,
                            description = description,
                            score = score.toIntOrNull(),
                            type = selectedType,
                            status = AssignStatus.DRAFT,
                            priority = selectedPriority,
                            address = address,
                            imageUris = selectedImages,
                            videoUri = selectedVideo,
                            taskId = uiState.currentTaskId.ifBlank { taskId ?: "" }
                        )
                    } else {
                        Toast.makeText(context, "Please enter at least title or description", Toast.LENGTH_SHORT).show()
                    }
                },
                onSubmitReport = {
                    // 1) Không cho submit nếu còn DRAFT
                    if (selectedStatus == AssignStatus.DRAFT) {
                        Toast.makeText(context, "Please choose a non-draft status before submitting", Toast.LENGTH_SHORT).show()
                        return@ActionButtonsSection
                    }

                    // 2) Validate form
                    if (!validateForm()) {
                        return@ActionButtonsSection
                    }

                    // 3) Submit
                    viewModel.submitReport(
                        title = title,
                        description = description,
                        score = score.toInt(),
                        type = selectedType,
                        status = selectedStatus,
                        priority = selectedPriority,
                        address = address,
                        imageUris = selectedImages,
                        videoUri = selectedVideo
                    )
                }
            )
        }
    }
}
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

    Text(
        "Basic Information",
        color = SafetyYellow,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

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
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = TextSecondary
            )
        },
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

    Text(
        "Assessment",
        color = SafetyYellow,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    OutlinedTextField(
        value = score,
        onValueChange = { input ->
            if (input.isEmpty() || (input.toIntOrNull() != null && input.toInt() in 0..100)) {
                onScoreChange(input)
            }
        },
        label = { Text("Score (0-100) *") },
        placeholder = { Text("Enter score") },
        singleLine = true,
        colors = textFieldColors,
        modifier = Modifier.fillMaxWidth(),
        isError = scoreError != null,
        supportingText = scoreError?.let { { Text(it, color = SafetyRed) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    )

    // Inspection Type Dropdown
    var typeExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = typeExpanded,
        onExpandedChange = { typeExpanded = !typeExpanded }
    ) {
        OutlinedTextField(
            value = selectedType.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = { Text("Inspection Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
            colors = textFieldColors,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = typeExpanded,
            onDismissRequest = { typeExpanded = false }
        ) {
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

    // Status Selection
    Text("Assessment Result", color = OffWhite, fontSize = 16.sp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(
            AssignStatus.PASSED to StatusGreen,
            AssignStatus.FAILED to SafetyRed,
            AssignStatus.NEEDS_ATTENTION to StatusOrange
        ).forEach { (status, color) ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusChange(status) },
                label = {
                    Text(
                        status.name.replace("_", " "),
                        fontWeight = FontWeight.Medium
                    )
                },
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

    // Priority Selection
    Text("Priority Level", color = OffWhite, fontSize = 16.sp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(
            Priority.HIGH to SafetyRed,
            Priority.NORMAL to StatusBlue,
            Priority.LOW to StatusGray
        ).forEach { (priority, color) ->
            FilterChip(
                selected = selectedPriority == priority,
                onClick = { onPriorityChange(priority) },
                label = {
                    Text(
                        priority.name,
                        fontWeight = FontWeight.Medium
                    )
                },
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
    Text(
        "Media Attachments",
        color = SafetyYellow,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = onAddImages,
            border = BorderStroke(1.dp, SafetyYellow),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SafetyYellow,
                containerColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.Radius12)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.Space8))
            Text(if (selectedImages.isNotEmpty()) "Replace Images" else "Add Images")
        }

        OutlinedButton(
            onClick = onAddVideo,
            border = BorderStroke(1.dp, SafetyYellow),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SafetyYellow,
                containerColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.Radius12)
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.Space8))
            Text(if (selectedVideo != null) "Replace Video" else "Add Video")
        }
    }

    Spacer(modifier = Modifier.height(Dimens.Space8))

    // Display selected images
    if (selectedImages.isNotEmpty()) {
        Text("Selected Images:", color = TextSecondary, fontSize = 14.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)
        ) {
            items(selectedImages) { uri ->
                Box {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimens.AttachmentSize)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    IconButton(
                        onClick = { onRemoveImage(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Display selected video
    selectedVideo?.let { uri ->
        Text("Selected Video:", color = TextSecondary, fontSize = 14.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .padding(Dimens.Space12)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SafetyYellow
                    )
                    Spacer(modifier = Modifier.width(Dimens.Space8))
                    Text(
                        "Video Selected",
                        color = OffWhite,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onRemoveVideo) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Video",
                        tint = SafetyRed
                    )
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onSaveAsDraft,
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceDarkHigh,
                contentColor = OffWhite
            ),
            shape = RoundedCornerShape(Dimens.Radius12),
            enabled = !isLoading
        ) {
            if (isLoading && actionType == "save") {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = OffWhite
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.Space8))
                Text("SAVE DRAFT", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onSubmitReport,
            modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = SafetyYellow,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(Dimens.Radius12),
            enabled = !isLoading
        ) {
            if (isLoading && actionType == "submit") {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.Space8))
                Text("SUBMIT", fontWeight = FontWeight.Bold)
            }
        }
    }
}