package com.example.lumen

enum class SensorType {
    PIR,
    LDR
}

data class Sensor(
    val id: Int,
    var name: String,
    val type: SensorType,
    var gpio: Int,
    var linkedLightId: Int? = null,
    var state: PirState = PirState.IDLE
)