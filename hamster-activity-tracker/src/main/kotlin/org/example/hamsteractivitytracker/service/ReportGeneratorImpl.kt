package org.example.hamsteractivitytracker.service

import org.example.hamsteractivitytracker.model.DailyReport
import org.example.hamsteractivitytracker.model.HamsterStats
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ReportGeneratorImpl(
    private val tracker: HamsterActivityTrackerService
) : ReportGenerator {

    override suspend fun generateDailyReport(date: LocalDate): DailyReport {
        val hamsterStatsSnapshot = tracker.getHamsterStatsSnapshot()

        val stats = hamsterStatsSnapshot.mapValues { (hamsterId, stat) ->
            val rounds = stat.totalRounds  // <-- используем именно totalRounds
            HamsterStats(
                hamsterId = hamsterId,
                totalRounds = rounds,
                isActive = rounds > 0         // <-- считаем активным, если есть хоть один круг
            )
        }

        return DailyReport(
            date = date,
            hamsterStats = stats
        )
    }
}