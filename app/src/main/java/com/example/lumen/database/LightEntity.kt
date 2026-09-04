package com.example.lumen.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lights")
data class LightEntity(

    @PrimaryKey
    val id: Int,

    val room: String,

    val pirName: String,

    val mqttTopic: String,

    val isOn: Boolean = false
)