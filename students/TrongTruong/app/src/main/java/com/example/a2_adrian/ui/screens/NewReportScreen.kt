package com.example.a2_adrian.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.a2_adrian.ui.theme.BgDark
import com.example.a2_adrian.ui.theme.BorderDark
import com.example.a2_adrian.ui.theme.InputBgDark
import com.example.a2_adrian.ui.theme.OffWhite
import com.example.a2_adrian.ui.theme.SafetyYellow
import com.example.a2_adrian.ui.theme.StatusGreen
import com.example.a2_adrian.ui.theme.StatusOrange
import com.example.a2_adrian.ui.theme.StatusRed
import com.example.a2_adrian.ui.theme.SurfaceDark
import com.example.a2_adrian.ui.theme.SurfaceDarkHigh
import com.example.a2_adrian.ui.theme.TextSecondary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(
    onSubmit: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Passed") }
    var inspectionType by remember { mutableStateOf("Electrical") }
    val inspectionOptions = listOf(
        "Electrical", "Fire Safety", "Structural", "Food Hygiene", "Environmental"
    )
    var expanded by remember { mutableStateOf(false) }


    Scaffold(
        containerColor = BgDark,
        topBar = {
            val unreadCount = com.example.a2_adrian.data.FakeRepository.reports.size

            TopAppBar(
                title = { Text("New inspection") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = OffWhite
                ),
                actions = {
                    IconButton(onClick = onOpenNotifications, modifier = Modifier.size(48.dp)) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFF3B82F6)
                                    ) {
                                        Text(
                                            unreadCount.toString(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = OffWhite,
                                modifier = Modifier.size(28.dp) // tăng/giảm kích thước icon tại đây
                            )
                        }
                    }
                }
            )

        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Inspector: Jane Doe / INSP-00123",
                style = MaterialTheme.typography.titleSmall, color = OffWhite)

            // INPUTS – nền tối, viền xám đậm, focus vàng
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

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, placeholder = { Text("Enter title") },
                singleLine = true, colors = tfColors,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, placeholder = { Text("Enter notes") },
                minLines = 5, colors = tfColors, modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = score, onValueChange = { score = it },
                    label = { Text("Score") }, placeholder = { Text("Enter score") },
                    singleLine = true, colors = tfColors, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = outcome, onValueChange = { outcome = it },
                    label = { Text("Outcome") }, placeholder = { Text("Outcome") },
                    singleLine = true, colors = tfColors, modifier = Modifier.weight(1f)
                )
            }


            // Inspection Type – REAL DROPDOWN
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = inspectionType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Inspection Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = tfColors,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = SurfaceDarkHigh
                ) {
                    inspectionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = OffWhite) },
                            onClick = {
                                inspectionType = option
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }


            Text("Attach media", style = MaterialTheme.typography.titleSmall, color = OffWhite)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    Box(
                        Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDarkHigh),
                        contentAlignment = Alignment.Center
                    ) { Text("📷") }
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, BorderDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("+", color = OffWhite) }
            }

            Text("Status", style = MaterialTheme.typography.titleSmall, color = OffWhite)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("Passed", status == "Passed") { status = "Passed" }
                StatusChip("Failed", status == "Failed") { status = "Failed" }
                StatusChip("Needs",  status == "Needs")  { status = "Needs" }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                // SAVE = vàng
                Button(
                    onClick = { /* Save */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafetyYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("SAVE") }

                // SUBMIT = tối
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceDarkHigh,
                        contentColor = OffWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("SUBMIT") }
            }
        }
    }
}

@Composable
private fun StatusChip(item: String, selected: Boolean, onClick: () -> Unit) {
    val color = when (item) {
        "Passed" -> StatusGreen
        "Failed" -> StatusRed
        else     -> StatusOrange
    }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(item) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = Color.White,
            containerColor = SurfaceDarkHigh,
            labelColor = if (selected) Color.White else color
        ),
        border = if (selected) null else BorderStroke(1.dp, color)
    )
}
