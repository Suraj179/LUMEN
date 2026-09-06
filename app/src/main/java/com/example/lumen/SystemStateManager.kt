package com.example.lumen

object SystemStateManager {

    val state = SystemState(
        connected = true,
        mode = SystemMode.MANUAL,
        ambient = AmbientState.DAY
    )

    fun loadDevices(
        lights: List<Light>,
        sensors: List<Sensor>
    ) {
        val currentLightStates =
            this.lights.associate { light ->
                light.id to light.isOn
            }

        val currentPirStates =
            this.sensors
                .filter { it.type == SensorType.PIR }
                .associate { sensor ->
                    sensor.id to sensor.state
                }

        this.lights.clear()

        this.lights.addAll(
            lights.map { light ->
                light.copy(
                    isOn = currentLightStates[light.id]
                        ?: light.isOn
                )
            }
        )

        this.sensors.clear()

        this.sensors.addAll(
            sensors.map { sensor ->
                sensor.copy(
                    state = currentPirStates[sensor.id]
                        ?: sensor.state
                )
            }
        )
    }

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

    // =========================================================
    // ID GENERATION
    // =========================================================

    private fun getNextLightId(): Int {
        return (lights.maxOfOrNull { it.id } ?: 0) + 1
    }

    private fun getNextSensorId(): Int {
        return (sensors.maxOfOrNull { it.id } ?: 0) + 1
    }

    // =========================================================
    // LIGHT MANAGEMENT
    // =========================================================

    fun addLight(
        room: String
    ): Light {

        val id = getNextLightId()

        val newLight = Light(
            id = id,
            room = room,
            pirName = "PIR $id",
            mqttTopic = createMqttTopic(room, id),
            isOn = false
        )

        lights.add(newLight)

        return newLight
    }

    fun updateLight(
        lightId: Int,
        room: String
    ): Boolean {

        val light = lights.find {
            it.id == lightId
        } ?: return false

        light.room = room

        light.mqttTopic =
            createMqttTopic(
                room = room,
                id = light.id
            )

        return true
    }

    fun deleteLight(
        lightId: Int
    ) {

        // Find light
        lights.removeAll {
            it.id == lightId
        }

        // If a PIR is linked to this light,
        // remove the relationship.
        sensors.forEach { sensor ->

            if (sensor.linkedLightId == lightId) {
                sensor.linkedLightId = null
            }
        }

        updateAutomaticLights()
    }

    // =========================================================
    // SENSOR MANAGEMENT
    // =========================================================

    fun addPir(
        name: String,
        gpio: Int,
        linkedLightId: Int?
    ): Sensor {

        val id = getNextSensorId()

        val newSensor = Sensor(
            id = id,
            name = name,
            type = SensorType.PIR,
            gpio = gpio,
            linkedLightId = linkedLightId,
            state = PirState.IDLE
        )

        sensors.add(newSensor)

        return newSensor
    }

    fun deleteSensor(
        sensorId: Int
    ) {

        sensors.removeAll {
            it.id == sensorId
        }

        updateAutomaticLights()
    }

    fun addLdr(
        gpio: Int
    ): Sensor {

        val id = getNextSensorId()

        val newSensor = Sensor(
            id = id,
            name = "LDR",
            type = SensorType.LDR,
            gpio = gpio
        )

        sensors.add(newSensor)

        return newSensor
    }

    fun hasLdr(): Boolean {
        return sensors.any {
            it.type == SensorType.LDR
        }
    }

    // =========================================================
    // MQTT TOPIC
    // =========================================================

    private fun createMqttTopic(
        room: String,
        id: Int
    ): String {

        val cleanRoom = room
            .lowercase()
            .replace(" ", "")

        return "home/$cleanRoom/light$id"
    }

    // =========================================================
    // SYSTEM MODE
    // =========================================================

    fun setMode(mode: SystemMode) {

        state.mode = mode

        when (mode) {

            SystemMode.MANUAL -> {
                setAllPirIdle()
            }

            SystemMode.AUTO -> {

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
                    turnAllLightsOff()
                    setAllPirIdle()
                }

                AmbientState.DARK -> {
                    updateAutomaticLights()
                }
            }
        }
    }

    // =========================================================
    // PIR
    // =========================================================

    fun setPirState(
        pirNumber: Int,
        pirState: PirState
    ) {

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

    // =========================================================
    // AUTOMATIC LIGHT CONTROL
    // =========================================================

    private fun updateAutomaticLights() {

        if (
            state.mode != SystemMode.AUTO ||
            state.ambient != AmbientState.DARK
        ) {
            return
        }

        lights.forEach {
            it.isOn = false
        }

        sensors
            .filter {
                it.type == SensorType.PIR
            }
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

    // =========================================================
    // MANUAL LIGHT CONTROL
    // =========================================================

    fun setLightState(
        lightId: Int,
        isOn: Boolean
    ) {

        if (state.mode != SystemMode.MANUAL) {
            return
        }

        val light = lights.find {
            it.id == lightId
        }

        light?.isOn = isOn
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private fun turnAllLightsOff() {

        lights.forEach {
            it.isOn = false
        }
    }

    private fun setAllPirIdle() {

        sensors
            .filter {
                it.type == SensorType.PIR
            }
            .forEach {
                it.state = PirState.IDLE
            }
    }

    fun isGpioInUse(
        gpio: Int,
        excludingSensorId: Int? = null
    ): Boolean {
        return sensors.any { sensor ->
            sensor.gpio == gpio &&
                    sensor.id != excludingSensorId
        }
    }

    fun isLightAssignedToPir(
        lightId: Int,
        excludingSensorId: Int? = null
    ): Boolean {
        return sensors.any { sensor ->
            sensor.type == SensorType.PIR &&
                    sensor.linkedLightId == lightId &&
                    sensor.id != excludingSensorId
        }
    }

    fun updatePir(
        sensorId: Int,
        name: String,
        gpio: Int,
        linkedLightId: Int?
    ): Boolean {

        val sensor = sensors.find {
            it.id == sensorId &&
                    it.type == SensorType.PIR
        } ?: return false

        sensor.name = name

        // Sensor.gpio is currently val, so this will require
        // changing it to var in Sensor.kt.
        sensor.gpio = gpio

        sensor.linkedLightId = linkedLightId

        return true
    }
}