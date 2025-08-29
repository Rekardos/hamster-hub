package org.example.hamsteractivitytracker.model

data class HamsterStat(
    var entriesCount: Int = 0,
    var exitsCount: Int = 0,
    var totalRounds: Int = 0,
    var totalActiveDuration: Long = 0,
    var lastActiveTimestamp: Long? = null,
    var lastSeenTimestamp: Long = System.currentTimeMillis()  // для проверки активности
)