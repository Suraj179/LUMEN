package com.example.lumen.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttManager(
    context: Context,
    private val brokerUrl: String,
    private val clientId: String
) {

    companion object {
        private const val TAG = "MqttManager"
    }

    private val mqttClient = MqttAndroidClient(
        context.applicationContext,
        brokerUrl,
        clientId
    )

    var isConnected: Boolean = false
        private set

    var onConnectionChanged: ((Boolean) -> Unit)? = null

    var onMessageReceived: ((String, String) -> Unit)? = null

    var onError: ((String) -> Unit)? = null

    init {
        mqttClient.setCallback(object : MqttCallbackExtended {

            override fun connectComplete(
                reconnect: Boolean,
                serverURI: String?
            ) {
                isConnected = true

                Log.d(
                    TAG,
                    "Connected to MQTT broker: $serverURI"
                )

                onConnectionChanged?.invoke(true)
            }

            override fun connectionLost(cause: Throwable?) {
                isConnected = false

                Log.d(
                    TAG,
                    "MQTT connection lost",
                    cause
                )

                onConnectionChanged?.invoke(false)

                cause?.message?.let {
                    onError?.invoke(it)
                }
            }

            override fun messageArrived(
                topic: String?,
                message: MqttMessage?
            ) {
                if (topic == null || message == null) {
                    return
                }

                val payload = String(message.payload)

                Log.d(
                    TAG,
                    "Message received: $topic -> $payload"
                )

                onMessageReceived?.invoke(
                    topic,
                    payload
                )
            }

            override fun deliveryComplete(
                token: IMqttDeliveryToken?
            ) {
                Log.d(
                    TAG,
                    "Message delivery complete"
                )
            }
        })
    }

    fun connect() {

        if (mqttClient.isConnected) {
            Log.d(TAG, "Already connected")
            return
        }

        val options = MqttConnectOptions().apply {

            isAutomaticReconnect = true

            isCleanSession = true

            connectionTimeout = 10

            keepAliveInterval = 30
        }

        try {

            mqttClient.connect(
                options,
                null,
                object : IMqttActionListener {

                    override fun onSuccess(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?
                    ) {
                        Log.d(
                            TAG,
                            "MQTT connection successful"
                        )

                        isConnected = true
                        onConnectionChanged?.invoke(true)
                    }

                    override fun onFailure(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?,
                        exception: Throwable?
                    ) {
                        isConnected = false

                        Log.e(
                            TAG,
                            "MQTT connection failed",
                            exception
                        )

                        onConnectionChanged?.invoke(false)

                        onError?.invoke(
                            exception?.message
                                ?: "MQTT connection failed"
                        )
                    }
                }
            )

        } catch (exception: MqttException) {

            isConnected = false

            Log.e(
                TAG,
                "MQTT connection exception",
                exception
            )

            onConnectionChanged?.invoke(false)

            onError?.invoke(
                exception.message
                    ?: "MQTT connection exception"
            )
        }
    }

    fun disconnect() {

        if (!mqttClient.isConnected) {
            return
        }

        try {

            mqttClient.disconnect(
                null,
                object : IMqttActionListener {

                    override fun onSuccess(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?
                    ) {
                        isConnected = false

                        Log.d(
                            TAG,
                            "MQTT disconnected"
                        )

                        onConnectionChanged?.invoke(false)
                    }

                    override fun onFailure(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?,
                        exception: Throwable?
                    ) {
                        Log.e(
                            TAG,
                            "MQTT disconnect failed",
                            exception
                        )
                    }
                }
            )

        } catch (exception: MqttException) {

            Log.e(
                TAG,
                "MQTT disconnect exception",
                exception
            )
        }
    }

    fun publish(
        topic: String,
        message: String,
        qos: Int = 1,
        retained: Boolean = false
    ) {

        if (!mqttClient.isConnected) {
            onError?.invoke(
                "Cannot publish: MQTT is not connected"
            )
            return
        }

        try {

            val mqttMessage = MqttMessage(
                message.toByteArray()
            ).apply {
                this.qos = qos
                isRetained = retained
            }

            mqttClient.publish(
                topic,
                mqttMessage
            )

            Log.d(
                TAG,
                "Message published: $topic -> $message"
            )

        } catch (exception: MqttException) {

            Log.e(
                TAG,
                "Publish failed",
                exception
            )

            onError?.invoke(
                exception.message
                    ?: "MQTT publish failed"
            )
        }
    }

    fun subscribe(
        topic: String,
        qos: Int = 1
    ) {

        if (!mqttClient.isConnected) {
            onError?.invoke(
                "Cannot subscribe: MQTT is not connected"
            )
            return
        }

        try {

            mqttClient.subscribe(
                topic,
                qos,
                null,
                object : IMqttActionListener {

                    override fun onSuccess(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?
                    ) {
                        Log.d(
                            TAG,
                            "Subscribed to: $topic"
                        )
                    }

                    override fun onFailure(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?,
                        exception: Throwable?
                    ) {
                        Log.e(
                            TAG,
                            "Subscription failed: $topic",
                            exception
                        )

                        onError?.invoke(
                            exception?.message
                                ?: "MQTT subscription failed"
                        )
                    }
                }
            )

        } catch (exception: MqttException) {

            Log.e(
                TAG,
                "Subscribe exception",
                exception
            )

            onError?.invoke(
                exception.message
                    ?: "MQTT subscribe failed"
            )
        }
    }

    fun subscribe(
        topic: String,
        qos: Int = 1,
        listener: IMqttMessageListener
    ) {

        if (!mqttClient.isConnected) {
            onError?.invoke(
                "Cannot subscribe: MQTT is not connected"
            )
            return
        }

        try {

            mqttClient.subscribe(
                topic,
                qos,
                null,
                object : IMqttActionListener {

                    override fun onSuccess(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?
                    ) {
                        Log.d(
                            TAG,
                            "Subscribed to: $topic"
                        )
                    }

                    override fun onFailure(
                        asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?,
                        exception: Throwable?
                    ) {
                        Log.e(
                            TAG,
                            "Subscription failed: $topic",
                            exception
                        )
                    }
                },
                listener
            )

        } catch (exception: MqttException) {

            Log.e(
                TAG,
                "Subscribe exception",
                exception
            )
        }
    }

    fun unsubscribe(topic: String) {

        if (!mqttClient.isConnected) {
            return
        }

        try {

            mqttClient.unsubscribe(topic)

            Log.d(
                TAG,
                "Unsubscribed from: $topic"
            )

        } catch (exception: MqttException) {

            Log.e(
                TAG,
                "Unsubscribe failed",
                exception
            )
        }
    }
}