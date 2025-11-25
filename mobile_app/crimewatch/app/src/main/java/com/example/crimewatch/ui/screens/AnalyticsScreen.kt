package com.example.crimewatch.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location
import com.example.crimewatch.data.models.RecentReport
import com.example.crimewatch.data.models.ReportCountSummary
import com.example.crimewatch.data.models.RiskLevel
import com.example.crimewatch.viewmodel.AnalyticsViewModel

// -------------------------
// Risk Level Color Helper
// -------------------------
fun levelColor(level: String): Color =
    when (level) {
        "Low" -> Color(0xFF4CAF50)
        "Medium" -> Color(0xFFFFC107)
        "High" -> Color(0xFFF44336)
        else -> Color.Gray
    }

// -------------------------
// Location Selector (explicit, non-generic)
// -------------------------
@Composable
fun LocationSelector(
    locations: List<Location>,
    selectedLocation: Location?,
    onSelect: (Location) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Select Location", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp, max = 200.dp)
        ) {
            items(locations.size) { index ->
                val loc = locations[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(loc) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (loc == selectedLocation),
                        onClick = { onSelect(loc) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(loc.area)
                }
                Divider()
            }
        }
    }
}

// -------------------------
// Crime Type Selector (explicit, non-generic)
// -------------------------
@Composable
fun CrimeTypeSelector(
    crimeTypes: List<CrimeType>,
    selectedType: CrimeType?,
    onSelect: (CrimeType) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Filter by Crime Type (optional)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp, max = 200.dp)
        ) {
            items(crimeTypes.size) { index ->
                val type = crimeTypes[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(type) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (type == selectedType),
                        onClick = { onSelect(type) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(type.name)
                }
                Divider()
            }
        }
    }
}

// -------------------------
// AnalyticsScreen
// -------------------------
@Composable
fun AnalyticsScreen(analyticsViewModel: AnalyticsViewModel = viewModel()) {

    val locations by analyticsViewModel.locations.collectAsState()
    val crimeTypes by analyticsViewModel.crimeTypes.collectAsState()
    val riskLevels by analyticsViewModel.riskLevels.collectAsState()
    val reportCounts by analyticsViewModel.reportCounts.collectAsState()
    val recentReports by analyticsViewModel.recentReports.collectAsState()

    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedCrimeType by remember { mutableStateOf<CrimeType?>(null) }
    var selectedRange by remember { mutableStateOf("30 days") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text("Analytics Dashboard", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        // Location selector (explicit)
        LocationSelector(
            locations = locations,
            selectedLocation = selectedLocation,
            onSelect = { location ->
                selectedLocation = location
                analyticsViewModel.loadAnalytics(location.id, selectedCrimeType?.id, selectedRange)
            }
        )

        Spacer(Modifier.height(20.dp))

        // Crime type selector (explicit)
        CrimeTypeSelector(
            crimeTypes = crimeTypes,
            selectedType = selectedCrimeType,
            onSelect = { crimeType ->
                selectedCrimeType = crimeType
                selectedLocation?.let { loc ->
                    analyticsViewModel.loadAnalytics(loc.id, crimeType.id, selectedRange)
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        // Time Range Selector
        Text("Time Range", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("7 days", "30 days", "90 days").forEach { range ->
                OutlinedButton(
                    onClick = {
                        selectedRange = range
                        selectedLocation?.let { loc ->
                            analyticsViewModel.loadAnalytics(loc.id, selectedCrimeType?.id, selectedRange)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedRange == range)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                ) {
                    Text(range)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Risk Levels
        if (riskLevels.isNotEmpty()) {
            Text("Risk Levels", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            riskLevels.forEach { risk ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = levelColor(risk.level)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${risk.crime_type}: ${risk.level}",
                        modifier = Modifier.padding(12.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // Report Counts
        if (reportCounts.isNotEmpty()) {
            Text("Reports by Crime Type", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            reportCounts.forEach { c ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${c.crime_type}: ${c.count} reports",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // Recent Reports
        if (recentReports.isNotEmpty()) {
            Text("Recent Reports", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            recentReports.forEach { r ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${r.crime_type} — ${r.time_ago}")
                        Text(r.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Add spacer for bottom navigation bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}
