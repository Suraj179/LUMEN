package com.example.lumen.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress

class MqttManager(
    private val brokerHost: String,
    private val brokerPort: Int,
    private val clientId: String
) {

    companion object {
        private const val TAG = "MqttManager"
    }

    private val managerScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var mqttClient: Mqtt3AsyncClient? = null

    var isConnected: Boolean = false
        private set

    var onConnectionChanged: ((Boolean) -> Unit)? = null
    var onMessageReceived: ((String, String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun connect() {

        if (mqttClient?.state?.isConnected == true) {
            Log.d(TAG, "Already connected")
            updateConnectionState(true)
            return
        }

        managerScope.launch {

            try {

                Log.d(
                    TAG,
                    "Resolving IPv4 address for $brokerHost"
                )

                val ipv4Address = InetAddress
                    .getAllByName(brokerHost)
                    .firstOrNull { it is Inet4Address }

                if (ipv4Address == null) {

                    Log.e(
                        TAG,
                        "No IPv4 address found for $brokerHost"
                    )

                    updateConnectionState(false)

                    onError?.invoke(
                        "Could not find an IPv4 address for $brokerHost"
                    )

                    return@launch
                }

                val ipv4Host =
                    ipv4Address.hostAddress

                Log.d(
                    TAG,
                    "Using IPv4 address: $ipv4Host:$brokerPort"
                )

                val client = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(clientId)
                    .serverHost(ipv4Host)
                    .serverPort(brokerPort)
                    .buildAsync()

                mqttClient = client

                Log.d(
                    TAG,
                    "Connecting to $ipv4Host:$brokerPort"
                )

                client.connect()
                    .whenComplete { _, throwable ->

                        if (throwable != null) {

                            Log.e(
                                TAG,
                                "MQTT connection failed",
                                throwable
                            )

                            updateConnectionState(false)

                            onError?.invoke(
                                throwable.message
                                    ?: "MQTT connection failed"
                            )

                        } else {

                            Log.d(
                                TAG,
                                "Connected to MQTT broker"
                            )

                            updateConnectionState(true)
                        }
                    }

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "MQTT setup failed",
                    exception
                )

                updateConnectionState(false)

                onError?.invoke(
                    exception.message
                        ?: "MQTT setup failed"
                )
            }
        }
    }

    fun disconnect() {

        val client = mqttClient

        if (client == null ||
            !client.state.isConnected
        ) {

            updateConnectionState(false)
            return
        }

        client.disconnect()
            .whenComplete { _, throwable ->

                if (throwable != null) {

                    Log.e(
                        TAG,
                        "MQTT disconnect failed",
                        throwable
                    )

                    onError?.invoke(
                        throwable.message
                            ?: "MQTT disconnect failed"
                    )

                } else {

                    Log.d(
                        TAG,
                        "MQTT disconnected"
                    )

                    updateConnectionState(false)
                }
            }
    }

    fun publish(
        topic: String,
        message: String,
        qos: Int = MqttConfig.DEFAULT_QOS,
        retained: Boolean = MqttConfig.DEFAULT_RETAINED
    ) {

        val client = mqttClient

        if (client == null ||
            !client.state.isConnected
        ) {

            Log.w(
                TAG,
                "Cannot publish. MQTT is not connected."
            )

            onError?.invoke(
                "MQTT is not connected"
            )

            return
        }

        client.publishWith()
            .topic(topic)
            .qos(convertQos(qos))
            .retain(retained)
            .payload(
                message.toByteArray(Charsets.UTF_8)
            )
            .send()
            .whenComplete { _, throwable ->

                if (throwable != null) {

                    Log.e(
                        TAG,
                        "Publish failed: $topic",
                        throwable
                    )

                    onError?.invoke(
                        throwable.message
                            ?: "Publish failed"
                    )

                } else {

                    Log.d(
                        TAG,
                        "Published: $topic -> $message"
                    )
                }
            }
    }

    fun subscribe(
        topic: String,
        qos: Int = MqttConfig.DEFAULT_QOS
    ) {

        val client = mqttClient

        if (client == null ||
            !client.state.isConnected
        ) {

            Log.w(
                TAG,
                "Cannot subscribe. MQTT is not connected."
            )

            onError?.invoke(
                "MQTT is not connected"
            )

            return
        }

        client.subscribeWith()
            .topicFilter(topic)
            .qos(convertQos(qos))
            .callback { publish ->

                val receivedTopic =
                    publish.topic.toString()

                val receivedMessage =
                    publish.payload
                        .map { buffer ->

                            val bytes =
                                ByteArray(buffer.remaining())

                            buffer.get(bytes)

                            String(
                                bytes,
                                Charsets.UTF_8
                            )
                        }
                        .orElse("")

                Log.d(
                    TAG,
                    "Received: $receivedTopic -> $receivedMessage"
                )

                onMessageReceived?.invoke(
                    receivedTopic,
                    receivedMessage
                )
            }
            .send()
            .whenComplete { _, throwable ->

                if (throwable != null) {

                    Log.e(
                        TAG,
                        "Subscribe failed: $topic",
                        throwable
                    )

                    onError?.invoke(
                        throwable.message
                            ?: "Subscribe failed"
                    )

                } else {

                    Log.d(
                        TAG,
                        "Subscribed: $topic"
                    )
                }
            }
    }

    fun unsubscribe(topic: String) {

        val client = mqttClient

        if (client == null ||
            !client.state.isConnected
        ) {
            return
        }

        client.unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenComplete { _, throwable ->

                if (throwable != null) {

                    Log.e(
                        TAG,
                        "Unsubscribe failed: $topic",
                        throwable
                    )

                } else {

                    Log.d(
                        TAG,
                        "Unsubscribed: $topic"
                    )
                }
            }
    }

    private fun convertQos(qos: Int): MqttQos {

        return when (qos) {

            0 -> MqttQos.AT_MOST_ONCE

            1 -> MqttQos.AT_LEAST_ONCE

            2 -> MqttQos.EXACTLY_ONCE

            else -> MqttQos.AT_LEAST_ONCE
        }
    }

    private fun updateConnectionState(
        connected: Boolean
    ) {

        isConnected = connected

        onConnectionChanged?.invoke(
            connected
        )
    }
}