package com.example.lumen

import android.content.Context
import androidx.room.withTransaction
import com.example.lumen.database.AppDatabase
import com.example.lumen.database.LightEntity
import com.example.lumen.database.SensorEntity

class DatabaseInitializer(
    private val context: Context,
    private val database: AppDatabase
) {

    companion object {
        private const val PREF_NAME = "lumen_database_preferences"
        private const val KEY_INITIALIZED = "database_initialized"
    }

    suspend fun initialize() {

        val preferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        val alreadyInitialized =
            preferences.getBoolean(KEY_INITIALIZED, false)

        if (!alreadyInitialized) {

            database.withTransaction {

                insertDefaultLights()
                insertDefaultSensors()
            }

            preferences.edit()
                .putBoolean(KEY_INITIALIZED, true)
                .apply()
        }

        loadDevicesIntoSystemState()
    }

    // =========================================================
    // DEFAULT LIGHTS
    // =========================================================

    private suspend fun insertDefaultLights() {

        val lights = listOf(

            LightEntity(
                id = 1,
                room = "Living Room",
                pirName = "PIR 1",
                mqttTopic = "home/livingroom/light1",
                isOn = false
            ),

            LightEntity(
                id = 2,
                room = "Bedroom",
                pirName = "PIR 2",
                mqttTopic = "home/bedroom/light2",
                isOn = false
            ),

            LightEntity(
                id = 3,
                room = "Kitchen",
                pirName = "PIR 3",
                mqttTopic = "home/kitchen/light3",
                isOn = false
            )
        )

        lights.forEach { light ->
            database.lightDao().insertLight(light)
        }
    }

    // =========================================================
    // DEFAULT SENSORS
    // =========================================================

    private suspend fun insertDefaultSensors() {

        val sensors = listOf(

            SensorEntity(
                id = 1,
                name = "PIR 1",
                type = SensorType.PIR.name,
                gpio = 14,
                linkedLightId = 1,
                state = PirState.IDLE.name
            ),

            SensorEntity(
                id = 2,
                name = "PIR 2",
                type = SensorType.PIR.name,
                gpio = 27,
                linkedLightId = 2,
                state = PirState.IDLE.name
            ),

            SensorEntity(
                id = 3,
                name = "PIR 3",
                type = SensorType.PIR.name,
                gpio = 26,
                linkedLightId = 3,
                state = PirState.IDLE.name
            ),

            SensorEntity(
                id = 4,
                name = "LDR",
                type = SensorType.LDR.name,
                gpio = 34,
                linkedLightId = null,
                state = PirState.IDLE.name
            )
        )

        sensors.forEach { sensor ->
            database.sensorDao().insertSensor(sensor)
        }
    }

    // =========================================================
    // LOAD ROOM DATA INTO RUNTIME STATE
    // =========================================================

    private suspend fun loadDevicesIntoSystemState() {

        val savedLights =
            database.lightDao().getAllLights()

        val savedSensors =
            database.sensorDao().getAllSensors()

        val lights = savedLights.map { entity ->

            Light(
                id = entity.id,
                room = entity.room,
                pirName = entity.pirName,
                mqttTopic = entity.mqttTopic,
                isOn = entity.isOn
            )
        }

        val sensors = savedSensors.map { entity ->

            Sensor(
                id = entity.id,
                name = entity.name,
                type = SensorType.valueOf(entity.type),
                gpio = entity.gpio,
                linkedLightId = entity.linkedLightId,
                state = PirState.valueOf(entity.state)
            )
        }

        SystemStateManager.loadDevices(
            lights = lights,
            sensors = sensors
        )
    }
}