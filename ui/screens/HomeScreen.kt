package com.example.ui_for_assignment2.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Approval
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.campuscompanion.R
import com.example.campuscompanion.domain.model.Room

val PrimaryColor = Color(0xFFFFD700)

data class Report(
    val id: String,
    @DrawableRes val painter: Int,
    val time: String,
    val location: String,
    val inspector: String,
    val type: String,
    val status: String
)

@Composable
fun HomeScreen() {

    var isSearching by remember {mutableStateOf(false)}
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val list = listOf(
        Report(
            id = "1",
            status = "Pending",
            type = "Safety Inspection",
            time = "2023-10-01",
            location = "Tan Binh Storage",
            inspector = "John Doe",
            painter = R.drawable.onboarding2,
        ),
        Report(
            id = "2",
            status = "Passed",
            type = "Fire Drill",
            time = "2023-10-02",
            location = "Binh Tan Storage",
            inspector = "Jane Smith",
            painter = R.drawable.onboarding2,
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        SummarySection()

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 30.dp),
                shape = RoundedCornerShape(24.dp),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black
                )
            )
            Box {
                OutlinedButton(
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    onClick = { typeDropdownExpanded = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(selectedType, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                DropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
//                    types.forEach { type ->
//                        DropdownMenuItem(
//                            text = { Text(type) },
//                            onClick = {
//                                selectedType = type
//                                typeDropdownExpanded = false
//                            }
//                        )
//                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(list) { report ->
                ReportCard(report = report, onClick = { /* Handle click */ })
            }
        }

    }
}

@Composable
fun SummarySection() {
    Text(
        text = "Summary",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        SummaryCard(
            icon = Icons.Outlined.Pending,
            title = "Pending Reports",
            type = "Pending",
            subtext = "5",
            count = "10"
        )
        SummaryCard(
            icon = Icons.Outlined.Approval,
            title = "Passed Reports",
            type = "Passed",
            subtext = "3",
            count = "8"
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        SummaryCard(
            icon = Icons.Outlined.Remove,
            title = "Fails Reports",
            type = "Failed",
            subtext = "5",
            count = "10"
        )
        SummaryCard(
            icon = Icons.Outlined.CenterFocusWeak,
            title = "Need Attention",
            type = "Attention",
            subtext = "3",
            count = "8"
        )
    }
}

@Composable
fun SummaryCard(icon: ImageVector, type: String, title: String, subtext:String, count: String) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF6F6) // light pink background
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top icon

            val statusColor = when (type.lowercase()) {
                "passed" -> Color(0xFF4CAF50)   // Green
                "failed" -> Color(0xFFF44336)   // Red
                "pending" -> Color(0xFFFFC107)  // Amber/Yellow
                "attention" -> Color(0xFF2196F3) // Blue
                else -> Color.Gray              // Default fallback
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(statusColor, RoundedCornerShape(8.dp)), // <-- use statusColor here
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "User Icon",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Number + text
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = count,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // Chart + new users
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fake chart placeholder
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(30.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(statusColor.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.3f))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Text(
                    text = "+ " + subtext + " new today",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Composable
fun ReportCard(
    report: Report,
    onClick: (Room) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(8.dp)
            .fillMaxWidth()
            .clickable{
                //onClick(room)
            },
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding2),
                contentDescription = report.time,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            val statusColor = when (report.status.lowercase()) {
                "passed" -> Color(0xFF4CAF50)   // Green
                "failed" -> Color(0xFFF44336)   // Red
                "pending" -> Color(0xFFFFC107)  // Amber/Yellow
                "attention" -> Color(0xFF2196F3) // Blue
                else -> Color.Gray              // Default fallback
            }

            Text(
                text = report.status, // <-- your status text
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(
                        color = statusColor, // yellow status background
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ){
                Icon(imageVector = Icons.Outlined.Book, contentDescription = null, tint = Color.Gray)
                Text(
                    report.type,
                    color = Color.Gray,
                    fontSize = 16.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ){
                Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = Color.Gray)
                Text(
                    report.inspector,
                    color = Color.Gray,
                    fontSize = 16.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ){
                Icon(imageVector = Icons.Outlined.DateRange, contentDescription = null, tint = Color.Gray)
                Text(
                    report.time,
                    color = Color.Gray,
                    fontSize = 16.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ){
                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray)
                Text(
                    report.location,
                    color = Color.Gray,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
