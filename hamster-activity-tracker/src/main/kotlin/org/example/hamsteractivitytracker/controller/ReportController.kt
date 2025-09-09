package org.example.hamsteractivitytracker.controller

import org.example.hamsteractivitytracker.model.DailyReport
import org.example.hamsteractivitytracker.service.HamsterActivityService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/report")
class ReportController(private val activityService: HamsterActivityService) {

    @GetMapping("/daily")
    suspend fun getDailyReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): DailyReport {
        return activityService.generateDailyReport(date)
    }
}