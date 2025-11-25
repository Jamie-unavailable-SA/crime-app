package com.example.crimewatch.data.models

data class AnalyticsResponse(
    val risk_levels: List<RiskLevel>,
    val report_counts: List<ReportCountSummary>,
    val recent_reports: List<RecentReport>
)
