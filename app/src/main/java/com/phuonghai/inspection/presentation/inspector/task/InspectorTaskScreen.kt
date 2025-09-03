package com.phuonghai.inspection.presentation.inspector.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.phuonghai.inspection.presentation.generalUI.ButtonUI
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.theme.SafetyYellow

// Example task model
data class Task(
    val taskId: String,
    val title: String,
    val description: String,
    val branch: String,
    val priority: String,
    val dueDate: String,
)

@Composable
fun InspectorTaskScreen(
   navController: NavController
) {
    var  tasks: List<Task> = sampleTasks() // You can pass real data from ViewModel later
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("All") }
    var selectedDueDate by remember { mutableStateOf("All") }

    val priorities = listOf("All", "HIGH", "NORMAL", "LOW")
    val dueDates = listOf("All", "Today", "This Week", "This Month")

    // Filter logic
    val filteredTasks = tasks.filter { task ->
        (searchQuery.isEmpty() || task.title.contains(searchQuery, ignoreCase = true)) &&
                (selectedPriority == "All" || task.priority == selectedPriority) &&
                (selectedDueDate == "All" || matchesDueDate(task.dueDate, selectedDueDate))
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .padding(top = 70.dp)) {
        Text("Tasks", fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search tasks") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters Row
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Priority Dropdown
            DropdownFilter(
                label = "Priority",
                options = priorities,
                selectedOption = selectedPriority,
                onOptionSelected = { selectedPriority = it }
            )

            // Due Date Dropdown
            DropdownFilter(
                label = "Due Date",
                options = dueDates,
                selectedOption = selectedDueDate,
                onOptionSelected = { selectedDueDate = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleTasks()) { task ->
                TaskCard(
                    task = task,
                    supervisorName = "John Smith" ,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun DropdownFilter(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            border = BorderStroke(1.dp, SafetyYellow), // Safety yellow
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            Text("$label: $selectedOption", fontSize = 15.sp, color = Color.White)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Composable
fun TaskCard(
    task: Task,
    supervisorName: String,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Priority Tag (top-right corner)
            Surface(
                color = when (task.priority) {
                    "HIGH" -> MaterialTheme.colorScheme.error
                    "NORMAL" -> MaterialTheme.colorScheme.primary
                    "LOW" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = MaterialTheme.shapes.small,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = task.priority,
                    fontSize = 14.sp,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Card Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Description: ${task.description}", style = MaterialTheme.typography.bodyMedium, fontSize = 18.sp)
                Text("Branch: ${task.branch}", style = MaterialTheme.typography.bodySmall, fontSize = 18.sp)
                Text("Supervisor: $supervisorName", style = MaterialTheme.typography.bodySmall, fontSize = 18.sp)
                Text("Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall, fontSize = 16.sp)
            }

            // New report Tag (bottom-right corner)
            Surface(
                shape = MaterialTheme.shapes.small,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.InspectorNewReportScreen.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ){
                    Text(
                        text = "New Report",
                        fontSize = 14.sp,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
// Fake data
fun sampleTasks() = listOf(
    Task("1", "Check electrical panel", "Inspect wiring at branch A","Thu dau 2" , "HIGH", "Today"),
    Task("2", "Water pipe check", "Check leaks at branch B","Thu dau 1", "NORMAL", "This Week"),
    Task("3", "Safety audit", "Full audit at branch C","Thu dau 2" ,"LOW", "This Month"),
    Task("3", "Safety audit", "Full audit at branch C", "Thu dau 2" ,"LOW", "This Month"),
    Task("3", "Safety audit", "Full audit at branch C", "Thu dau 2" ,"LOW", "This Month"),
    Task("3", "Safety audit", "Full audit at branch C", "Thu dau 2" ,"LOW", "This Month"),
)

// Dummy due-date filter
fun matchesDueDate(taskDue: String, filter: String): Boolean {
    return filter == "All" || taskDue == filter
}
