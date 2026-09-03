package com.example.lumen

object SystemStateManager {

    val state = SystemState(
        connected = true,
        mode = SystemMode.MANUAL,
        ambient = AmbientState.DAY
    )

    /*
     * =========================================================
     * LIGHTS
     * =========================================================
     */

    val lights: MutableList<Light> = mutableListOf(

        Light(
            id = 1,
            room = "Living Room",
            pirName = "PIR 1",
            mqttTopic = "home/livingroom/light1",
            isOn = false
        ),

        Light(
            id = 2,
            room = "Bedroom",
            pirName = "PIR 2",
            mqttTopic = "home/bedroom/light2",
            isOn = false
        ),

        Light(
            id = 3,
            room = "Kitchen",
            pirName = "PIR 3",
            mqttTopic = "home/kitchen/light3",
            isOn = false
        )
    )


    /*
     * =========================================================
     * SENSORS
     * =========================================================
     */

    val sensors: MutableList<Sensor> = mutableListOf(

        Sensor(
            id = 1,
            name = "PIR 1",
            type = SensorType.PIR,
            gpio = 14,
            linkedLightId = 1
        ),

        Sensor(
            id = 2,
            name = "PIR 2",
            type = SensorType.PIR,
            gpio = 27,
            linkedLightId = 2
        ),

        Sensor(
            id = 3,
            name = "PIR 3",
            type = SensorType.PIR,
            gpio = 26,
            linkedLightId = 3
        ),

        Sensor(
            id = 4,
            name = "LDR",
            type = SensorType.LDR,
            gpio = 34
        )
    )


    /*
     * =========================================================
     * MODE
     * =========================================================
     */

    fun setMode(mode: SystemMode) {

        state.mode = mode

        when (mode) {

            SystemMode.MANUAL -> {

                /*
                 * PIR has no control in MANUAL mode.
                 */

                setAllPirIdle()
            }

            SystemMode.AUTO -> {

                /*
                 * AUTO + DAY:
                 *
                 * All lights OFF
                 * PIR IDLE
                 */

                if (state.ambient == AmbientState.DAY) {

                    turnAllLightsOff()
                    setAllPirIdle()
                }
            }
        }

        updateAutomaticLights()
    }


    /*
     * =========================================================
     * AMBIENT
     * =========================================================
     */

    fun setAmbient(ambient: AmbientState) {

        state.ambient = ambient

        if (state.mode == SystemMode.AUTO) {

            when (ambient) {

                AmbientState.DAY -> {

                    /*
                     * Daytime:
                     *
                     * All lights OFF
                     * PIR IDLE
                     */

                    turnAllLightsOff()
                    setAllPirIdle()
                }

                AmbientState.DARK -> {

                    /*
                     * Darkness:
                     *
                     * PIR controls lights.
                     */

                    updateAutomaticLights()
                }
            }
        }
    }


    /*
     * =========================================================
     * PIR STATE
     * =========================================================
     */

    fun setPirState(
        pirNumber: Int,
        pirState: PirState
    ) {

        /*
         * PIR only controls lights during
         *
         * AUTO + DARK
         */

        if (
            state.mode != SystemMode.AUTO ||
            state.ambient != AmbientState.DARK
        ) {
            return
        }

        val sensor = sensors.find {
            it.id == pirNumber &&
                    it.type == SensorType.PIR
        }

        sensor?.state = pirState

        updateAutomaticLights()
    }


    /*
     * =========================================================
     * AUTOMATIC LIGHT CONTROL
     * =========================================================
     */

    private fun updateAutomaticLights() {

        /*
         * Automatic lighting only works when:
         *
         * AUTO + DARK
         */

        if (
            state.mode != SystemMode.AUTO ||
            state.ambient != AmbientState.DARK
        ) {
            return
        }

        /*
         * Start by turning all configured lights OFF.
         */

        lights.forEach {
            it.isOn = false
        }

        /*
         * Each PIR controls its linked light.
         */

        sensors
            .filter { it.type == SensorType.PIR }
            .forEach { sensor ->

                val linkedLightId = sensor.linkedLightId

                if (linkedLightId != null) {

                    val light = lights.find {
                        it.id == linkedLightId
                    }

                    if (light != null) {

                        light.isOn =
                            sensor.state == PirState.ACTIVE
                    }
                }
            }
    }


    /*
     * =========================================================
     * LIGHT CONTROL
     * =========================================================
     */

    fun setLightState(
        lightId: Int,
        isOn: Boolean
    ) {

        /*
         * Manual control is only allowed
         * in MANUAL mode.
         */

        if (state.mode != SystemMode.MANUAL) {
            return
        }

        val light = lights.find {
            it.id == lightId
        }

        light?.isOn = isOn
    }


    /*
     * =========================================================
     * TURN ALL LIGHTS OFF
     * =========================================================
     */

    private fun turnAllLightsOff() {

        lights.forEach {
            it.isOn = false
        }
    }


    /*
     * =========================================================
     * SET ALL PIR IDLE
     * =========================================================
     */

    private fun setAllPirIdle() {

        sensors
            .filter {
                it.type == SensorType.PIR
            }
            .forEach {
                it.state = PirState.IDLE
            }
    }
}