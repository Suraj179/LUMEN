package com.example.lumen

data class Light(
    val id: Int,
    var room: String,
    val pirName: String,
    var mqttTopic: String,
    var isOn: Boolean = false
)