package com.example.lumen.repository

import com.example.lumen.Light
import com.example.lumen.database.LightDao
import com.example.lumen.database.LightEntity

class LightRepository(
    private val lightDao: LightDao
) {

    suspend fun getAllLights(): List<Light> {
        return lightDao.getAllLights().map { entity ->
            entity.toLight()
        }
    }

    suspend fun getLightById(lightId: Int): Light? {
        return lightDao.getLightById(lightId)?.toLight()
    }

    suspend fun insertLight(light: Light) {
        lightDao.insertLight(light.toEntity())
    }

    suspend fun updateLight(light: Light) {
        lightDao.updateLight(light.toEntity())
    }

    suspend fun deleteLight(light: Light) {
        lightDao.deleteLight(light.toEntity())
    }

    suspend fun deleteAllLights() {
        lightDao.deleteAllLights()
    }

    private fun LightEntity.toLight(): Light {
        return Light(
            id = id,
            room = room,
            pirName = pirName,
            mqttTopic = mqttTopic,
            isOn = isOn
        )
    }

    private fun Light.toEntity(): LightEntity {
        return LightEntity(
            id = id,
            room = room,
            pirName = pirName,
            mqttTopic = mqttTopic,
            isOn = isOn
        )
    }
}