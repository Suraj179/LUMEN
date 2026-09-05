package com.example.lumen.mqtt

object MqttConfig {

    // MQTT broker
    const val BROKER_URL = "tcp://broker.hivemq.com:1883"

    // Generate/use a unique client ID for this Android app
    const val CLIENT_ID_PREFIX = "lumen_android_"

    // MQTT Quality of Service
    const val DEFAULT_QOS = 1

    // Do not retain commands by default
    const val DEFAULT_RETAINED = false

    /*
     * Light command topics
     *
     * Android -> MQTT Broker -> ESP32
     */
    const val LIGHT_TOPIC_PREFIX = "home"

    /*
     * Sensor state topics
     *
     * ESP32 -> MQTT Broker -> Android
     */
    const val PIR_TOPIC_SUFFIX = "state"
    const val LDR_TOPIC = "home/ambient/ldr/state"

    /*
     * Generate a light topic from room and light ID.
     *
     * Example:
     * room = "Living Room"
     * id = 1
     *
     * Result:
     * home/livingroom/light1
     */
    fun createLightTopic(
        room: String,
        lightId: Int
    ): String {

        val cleanRoom = room
            .lowercase()
            .replace(" ", "")

        return "$LIGHT_TOPIC_PREFIX/$cleanRoom/light$lightId"
    }

    /*
     * Generate a PIR state topic.
     *
     * Example:
     * room = "Living Room"
     * id = 1
     *
     * Result:
     * home/livingroom/pir1/state
     */
    fun createPirTopic(
        room: String,
        pirId: Int
    ): String {

        val cleanRoom = room
            .lowercase()
            .replace(" ", "")

        return "$LIGHT_TOPIC_PREFIX/$cleanRoom/pir$pirId/$PIR_TOPIC_SUFFIX"
    }
}