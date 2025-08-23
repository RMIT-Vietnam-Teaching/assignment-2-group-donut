package com.example.supervisor_ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.supervisor_ui.data.InspectionStatus
import com.example.supervisor_ui.data.MockData
import com.example.supervisor_ui.components.BottomNavigation
import com.example.supervisor_ui.components.FilterButton
import com.example.supervisor_ui.components.ItemCard
import com.example.supervisor_ui.data.InspectionAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorPendingScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var searchText by remember { mutableStateOf("") }
    var activeScreen by remember { mutableStateOf("history") }
    var selectedTypeFilter by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedStatusFilter by remember { mutableStateOf<Set<InspectionStatus>>(emptySet()) }
    var selectedDateFilter by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedLocationFilter by remember { mutableStateOf<Set<String>>(emptySet()) }

    val statusDisplayMap = mapOf(
        InspectionStatus.PENDING_REVIEW to "Pending Review",
        InspectionStatus.PASSED to "Passed",
        InspectionStatus.FAILED to "Failed",
        InspectionStatus.NEEDS_ATTENTION to "Needs Attention"
    )
    val filteredItems by remember(
        searchText,
        selectedTypeFilter,
        selectedStatusFilter,
        selectedDateFilter,
        selectedLocationFilter
    ) {
        derivedStateOf {
            MockData.inspectionItems.filter { item ->
                val matchesSearch = searchText.isEmpty() ||
                        item.title.contains(searchText, ignoreCase = true) ||
                        item.description.contains(searchText, ignoreCase = true)
                val matchesType = selectedTypeFilter.isEmpty() || selectedTypeFilter.contains(item.type)
                val matchesStatus = selectedStatusFilter.isEmpty() || selectedStatusFilter.contains(item.status)
                val matchesDate = selectedDateFilter.isEmpty() ||
                        selectedDateFilter.contains("All Dates") ||
                        selectedDateFilter.contains(item.date)
                val matchesLocation = selectedLocationFilter.isEmpty() || selectedLocationFilter.contains(item.location)
                val isPendingAction = item.action == InspectionAction.NONE

                matchesSearch && matchesType && matchesStatus && matchesDate && matchesLocation && isPendingAction
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                currentRoute = currentRoute,
                activeScreen = activeScreen,
                onScreenChange = { activeScreen = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search inspections...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                FilterButton(
                    label = "Type",
                    isActive = selectedTypeFilter.isNotEmpty(),
                    dropdownOptions = listOf("Electrical", "Fire", "Safety", "Structural"),
                    selectedOptions = selectedTypeFilter,
                    onOptionsSelected = { selectedTypeFilter = it }
                )


                FilterButton(
                    label = "Status",
                    isActive = selectedStatusFilter.isNotEmpty(),
                    dropdownOptions = statusDisplayMap.values.toList(),
                    selectedOptions = selectedStatusFilter.map { statusDisplayMap[it]!! }.toSet(),
                    onOptionsSelected = { selectedStrings ->
                        selectedStatusFilter = statusDisplayMap.filterValues { it in selectedStrings }.keys.toSet()
                    }
                )


                FilterButton(
                    label = "Date",
                    isActive = selectedDateFilter.isNotEmpty(),
                    dropdownOptions = listOf("All Dates", "August 15", "August 18", "August 12", "August 25"),
                    selectedOptions = selectedDateFilter,
                    onOptionsSelected = { selectedDateFilter = it }
                )


                FilterButton(
                    label = "Location",
                    isActive = selectedLocationFilter.isNotEmpty(),
                    dropdownOptions = listOf("Main Building", "All Floors", "Warehouse", "Building Foundation"),
                    selectedOptions = selectedLocationFilter,
                    onOptionsSelected = { selectedLocationFilter = it }
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredItems) { item ->
                    ItemCard(item = item)
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No inspections found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}