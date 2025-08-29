package org.example.hamsteractivitytracker.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"  // именно это поле нужно в JSON
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HamsterEvent.HamsterEnter::class, name = "HamsterEnter"),
    JsonSubTypes.Type(value = HamsterEvent.HamsterExit::class, name = "HamsterExit"),
    JsonSubTypes.Type(value = HamsterEvent.WheelSpin::class, name = "WheelSpin"),
    JsonSubTypes.Type(value = HamsterEvent.SensorFailure::class, name = "SensorFailure")
)
sealed class HamsterEvent {
    data class WheelSpin(val wheelId: String, val durationMs: Long) : HamsterEvent()
    data class HamsterEnter(val hamsterId: String, val wheelId: String) : HamsterEvent()
    data class HamsterExit(val hamsterId: String, val wheelId: String) : HamsterEvent()
    data class SensorFailure(val sensorId: String, val errorCode: Int) : HamsterEvent()
}