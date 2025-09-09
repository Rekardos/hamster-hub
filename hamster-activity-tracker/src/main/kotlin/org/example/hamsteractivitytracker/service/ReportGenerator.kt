package org.example.hamsteractivitytracker.service

import org.example.hamsteractivitytracker.model.DailyReport
import java.time.LocalDate

interface ReportGenerator {
    suspend fun generateDailyReport(date: LocalDate = LocalDate.now()): DailyReport
}