package com.example.lumen.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensors")
data class SensorEntity(

    @PrimaryKey
    val id: Int,

    val name: String,

    val type: String,

    val gpio: Int,

    val linkedLightId: Int? = null,

    val state: String = "IDLE"
)