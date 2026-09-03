package com.example.lumen

object SystemStateManager {

    val state = SystemState(
        connected = true,
        mode = SystemMode.MANUAL,
        ambient = AmbientState.DAY
    )

    fun setMode(mode: SystemMode) {
        state.mode = mode

        if (mode == SystemMode.MANUAL) {
            // PIR does not control lights in manual mode
            state.pir1 = PirState.IDLE
            state.pir2 = PirState.IDLE
            state.pir3 = PirState.IDLE
        }
    }

    fun setAmbient(ambient: AmbientState) {
        state.ambient = ambient

        if (
            state.mode == SystemMode.AUTO &&
            ambient == AmbientState.DAY
        ) {
            // During daylight, automatic lighting is disabled
            state.livingRoomOn = false
            state.bedroomOn = false
            state.kitchenOn = false

            state.pir1 = PirState.IDLE
            state.pir2 = PirState.IDLE
            state.pir3 = PirState.IDLE
        }
    }
}