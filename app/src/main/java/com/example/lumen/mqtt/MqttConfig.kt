package com.example.lumen.mqtt

object MqttConfig {

    const val BROKER_HOST = "broker.hivemq.com"
    const val BROKER_PORT = 1883

    const val CLIENT_ID_PREFIX = "lumen_android_"

    const val DEFAULT_QOS = 1
    const val DEFAULT_RETAINED = false

    const val LIGHT_TOPIC_PREFIX = "home"
    const val PIR_TOPIC_SUFFIX = "state"
    const val LDR_TOPIC = "home/ambient/ldr/state"

    const val MQTT_ON = "ON"
    const val MQTT_OFF = "OFF"


    fun createLightTopic(room: String, lightId: Int): String {
        val cleanRoom = room
            .lowercase()
            .replace(" ", "")

        return "$LIGHT_TOPIC_PREFIX/$cleanRoom/light$lightId"
    }

    fun createPirTopic(room: String, pirId: Int): String {
        val cleanRoom = room
            .lowercase()
            .replace(" ", "")

        return "$LIGHT_TOPIC_PREFIX/$cleanRoom/pir$pirId/$PIR_TOPIC_SUFFIX"
    }
}