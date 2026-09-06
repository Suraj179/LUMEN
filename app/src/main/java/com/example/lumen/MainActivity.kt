package com.example.lumen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lumen.database.DatabaseProvider
import com.example.lumen.databinding.ActivityMainBinding
import com.example.lumen.mqtt.MqttConfig
import com.example.lumen.mqtt.MqttManager
import com.example.lumen.repository.LightRepository
import com.example.lumen.repository.SensorRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var lightRepository: LightRepository
    private lateinit var sensorRepository: SensorRepository

    private lateinit var mqttManager: MqttManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------------------------------------------------
        // Initialize Room database
        // ---------------------------------------------------------

        val database =
            DatabaseProvider.getDatabase(applicationContext)

        lightRepository =
            LightRepository(
                database.lightDao()
            )

        sensorRepository =
            SensorRepository(
                database.sensorDao()
            )

        // ---------------------------------------------------------
        // Get the single application-level MQTT instance
        // ---------------------------------------------------------

        mqttManager =
            (application as MqttApplication).mqttManager

        setupMqttCallbacks()

        // ---------------------------------------------------------
        // Initialize/load persistent device data
        // ---------------------------------------------------------

        val databaseInitializer =
            DatabaseInitializer(
                context = applicationContext,
                database = database
            )

        lifecycleScope.launch {

            databaseInitializer.initialize()

            updateGlobalSystemStatus()

            // Open Dashboard first
            if (savedInstanceState == null) {
                loadFragment(DashboardFragment())
            }
        }

        // ---------------------------------------------------------
        // Bottom navigation
        // ---------------------------------------------------------

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }

                R.id.nav_devices -> {
                    loadFragment(DevicesFragment())
                    true
                }

                R.id.nav_sensors -> {
                    loadFragment(SensorsFragment())
                    true
                }

                else -> false
            }
        }

        updateGlobalSystemStatus()
    }

    // -------------------------------------------------------------
    // MQTT lifecycle
    // -------------------------------------------------------------

    override fun onStart() {
        super.onStart()

        // Connect when MainActivity becomes visible
        mqttManager.connect()
    }

    override fun onResume() {
        super.onResume()

        updateGlobalSystemStatus()
    }

    override fun onStop() {
        super.onStop()

        /*
         * Do not disconnect here.
         *
         * MqttManager belongs to the Application, so the MQTT
         * connection can remain available while navigating
         * between fragments.
         */
    }

    // -------------------------------------------------------------
    // MQTT callbacks
    // -------------------------------------------------------------

    private fun setupMqttCallbacks() {

        // ---------------------------------------------------------
        // Connection status
        // ---------------------------------------------------------

        mqttManager.onConnectionChanged = { connected ->

            runOnUiThread {

                SystemStateManager
                    .state
                    .connected = connected

                if (connected) {
                    subscribeToSensorTopics()
                }

                updateGlobalSystemStatus()
            }
        }

        // ---------------------------------------------------------
        // Incoming MQTT messages
        // ---------------------------------------------------------

        mqttManager.onMessageReceived = {
                topic,
                message ->

            runOnUiThread {

                println(
                    "MQTT MESSAGE: " +
                            "$topic -> $message"
                )

                handleMqttMessage(
                    topic = topic,
                    message = message
                )
            }
        }

        // ---------------------------------------------------------
        // MQTT errors
        // ---------------------------------------------------------

        mqttManager.onError = { errorMessage ->

            runOnUiThread {

                println(
                    "MQTT ERROR: $errorMessage"
                )
            }
        }
    }

    // -------------------------------------------------------------
    // Subscribe to ESP32 sensor topics
    // -------------------------------------------------------------

    private fun subscribeToSensorTopics() {

        mqttManager.subscribe(
            topic = "home/livingroom/pir1/state"
        )

        mqttManager.subscribe(
            topic = "home/bedroom/pir2/state"
        )

        mqttManager.subscribe(
            topic = "home/kitchen/pir3/state"
        )

        mqttManager.subscribe(
            topic = MqttConfig.LDR_TOPIC
        )

        println("Subscribed to sensor topics")
    }

    // -------------------------------------------------------------
    // Handle incoming MQTT sensor messages
    // -------------------------------------------------------------

    private fun handleMqttMessage(
        topic: String,
        message: String
    ) {

        val normalizedMessage =
            message.trim().uppercase()

        when (topic) {

            // -----------------------------------------------------
            // PIR 1
            // -----------------------------------------------------

            "home/livingroom/pir1/state" -> {

                val pirState =
                    when (normalizedMessage) {

                        "ON",
                        "ACTIVE",
                        "1" -> PirState.ACTIVE

                        "OFF",
                        "IDLE",
                        "0" -> PirState.IDLE

                        else -> return
                    }

                SystemStateManager.setPirState(
                    pirNumber = 1,
                    pirState = pirState
                )
            }

            // -----------------------------------------------------
            // PIR 2
            // -----------------------------------------------------

            "home/bedroom/pir2/state" -> {

                val pirState =
                    when (normalizedMessage) {

                        "ON",
                        "ACTIVE",
                        "1" -> PirState.ACTIVE

                        "OFF",
                        "IDLE",
                        "0" -> PirState.IDLE

                        else -> return
                    }

                SystemStateManager.setPirState(
                    pirNumber = 2,
                    pirState = pirState
                )
            }

            // -----------------------------------------------------
            // PIR 3
            // -----------------------------------------------------

            "home/kitchen/pir3/state" -> {

                val pirState =
                    when (normalizedMessage) {

                        "ON",
                        "ACTIVE",
                        "1" -> PirState.ACTIVE

                        "OFF",
                        "IDLE",
                        "0" -> PirState.IDLE

                        else -> return
                    }

                SystemStateManager.setPirState(
                    pirNumber = 3,
                    pirState = pirState
                )
            }

            // -----------------------------------------------------
            // LDR
            // -----------------------------------------------------

            MqttConfig.LDR_TOPIC -> {

                when (normalizedMessage) {

                    "DARK",
                    "NIGHT",
                    "1" -> {

                        SystemStateManager.setAmbient(
                            AmbientState.DARK
                        )
                    }

                    "DAY",
                    "LIGHT",
                    "0" -> {

                        SystemStateManager.setAmbient(
                            AmbientState.DAY
                        )
                    }
                }
            }
        }

        // Refresh global status after processing the message
        updateGlobalSystemStatus()
    }

    // -------------------------------------------------------------
    // Global system status
    // -------------------------------------------------------------

    private fun updateGlobalSystemStatus() {

        if (SystemStateManager.state.connected) {

            binding.txtGlobalConnection.text =
                "● Connected"

            binding.txtGlobalConnectionDescription.text =
                "System is online"

        } else {

            binding.txtGlobalConnection.text =
                "● Disconnected"

            binding.txtGlobalConnectionDescription.text =
                "System is offline"
        }

        when (SystemStateManager.state.mode) {

            SystemMode.MANUAL -> {

                binding.txtGlobalMode.text =
                    "MANUAL"

                binding.txtGlobalModeDescription.text =
                    "Manual control"
            }

            SystemMode.AUTO -> {

                binding.txtGlobalMode.text =
                    "AUTO"

                binding.txtGlobalModeDescription.text =
                    "Automatic control"
            }
        }
    }

    // -------------------------------------------------------------
    // Fragment navigation
    // -------------------------------------------------------------

    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()
    }

    // -------------------------------------------------------------
    // Called by fragments when global state changes
    // -------------------------------------------------------------

    fun refreshGlobalStatus() {
        updateGlobalSystemStatus()
    }
}