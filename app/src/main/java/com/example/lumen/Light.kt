package com.example.lumen
data class Light(
    val id: Int,
    val room: String,
    val pirName: String,
    val mqttTopic: String,
    var isOn: Boolean = false
)