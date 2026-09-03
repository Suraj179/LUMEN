package com.example.lumen
data class Light(
    val id: Int,
    var room: String,
    val pirName: String,
    val mqttTopic: String,
    var isOn: Boolean = false
)