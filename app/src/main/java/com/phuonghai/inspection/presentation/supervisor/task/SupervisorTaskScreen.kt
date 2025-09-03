package com.phuonghai.inspection.presentation.supervisor.task

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorTaskScreen(

) {
    val inspectors = listOf("Inspector A", "Inspector B", "Inspector C")
    val branches = listOf("Branch X", "Branch Y", "Branch Z")

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }
    var selectedInspector by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    val priorities = listOf("HIGH", "NORMAL", "LOW")

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 60.dp),
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
                value = selectedInspector,
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
                        text = { Text(it) },
                        onClick = {
                            selectedInspector = it
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
                value = selectedBranch,
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
                        text = { Text(it) },
                        onClick = {
                            selectedBranch = it
                            expandedBranch = false
                        }
                    )
                }
            }
        }

        // Due Date Picker
        Button(
            onClick = {
//                DatePickerDialog(
//                    LocalContext.current,
//                    { _, year, month, dayOfMonth ->
//                        calendar.set(year, month, dayOfMonth)
//                        dueDate = dateFormat.format(calendar.time)
//                    },
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH)
//                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (dueDate.isEmpty()) "Pick Due Date" else "Due: $dueDate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
//                if (title.isNotBlank() && selectedInspector.isNotBlank() && selectedBranch.isNotBlank()) {
//                    onAssignTask(title, description, priority, selectedInspector, selectedBranch, dueDate)
//                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Assign Task")
        }
    }
}
