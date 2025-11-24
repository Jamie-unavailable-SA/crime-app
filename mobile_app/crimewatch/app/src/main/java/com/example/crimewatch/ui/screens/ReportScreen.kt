package com.example.crimewatch.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.crimewatch.R
import com.example.crimewatch.viewmodel.ReportViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(reportViewModel: ReportViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedCrimeType by remember { mutableStateOf<com.example.crimewatch.data.models.CrimeType?>(null) }
    var selectedLocation by remember { mutableStateOf<com.example.crimewatch.data.models.Location?>(null) }
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Report a Crime", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        // Crime Type Dropdown
        var expandedCrimeType by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expandedCrimeType, onExpandedChange = { expandedCrimeType = !expandedCrimeType }) {
            OutlinedTextField(
                value = selectedCrimeType?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Crime Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCrimeType) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCrimeType, onDismissRequest = { expandedCrimeType = false }) {
                crimeTypes.forEach { type ->
                    DropdownMenuItem(text = { Text(type.name) }, onClick = {
                        selectedCrimeType = type
                        expandedCrimeType = false
                    })
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        // Location Dropdown
        var expandedLocation by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expandedLocation, onExpandedChange = { expandedLocation = !expandedLocation }) {
            OutlinedTextField(
                value = selectedLocation?.area ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedLocation, onDismissRequest = { expandedLocation = false }) {
                locations.forEach { loc ->
                    DropdownMenuItem(text = { Text(loc.area) }, onClick = {
                        selectedLocation = loc
                        expandedLocation = false
                    })
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        // Occurrence Date Picker
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selYear, selMonth, selDay ->
                occurrenceDate = "$selDay/${selMonth + 1}/$selYear"
            },
            year, month, day
        )
        OutlinedTextField(
            value = occurrenceDate,
            onValueChange = {},
            label = { Text("Occurrence Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        // Occurrence Time Picker
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            context,
            { _, selHour, selMinute ->
                occurrenceTime = String.format(Locale.getDefault(), "%02d:%02d", selHour, selMinute)
            },
            hour, minute, true
        )
        OutlinedTextField(
            value = occurrenceTime,
            onValueChange = {},
            label = { Text("Occurrence Time") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { timePickerDialog.show() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_schedule), contentDescription = "Select Time")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        // Description Field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(Modifier.height(10.dp))
        // Scene Media
        Text("Scene Media:")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            imageUris.forEach { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Media",
                    modifier = Modifier.size(100.dp)
                )
            }
            IconButton(onClick = { launcher.launch("*/*") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Media"
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                val fullOccurrenceTime = if (occurrenceDate.isNotBlank() && occurrenceTime.isNotBlank()) {
                    val parts = occurrenceDate.split("/")
                    val day = parts[0].padStart(2, '0')
                    val month = parts[1].padStart(2, '0')
                    val year = parts[2]
                    "$day/$month/$year $occurrenceTime"
                } else {
                    null
                }
                reportViewModel.submitReport(
                    crimeTypeId = selectedCrimeType?.id,
                    description = description,
                    locationId = selectedLocation?.id,
                    occurrenceTime = fullOccurrenceTime,
                    imageUris = imageUris.toList()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Submit", color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
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
            is ReportViewModel.ReportState.Empty -> {
                // Do nothing
            }
        }
    }
}
