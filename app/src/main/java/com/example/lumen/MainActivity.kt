package com.example.lumen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lumen.database.DatabaseProvider
import com.example.lumen.databinding.ActivityMainBinding
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

    private fun setupMqttCallbacks() {

        mqttManager.onConnectionChanged = { connected ->

            runOnUiThread {

                SystemStateManager
                    .state
                    .connected = connected

                updateGlobalSystemStatus()
            }
        }

        mqttManager.onMessageReceived = {
                topic,
                message ->

            runOnUiThread {

                println(
                    "MQTT MESSAGE: " +
                            "$topic -> $message"
                )
            }
        }

        mqttManager.onError = { errorMessage ->

            runOnUiThread {

                println(
                    "MQTT ERROR: $errorMessage"
                )
            }
        }
    }
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

    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()
    }

    fun refreshGlobalStatus() {
        updateGlobalSystemStatus()
    }
}