package org.example.hamsteractivitytracker.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.hamsteractivitytracker.model.HamsterEvent
import org.example.hamsteractivitytracker.model.HamsterStat
import org.example.hamsteractivitytracker.model.SensorStat
import org.springframework.stereotype.Service


@Service
class HamsterActivityTrackerService(
    private val alertService: AlertService
) {
    private val hamsterStats = mutableMapOf<String, HamsterStat>()
    private val sensorStats = mutableMapOf<String, SensorStat>()
    private val wheelToHamster = mutableMapOf<String, String>()  // <wheelId, hamsterId>
    private val mutex = Mutex()

    suspend fun processEvent(event: HamsterEvent) {
        mutex.withLock {
            when (event) {
                is HamsterEvent.HamsterEnter -> {
                    hamsterStats.getOrPut(event.hamsterId) { HamsterStat() }.entriesCount++
                    wheelToHamster[event.wheelId] = event.hamsterId
                }

                is HamsterEvent.HamsterExit -> {
                    hamsterStats.getOrPut(event.hamsterId) { HamsterStat() }.exitsCount++
                    wheelToHamster.remove(event.wheelId)
                }

                is HamsterEvent.WheelSpin -> {
                    val hamsterId = wheelToHamster[event.wheelId]
                    if (hamsterId != null) {
                        val stat = hamsterStats.getOrPut(hamsterId) { HamsterStat() }
                        stat.totalActiveDuration += event.durationMs
                        stat.totalRounds++
                        stat.lastActiveTimestamp = System.currentTimeMillis()
                    }
                    sensorStats[event.wheelId] = SensorStat(System.currentTimeMillis())
                }

                is HamsterEvent.SensorFailure -> {
                    alertService.sendAlert("Sensor ${event.sensorId} failure with code ${event.errorCode}")
                }
            }
        }
    }
    suspend fun checkInactiveHamstersAndSensors() {
        val now = System.currentTimeMillis()
        val inactiveThreshold = 5 * 60 * 1000L // 5 минут

        mutex.withLock {
            hamsterStats.forEach { (hamsterId, stat) ->
                val isActive = stat.lastActiveTimestamp?.let { now - it < inactiveThreshold } ?: false
                if (!isActive) {
                    alertService.sendAlert("Hamster $hamsterId is inactive")
                }
            }

            sensorStats.forEach { (sensorId, stat) ->
                if (now - stat.lastSeenTimestamp > inactiveThreshold) {
                    alertService.sendAlert("Sensor $sensorId is inactive")
                }
            }
        }
    }

    fun getHamsterStatsSnapshot(): Map<String, HamsterStat> = hamsterStats.toMap()
    fun getSensorStatsSnapshot(): Map<String, SensorStat> = sensorStats.toMap()
}