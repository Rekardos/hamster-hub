package org.example.hamsteractivitytracker.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ActivityMonitoringRunner(
    private val trackerService: HamsterActivityTrackerService
) : ApplicationRunner {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun run(args: ApplicationArguments?) {
        scope.launch {
            while (coroutineContext.isActive) {
                trackerService.checkInactiveHamstersAndSensors()
                delay(60_000)
            }
        }
    }
}