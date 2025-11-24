package com.example.crimewatch.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.crimewatch.R
import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location
import com.example.crimewatch.viewmodel.ReportViewModel
import java.util.Calendar
import java.util.Locale

// ---------------------------------------------------------
// 🔹 Reusable Scrollable Selection List Component
// ---------------------------------------------------------
@Composable
fun <T> SelectionList(
    title: String,
    items: List<T>,
    selectedItem: T?,
    labelSelector: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            LazyColumn {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(item) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = labelSelector(item),
                            modifier = Modifier.weight(1f)
                        )

                        if (selectedItem == item) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 🔹 Main ReportScreen
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(reportViewModel: ReportViewModel = viewModel()) {

    val context = LocalContext.current

    var selectedCrimeType by remember { mutableStateOf<CrimeType?>(null) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var occurrenceDate by remember { mutableStateOf("") }
    var occurrenceTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val crimeTypes by reportViewModel.crimeTypes.collectAsState()
    val locations by reportViewModel.locations.collectAsState()
    val imageUris = remember { mutableStateListOf<Uri>() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        imageUris.clear()
        imageUris.addAll(uris)
    }

    val reportState by reportViewModel.reportState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Report a Crime", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        // ---------------------------------------------------------
        // 🔥 Crime Type Selection List
        // ---------------------------------------------------------
        SelectionList(
            title = "Crime Type",
            items = crimeTypes,
            selectedItem = selectedCrimeType,
            labelSelector = { it.name },
            onSelected = { selectedCrimeType = it }
        )
        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------
        // 🔥 Location Selection List
        // ---------------------------------------------------------
        SelectionList(
            title = "Location",
            items = locations,
            selectedItem = selectedLocation,
            labelSelector = { it.area },
            onSelected = { selectedLocation = it }
        )
        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------
        // 📅 Date Picker
        // ---------------------------------------------------------
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                occurrenceDate = "$day/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        OutlinedTextField(
            value = occurrenceDate,
            onValueChange = {},
            label = { Text("Occurrence Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------
        // ⏰ Time Picker
        // ---------------------------------------------------------
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                occurrenceTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        OutlinedTextField(
            value = occurrenceTime,
            onValueChange = {},
            label = { Text("Occurrence Time") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { timePickerDialog.show() }) {
                    Icon(painterResource(id = R.drawable.ic_schedule), contentDescription = "Select Time")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------
        // ✍️ Description
        // ---------------------------------------------------------
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------
        // 📸 Scene Media
        // ---------------------------------------------------------
        Text("Scene Media:")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            imageUris.forEach { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }
            IconButton(onClick = { launcher.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Media")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------------------------------------------------------
        // 🚀 Submit Button
        // ---------------------------------------------------------
        Button(
            onClick = {
                val fullTime = if (occurrenceDate.isNotBlank() && occurrenceTime.isNotBlank()) {
                    val parts = occurrenceDate.split("/")
                    "${parts[0].padStart(2, '0')}/${parts[1].padStart(2, '0')}/${parts[2]} $occurrenceTime"
                } else null

                reportViewModel.submitReport(
                    crimeTypeId = selectedCrimeType?.id,
                    description = description,
                    locationId = selectedLocation?.id,
                    occurrenceTime = fullTime,
                    imageUris = imageUris.toList()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Submit", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------
        // 📌 Report State Feedback
        // ---------------------------------------------------------
        when (val state = reportState) {
            is ReportViewModel.ReportState.Loading -> CircularProgressIndicator()
            is ReportViewModel.ReportState.Success -> {
                Text("Report submitted successfully!", color = Color.Green)
                Toast.makeText(context, "Report submitted successfully!", Toast.LENGTH_SHORT).show()
            }
            is ReportViewModel.ReportState.Error -> {
                Text(state.message, color = Color.Red)
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }

        // Add spacer for bottom navigation bar
        Spacer(Modifier.height(80.dp))
    }
}
