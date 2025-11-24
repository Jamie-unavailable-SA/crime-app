package com.example.crimewatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crimewatch.data.models.CrimeIntensity
import com.example.crimewatch.data.models.CrimeRisk
import com.example.crimewatch.data.models.CrimeTrendPoint
import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location
import com.example.crimewatch.viewmodel.AnalyticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(analyticsViewModel: AnalyticsViewModel = viewModel()) {
    val crimeIntensity by analyticsViewModel.crimeIntensity.collectAsState()
    val crimeTrend by analyticsViewModel.crimeTrend.collectAsState()
    val riskLevels by analyticsViewModel.riskLevels.collectAsState()
    val locations by analyticsViewModel.locations.collectAsState()
    val crimeTypes by analyticsViewModel.crimeTypes.collectAsState()

    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedCrimeType by remember { mutableStateOf<CrimeType?>(null) }
    var locationExpanded by remember { mutableStateOf(false) }
    var crimeTypeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(locations) {
        if (selectedLocation == null && locations.isNotEmpty()) {
            selectedLocation = locations.first()
        }
    }

    LaunchedEffect(selectedLocation, selectedCrimeType) {
        selectedLocation?.let {
            analyticsViewModel.loadAnalytics(it.id, selectedCrimeType?.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Location and Crime Type Dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Dropdown
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = !locationExpanded }
                ) {
                    TextField(
                        value = selectedLocation?.area ?: "Select Location",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location.area) },
                                onClick = {
                                    selectedLocation = location
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Crime Type Dropdown
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = crimeTypeExpanded,
                    onExpandedChange = { crimeTypeExpanded = !crimeTypeExpanded }
                ) {
                    TextField(
                        value = selectedCrimeType?.name ?: "All Crimes",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = crimeTypeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = crimeTypeExpanded,
                        onDismissRequest = { crimeTypeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Crimes") },
                            onClick = {
                                selectedCrimeType = null
                                crimeTypeExpanded = false
                            }
                        )
                        crimeTypes.forEach { crimeType ->
                            DropdownMenuItem(
                                text = { Text(crimeType.name) },
                                onClick = {
                                    selectedCrimeType = crimeType
                                    crimeTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bar Chart for Crime Intensity
        BarChart(crimeIntensity = crimeIntensity)

        Spacer(modifier = Modifier.height(16.dp))

        // Line Chart for Crime Trend
        LineChart(crimeTrend = crimeTrend)

        Spacer(modifier = Modifier.height(16.dp))

        // Risk Levels
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(riskLevels) { risk ->
                RiskLevelItem(risk)
            }
        }
    }
}

@Composable
fun BarChart(crimeIntensity: List<CrimeIntensity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (crimeIntensity.isNotEmpty()) {
            val entries = crimeIntensity.mapIndexed { index, intensity ->
                BarEntry(index.toFloat(), intensity.count.toFloat())
            }
            val labels = crimeIntensity.map { it.crime_type }
            val dataSet = BarDataSet(entries, "Crime Intensity")
            val barData = BarData(dataSet)

            AndroidView(factory = { context ->
                BarChart(context).apply {
                    data = barData
                    description.isEnabled = false
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    invalidate()
                }
            }, update = { chart ->
                chart.data = barData
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                chart.notifyDataSetChanged()
                chart.invalidate()
            })
        }
    }
}

@Composable
fun LineChart(crimeTrend: List<CrimeTrendPoint>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (crimeTrend.isNotEmpty()) {
            val entries = crimeTrend.map {
                Entry(it.date.time.toFloat(), it.count.toFloat())
            }
            val dataSet = LineDataSet(entries, "Crime Trend")
            val lineData = LineData(dataSet)
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

            AndroidView(factory = { context ->
                LineChart(context).apply {
                    data = lineData
                    description.isEnabled = false
                    xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return dateFormat.format(Date(value.toLong()))
                        }
                    }
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    invalidate()
                }
            }, update = { chart ->
                chart.data = lineData
                chart.xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
                chart.notifyDataSetChanged()
                chart.invalidate()
            })
        }
    }
}


@Composable
fun RiskLevelItem(crimeRisk: CrimeRisk) {
    val riskColor = when (crimeRisk.level.lowercase(Locale.ROOT)) {
        "low" -> Color.Green
        "medium" -> Color.Yellow
        "high" -> Color.Red
        else -> Color.Gray
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = crimeRisk.crime_type)
            Text(text = "Risk: ${crimeRisk.level}")
        }
    }
}