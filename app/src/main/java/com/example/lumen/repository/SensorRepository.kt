package com.example.lumen.repository

import com.example.lumen.PirState
import com.example.lumen.Sensor
import com.example.lumen.SensorType
import com.example.lumen.database.SensorDao
import com.example.lumen.database.SensorEntity

class SensorRepository(
    private val sensorDao: SensorDao
) {

    suspend fun getAllSensors(): List<Sensor> {
        return sensorDao.getAllSensors().map { entity ->
            entity.toSensor()
        }
    }

    suspend fun getSensorById(sensorId: Int): Sensor? {
        return sensorDao.getSensorById(sensorId)?.toSensor()
    }

    suspend fun insertSensor(sensor: Sensor) {
        sensorDao.insertSensor(sensor.toEntity())
    }

    suspend fun updateSensor(sensor: Sensor) {
        sensorDao.updateSensor(sensor.toEntity())
    }

    suspend fun deleteSensor(sensor: Sensor) {
        sensorDao.deleteSensor(sensor.toEntity())
    }

    suspend fun deleteAllSensors() {
        sensorDao.deleteAllSensors()
    }

    private fun SensorEntity.toSensor(): Sensor {
        return Sensor(
            id = id,
            name = name,
            type = SensorType.valueOf(type),
            gpio = gpio,
            linkedLightId = linkedLightId,
            state = PirState.valueOf(state)
        )
    }

    private fun Sensor.toEntity(): SensorEntity {
        return SensorEntity(
            id = id,
            name = name,
            type = type.name,
            gpio = gpio,
            linkedLightId = linkedLightId,
            state = state.name
        )
    }
}