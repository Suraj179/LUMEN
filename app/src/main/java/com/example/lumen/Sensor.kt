package com.example.lumen

enum class SensorType {
    PIR,
    LDR
}

data class Sensor(
    val id: Int,
    val name: String,
    val type: SensorType,
    val gpio: Int,
    var linkedLightId: Int? = null,
    var state: PirState = PirState.IDLE
)