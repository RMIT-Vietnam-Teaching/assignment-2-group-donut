package com.donut.assignment2.presentation.supervisor.history

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.phuonghai.inspection.R
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.ResponseStatus
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.supervisor.history.ReportWithInspector
import com.phuonghai.inspection.presentation.supervisor.history.SupervisorHistoryViewModel
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorHistoryScreen(
    navController: NavController,
) {

    val viewModel: SupervisorHistoryViewModel = hiltViewModel()

    val reportsState by viewModel.reports.collectAsState()
    val supervisorNameState by viewModel.supervisorName.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()



    var searchQuery by remember { mutableStateOf("") }
    // Dropdown states
    var expandedTime by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedResponse by remember { mutableStateOf(false) }

    // Selected filters
    var selectedTimeFilter by remember { mutableStateOf("Most Recent") }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }
    var selectedResponseFilter by remember { mutableStateOf("All Responses") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp, top = 60.dp)
            ) {
                // 🔍 Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search reports...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = SafetyYellow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 🎛 Filter dropdown chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Time filter
                    Box {
                        FilterChip(
                            selected = expandedTime,
                            onClick = { expandedTime = !expandedTime },
                            label = { Text(selectedTimeFilter) },
                            leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                selectedContainerColor = SafetyYellow,
                                labelColor = Color.White,
                                selectedLabelColor = Black
                            )
                        )
                        DropdownMenu(
                            expanded = expandedTime,
                            onDismissRequest = { expandedTime = false }
                        ) {
                            listOf("Most Recent", "Oldest", "Today", "This Week").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedTimeFilter = option
                                        expandedTime = false
                                    }
                                )
                            }
                        }
                    }

                    // Status filter
                    Box {
                        FilterChip(
                            selected = expandedStatus,
                            onClick = { expandedStatus = !expandedStatus },
                            label = { Text(selectedStatusFilter) },
                            leadingIcon = { Icon(Icons.Default.Assignment, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                selectedContainerColor = SafetyYellow,
                                labelColor = Color.White,
                                selectedLabelColor = Black
                            )
                        )
                        DropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            listOf("All Status", "Pending Review", "Passed", "Failed", "Needs Attention").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedStatusFilter = option
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    // Response filter
                    Box {
                        FilterChip(
                            selected = expandedResponse,
                            onClick = { expandedResponse = !expandedResponse },
                            label = { Text(selectedResponseFilter) },
                            leadingIcon = { Icon(Icons.Default.Check, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                selectedContainerColor = SafetyYellow,
                                labelColor = Color.White,
                                selectedLabelColor = Black
                            )
                        )
                        DropdownMenu(
                            expanded = expandedResponse,
                            onDismissRequest = { expandedResponse = false }
                        ) {
                            listOf("All Responses", "Approved", "Rejected").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedResponseFilter = option
                                        expandedResponse = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        if(isLoadingState){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator(color = Color.White)
            }
        }else{
            val filteredReports = reportsState
                // 🔍 Search filter
                .filter { report ->
                    searchQuery.isBlank() ||
                            report.report.title.contains(searchQuery, ignoreCase = true) ||
                            report.report.address?.contains(searchQuery, ignoreCase = true) == true
                }
                // 📌 Status filter
                .filter { report ->
                    when (selectedStatusFilter) {
                        "Pending Review" -> report.report.assignStatus.name == "PENDING"
                        "Passed" -> report.report.assignStatus.name == "PASSED"
                        "Failed" -> report.report.assignStatus.name == "FAILED"
                        "Needs Attention" -> report.report.assignStatus.name == "NEEDS_ATTENTION"
                        else -> true
                    }
                }
                // 📌 Response filter
                .filter { report ->
                    when (selectedResponseFilter) {
                        "Approved" -> report.report.responseStatus.name == "APPROVED"
                        "Rejected" -> report.report.responseStatus.name == "REJECTED"
                        else -> true
                    }
                }
                // 📌 Time filter
                .let { list ->
                    when (selectedTimeFilter) {
                        "Most Recent" -> list.sortedByDescending { it.report.completedAt?.toDate()?.time }
                        "Oldest" -> list.sortedBy { it.report.completedAt?.toDate()?.time }
                        "Today" -> list.filter { isToday(it.report.completedAt?.toDate()) }
                            .sortedByDescending { it.report.completedAt?.toDate()?.time }
                        "This Week" -> list.filter { isThisWeek(it.report.completedAt?.toDate()) }
                            .sortedByDescending { it.report.completedAt?.toDate()?.time }
                        else -> list.sortedByDescending { it.report.completedAt?.toDate()?.time }
                    }
                }

            // 👉 Replace LazyColumn with filtered list
            if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reports found",
                        color = SafetyYellow,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 100.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReports.size) { index ->
                        SupervisorHistoryReportCard(filteredReports[index], navController, supervisorNameState)
                    }
                }
            }
        }
    }
}

// ---- Report Card for History ----
@Composable
fun SupervisorHistoryReportCard(
    report: ReportWithInspector, navController: NavController, supervisorName: String
) {
    val context = LocalContext.current

    val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
    val formattedDate = report.report.completedAt?.toDate()?.let { dateFormat.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        onClick = {
            navController.navigate(Screen.SupervisorReportDetailScreen.route + "/${report.report.reportId}")
        }
    ) {
        // Use a Box to allow children to be positioned individually
        Box(modifier = Modifier.fillMaxWidth()) {
            // Main content in a Column
            Column(modifier = Modifier.padding(14.dp)) {
                Text(report.report.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Submitted: $formattedDate", fontSize = 17.sp, color = Color.Gray)
                Text("Inspector: ${report.inspectorName}", fontSize = 17.sp, color = Color.Gray)
                Text("Status: ${report.report.assignStatus}", fontSize = 17.sp, color = Color.Gray)
                Text("Location: ${report.report.address}", fontSize = 17.sp, color = Color.Gray)
            }

            // 🎯 Response Tag: This is now a direct child of the Box and can be aligned
            Text(
                text = report.report.responseStatus.toString(), // Convert enum to string
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd) // This is now a direct child of Box
                    .padding(8.dp)
                    .background( Color(
                        when (report.report.responseStatus) {
                            ResponseStatus.APPROVED -> 0xFF4CAF50
                            ResponseStatus.REJECTED -> 0xFFE53935
                            else -> 0
                        }
                    ), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            if (report.report.responseStatus == ResponseStatus.APPROVED) {
                Text(
                    text = "Export PDF",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(SafetyYellow, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable {
                            generateInspectionReport(context, report,supervisorName)
                        }
                )
            }
        }
    }
}
fun isToday(date: Date?): Boolean {
    if (date == null) return false
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isThisWeek(date: Date?): Boolean {
    if (date == null) return false
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}
fun generateInspectionReport(context: Context, report: ReportWithInspector, supervisorName: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val fileName = "inspection_report_${report.report.reportId}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            document.setMargins(36f, 36f, 36f, 36f)

            // 🏢 Company Header with Logo
            val companyTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f)))
                .useAllAvailableWidth()


            // Logo (stored in drawable or file)
            val drawable = ContextCompat.getDrawable(context, R.drawable.logo)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageData = com.itextpdf.io.image.ImageDataFactory.create(stream.toByteArray())
            val logo = Image(imageData).scaleToFit(60f, 60f)

            companyTable.addCell(Cell().add(logo).setBorder(Border.NO_BORDER))
            companyTable.addCell(
                Cell().add(
                    Paragraph("Phuonghai JSC")
                        .setFontSize(18f)
                        .setBold()
                        .setTextAlignment(TextAlignment.RIGHT)
                ).setBorder(Border.NO_BORDER)
            )

            document.add(companyTable)
            document.add(LineSeparator(SolidLine()))
            document.add(Paragraph("\n"))

            // 📌 Report Title
            val title = Paragraph("Inspection Report")
                .setFontSize(22f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(title)
            document.add(Paragraph("\n"))
            val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())

            // 📊 Metadata Table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f)))
                .useAllAvailableWidth()
            table.addCell(Cell().add(Paragraph("Report ID")).setBold())
            table.addCell(report.report.reportId)
            table.addCell(Cell().add(Paragraph("Report Title")).setBold())
            table.addCell(report.report.title)
            table.addCell(Cell().add(Paragraph("Report Type")).setBold())
            table.addCell(report.report.type.name)
            table.addCell(Cell().add(Paragraph("Inspector Name")).setBold())
            table.addCell(report.inspectorName)
            table.addCell(Cell().add(Paragraph("Supervisor Name")).setBold())
            table.addCell(supervisorName)
            table.addCell(Cell().add(Paragraph("Report Priority")).setBold())
            table.addCell(report.report.priority.name)
            table.addCell(Cell().add(Paragraph("Report Score")).setBold())
            table.addCell(report.report.score.toString())
            table.addCell(Cell().add(Paragraph("Completed At")).setBold())
            table.addCell(report.report.completedAt?.let{
                dateFormat.format(it.toDate())
            })
            table.addCell(Cell().add(Paragraph("Location")).setBold())
            table.addCell(report.report.address)
            table.addCell(Cell().add(Paragraph("Status")).setBold())
            table.addCell(report.report.assignStatus.name)
            table.addCell(Cell().add(Paragraph("Response")).setBold())
            table.addCell(report.report.responseStatus.toString())
            document.add(table)
            document.add(Paragraph("\n"))

            // 📝 Description
            document.add(
                Paragraph("Inspection Summary")
                    .setFontSize(16f)
                    .setBold()
                    .setUnderline()
            )
            document.add(Paragraph(report.report.description ?: "No description").setFontSize(12f))
            document.add(Paragraph("\n"))

            // 📷 Inspection Image (downloaded before)
            val imageFile = report.report.imageUrl?.let {
                downloadImageToFile(context, it, "inspection_image.jpg")
            }
            if (imageFile != null && imageFile.exists()) {
                val imageData = com.itextpdf.io.image.ImageDataFactory.create(imageFile.absolutePath)
                val image = Image(imageData)
                    .scaleToFit(400f, 300f)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setBorder(SolidBorder(ColorConstants.BLACK, 1f))
                document.add(image)
            }

            // 📌 Footer (page numbers, disclaimer)
            val footer = Paragraph("Confidential – For internal use only")
                .setFontSize(9f)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)

            document.showTextAligned(
                footer,
                297f, 20f, pdfDocument.numberOfPages,
                TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0f
            )

            document.close()
            pdfDocument.close()

            withContext(Dispatchers.Main) {
                openPdf(context, file)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                e.printStackTrace()
                Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
    }
}
fun downloadImageToFile(context: Context, url: String, fileName: String): File? {
    return try {
        Log.d("downloadImageToFile", "Downloading image from URL: $url")
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            Log.d("downloadImageToFile", "Image download successful")
            val bytes = response.body?.bytes()
            if (bytes != null) {
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { it.write(bytes) }
                file
            } else null
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}