package org.example.hamsteractivitytracker.model

import java.time.LocalDate

data class DailyReport(
    val date: LocalDate,
    val hamsterStats: Map<String, HamsterStats>
)

data class HamsterStats(
    val hamsterId: String,
    val totalRounds: Int,
    val isActive: Boolean
)