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

    var ambient: AmbientState = AmbientState.DAY,

    var pir1: PirState = PirState.IDLE,
    var pir2: PirState = PirState.IDLE,
    var pir3: PirState = PirState.IDLE,

    var livingRoomOn: Boolean = false,
    var bedroomOn: Boolean = false,
    var kitchenOn: Boolean = false
)