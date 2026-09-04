package com.example.lumen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lumen.database.DatabaseProvider
import com.example.lumen.databinding.ActivityMainBinding
import com.example.lumen.repository.LightRepository
import com.example.lumen.repository.SensorRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var lightRepository: LightRepository
    private lateinit var sensorRepository: SensorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database
        val database = DatabaseProvider.getDatabase(applicationContext)

        lightRepository = LightRepository(
            database.lightDao()
        )

        sensorRepository = SensorRepository(
            database.sensorDao()
        )

        // Open Dashboard first
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

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

    override fun onResume() {
        super.onResume()

        updateGlobalSystemStatus()
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