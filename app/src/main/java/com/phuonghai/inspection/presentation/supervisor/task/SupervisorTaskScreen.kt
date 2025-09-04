package com.phuonghai.inspection.presentation.supervisor.task
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorTaskScreen(
    viewModel: SupervisorTaskViewModel = hiltViewModel()
) {


    val inspectors by viewModel.inspectors.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val error by viewModel.error.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }
    var selectedInspectorId by remember { mutableStateOf("") }
    var selectedInspectorName by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }
    var selectedBranchName by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Timestamp?>(null) }

    val priorities = listOf("HIGH", "NORMAL", "LOW")

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    val success by viewModel.success.collectAsState()

    LaunchedEffect(success) {
        if (success == true) {
            Toast.makeText(context, "Task assigned successfully!", Toast.LENGTH_SHORT).show()

            // reset fields
            title = ""
            description = ""
            priority = "NORMAL"
            selectedInspectorName = ""
            selectedBranchName = ""
            dueDate = null

            viewModel.resetSuccess()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 60.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Assign New Task", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Task Description") },
            modifier = Modifier.fillMaxWidth()
        )

        // Priority Dropdown
        var expandedPriority by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedPriority,
            onExpandedChange = { expandedPriority = !expandedPriority }
        ) {
            OutlinedTextField(
                value = priority,
                onValueChange = {},
                readOnly = true,
                label = { Text("Priority") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPriority,
                onDismissRequest = { expandedPriority = false }
            ) {
                priorities.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            priority = it
                            expandedPriority = false
                        }
                    )
                }
            }
        }

        // Inspector Dropdown
        var expandedInspector by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedInspector,
            onExpandedChange = { expandedInspector = !expandedInspector }
        ) {
            OutlinedTextField(
                value = selectedInspectorName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Inspector") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedInspector,
                onDismissRequest = { expandedInspector = false }
            ) {
                inspectors.forEach {
                    DropdownMenuItem(
                        text = { Text(it.fullName) },
                        onClick = {
                            selectedInspectorId = it.uId
                            selectedInspectorName = it.fullName
                            expandedInspector = false
                        }
                    )
                }
            }
        }

        // Branch Dropdown
        var expandedBranch by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedBranch,
            onExpandedChange = { expandedBranch = !expandedBranch }
        ) {
            OutlinedTextField(
                value = selectedBranchName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Branch") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedBranch,
                onDismissRequest = { expandedBranch = false }
            ) {
                branches.forEach {
                    DropdownMenuItem(
                        text = { Text(it.branchName) },
                        onClick = {
                            selectedBranchId = it.branchId
                            selectedBranchName = it.branchName
                            expandedBranch = false
                        }
                    )
                }
            }
        }

        // Due Date Picker
        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        dueDate = Timestamp(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (dueDate == null) "Pick Due Date" else "Due: ${dateFormat.format(dueDate!!.toDate())}")
        }

        // Assign Task
        Button(
            enabled = title.isNotBlank() &&
                    description.isNotBlank() &&
                    selectedInspectorId.isNotBlank() &&
                    selectedBranchId.isNotBlank() &&
                    dueDate != null,
            onClick = {
                val task = Task(
                    taskId = UUID.randomUUID().toString(),
                    supervisorId = currentUserId,
                    inspectorId = selectedInspectorId,
                    branchId = selectedBranchId,
                    title = title,
                    description = description,
                    priority = Priority.valueOf(priority),
                    status = TaskStatus.ASSIGNED,
                    dueDate = dueDate,
                    createdAt = Timestamp.now()
                )
                viewModel.assignTask(task)

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Assign Task")
        }

        if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}
