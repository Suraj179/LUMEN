package com.example.lumen

import android.app.Application
import com.example.lumen.mqtt.MqttConfig
import com.example.lumen.mqtt.MqttManager
import java.util.UUID

class MqttApplication : Application() {

    lateinit var mqttManager: MqttManager
        private set

    override fun onCreate() {
        super.onCreate()

        val clientId =
            MqttConfig.CLIENT_ID_PREFIX +
                    UUID.randomUUID().toString()

        mqttManager = MqttManager(
            brokerHost = MqttConfig.BROKER_HOST,
            brokerPort = MqttConfig.BROKER_PORT,
            clientId = clientId
        )
    }
}