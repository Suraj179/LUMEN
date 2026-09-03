package com.example.lumen

object SystemStateManager {

    val state = SystemState(
        connected = true,
        mode = SystemMode.MANUAL,
        ambient = AmbientState.DAY
    )

    fun setMode(mode: SystemMode) {

        state.mode = mode

        when (mode) {

            SystemMode.MANUAL -> {

                // PIR has no control in manual mode
                state.pir1 = PirState.IDLE
                state.pir2 = PirState.IDLE
                state.pir3 = PirState.IDLE

            }

            SystemMode.AUTO -> {

                /*
                 * When entering AUTO + DAY,
                 * all lights must be OFF.
                 */

                if (state.ambient == AmbientState.DAY) {

                    turnAllLightsOff()

                    setAllPirIdle()
                }
            }
        }

        updateAutomaticLights()
    }

    fun setAmbient(ambient: AmbientState) {

        state.ambient = ambient

        if (state.mode == SystemMode.AUTO) {

            when (ambient) {

                AmbientState.DAY -> {

                    /*
                     * During daylight:
                     *
                     * All lights OFF
                     * PIR IDLE
                     */

                    turnAllLightsOff()

                    setAllPirIdle()
                }

                AmbientState.DARK -> {

                    /*
                     * During darkness:
                     *
                     * PIR becomes responsible
                     * for controlling lights.
                     */

                    updateAutomaticLights()
                }
            }
        }
    }

    fun setPirState(
        pirNumber: Int,
        pirState: PirState
    ) {

        /*
         * PIR readings are only meaningful
         * when AUTO + DARK.
         */

        if (
            state.mode != SystemMode.AUTO ||
            state.ambient != AmbientState.DARK
        ) {
            return
        }

        when (pirNumber) {

            1 -> state.pir1 = pirState

            2 -> state.pir2 = pirState

            3 -> state.pir3 = pirState
        }

        updateAutomaticLights()
    }

    private fun updateAutomaticLights() {

        /*
         * Automatic lighting only works
         * when the system is:
         *
         * AUTO + DARK
         */

        if (
            state.mode != SystemMode.AUTO ||
            state.ambient != AmbientState.DARK
        ) {

            return
        }

        state.livingRoomOn =
            state.pir1 == PirState.ACTIVE

        state.bedroomOn =
            state.pir2 == PirState.ACTIVE

        state.kitchenOn =
            state.pir3 == PirState.ACTIVE
    }

    private fun turnAllLightsOff() {

        state.livingRoomOn = false
        state.bedroomOn = false
        state.kitchenOn = false
    }

    private fun setAllPirIdle() {

        state.pir1 = PirState.IDLE
        state.pir2 = PirState.IDLE
        state.pir3 = PirState.IDLE
    }
}