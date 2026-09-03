package com.example.lumen

enum class SystemMode {
    MANUAL,
    AUTO
}

enum class AmbientState {
    DAY,
    DARK
}

enum class PirState {
    IDLE,
    ACTIVE
}

data class SystemState(
    var connected: Boolean = true,
    var mode: SystemMode = SystemMode.MANUAL,
    var ambient: AmbientState = AmbientState.DAY
)