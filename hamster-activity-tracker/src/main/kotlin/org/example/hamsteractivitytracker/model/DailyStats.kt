package org.example.hamsteractivitytracker.model

data class DailyStats(
    val hamsterStats: MutableMap<String, HamsterStat> = mutableMapOf(),
    val sensorStats: MutableMap<String, SensorStat> = mutableMapOf()
)