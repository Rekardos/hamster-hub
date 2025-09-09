package org.example.hamsteractivitytracker.model

import java.time.LocalDate

data class DailyReport(
    val date: LocalDate,
    val hamsterStats: Map<String, HamsterStats>
)
