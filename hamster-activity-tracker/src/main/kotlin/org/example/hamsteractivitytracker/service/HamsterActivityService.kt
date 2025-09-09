package org.example.hamsteractivitytracker.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.hamsteractivitytracker.model.DailyReport
import org.example.hamsteractivitytracker.model.HamsterEvent
import org.example.hamsteractivitytracker.model.HamsterStats
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class HamsterActivityService(
    private val alertService: AlertService
) : ReportGenerator {

    companion object {
        private const val ROUND_DURATION_MS = 5000L
        private const val ACTIVE_THRESHOLD = 10
        private const val HAMSTER_INACTIVITY_MINUTES = 60L
        private const val SENSOR_INACTIVITY_MINUTES = 30L
    }

    private data class HamsterState(
        val currentWheelId: String? = null,
        val lastActivityTime: LocalDateTime = LocalDateTime.now()
    )

    private data class SensorState(
        val lastEventTime: LocalDateTime = LocalDateTime.now(),
        val isWorking: Boolean = true
    )
    private val hamsterStates = ConcurrentHashMap<String, HamsterState>()
    private val sensorStates = ConcurrentHashMap<String, SensorState>()
    private val dailyRounds = ConcurrentHashMap<String, AtomicInteger>()
    private val mutex = Mutex()

    private val monitoringScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        startMonitoring()
    }

    suspend fun processEvent(event: HamsterEvent) {
        when (event) {
            is HamsterEvent.WheelSpin -> handleWheelSpin(event)
            is HamsterEvent.HamsterEnter -> handleHamsterEnter(event)
            is HamsterEvent.HamsterExit -> handleHamsterExit(event)
            is HamsterEvent.SensorFailure -> handleSensorFailure(event)
        }
    }

    private suspend fun handleWheelSpin(event: HamsterEvent.WheelSpin) {
        if (event.durationMs <= 0) return

        updateSensorState(event.wheelId)

        val hamsterInWheel = findHamsterInWheel(event.wheelId) ?: return
        val rounds = (event.durationMs / ROUND_DURATION_MS).toInt()

        if (rounds > 0) {
            dailyRounds.computeIfAbsent(hamsterInWheel) { AtomicInteger(0) }.addAndGet(rounds)
            updateHamsterActivity(hamsterInWheel)
        }
    }

    private suspend fun handleHamsterEnter(event: HamsterEvent.HamsterEnter) {
        mutex.withLock {
            val current = hamsterStates[event.hamsterId]
            if (current?.currentWheelId != null) return

            hamsterStates[event.hamsterId] = HamsterState(
                currentWheelId = event.wheelId,
                lastActivityTime = LocalDateTime.now()
            )
        }
        updateSensorState(event.wheelId)
    }

    private suspend fun handleHamsterExit(event: HamsterEvent.HamsterExit) {
        mutex.withLock {
            val current = hamsterStates[event.hamsterId]
            if (current?.currentWheelId != event.wheelId) return

            hamsterStates[event.hamsterId] = current.copy(
                currentWheelId = null,
                lastActivityTime = LocalDateTime.now()
            )
        }
        updateSensorState(event.wheelId)
    }

    private suspend fun handleSensorFailure(event: HamsterEvent.SensorFailure) {
        sensorStates[event.sensorId] = SensorState(
            lastEventTime = LocalDateTime.now(),
            isWorking = false
        )
        alertService.sendAlert("Sensor ${event.sensorId} failed (error: ${event.errorCode})")
    }

    override suspend fun generateDailyReport(date: LocalDate): DailyReport {
        val stats = dailyRounds.mapValues { (hamsterId, rounds) ->
            val roundCount = rounds.get()
            HamsterStats(
                hamsterId = hamsterId,
                totalRounds = roundCount,
                isActive = roundCount > ACTIVE_THRESHOLD
            )
        }

        return DailyReport(date, stats)
    }

    private fun findHamsterInWheel(wheelId: String): String? {
        return hamsterStates.entries.find { it.value.currentWheelId == wheelId }?.key
    }

    private fun updateHamsterActivity(hamsterId: String) {
        hamsterStates.compute(hamsterId) { _, current ->
            current?.copy(lastActivityTime = LocalDateTime.now()) ?:
            HamsterState(lastActivityTime = LocalDateTime.now())
        }
    }

    private fun updateSensorState(sensorId: String) {
        sensorStates[sensorId] = SensorState(LocalDateTime.now(), true)
    }

    private fun startMonitoring() {
        monitoringScope.launch {
            while (isActive) {
                checkInactiveEntities()
                delay(60_000)
            }
        }
    }

    private suspend fun checkInactiveEntities() {
        val now = LocalDateTime.now()

        hamsterStates.forEach { (hamsterId, state) ->
            val minutes = ChronoUnit.MINUTES.between(state.lastActivityTime, now)
            if (minutes > HAMSTER_INACTIVITY_MINUTES) {
                alertService.sendAlert("Hamster $hamsterId inactive for $minutes minutes")
            }
        }

        sensorStates.forEach { (sensorId, state) ->
            val minutes = ChronoUnit.MINUTES.between(state.lastEventTime, now)
            if (minutes > SENSOR_INACTIVITY_MINUTES && state.isWorking) {
                alertService.sendAlert("Sensor $sensorId inactive for $minutes minutes")
            }
        }
    }

    @PreDestroy
    fun cleanup() {
        monitoringScope.cancel()
    }
}