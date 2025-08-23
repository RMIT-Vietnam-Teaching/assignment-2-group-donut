package com.example.supervisor_ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterButton(
    label: String,
    isActive: Boolean,
    dropdownOptions: List<String>,
    selectedOptions: Set<String>,
    onOptionsSelected: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        if (isActive) {
            FilledTonalButton(
                onClick = { expanded = true },
                modifier = Modifier.menuAnchor().height(48.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)

                )
            }
        } else {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.menuAnchor().height(48.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            dropdownOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = option in selectedOptions,
                                onCheckedChange = null // Handled by the parent
                            )
                            Text(
                                text = option,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    onClick = {
                        val newSelection = selectedOptions.toMutableSet()
                        if (option in newSelection) {
                            newSelection.remove(option)
                        } else {
                            newSelection.add(option)
                        }
                        onOptionsSelected(newSelection)
                    }
                )
            }
        }
    }
}