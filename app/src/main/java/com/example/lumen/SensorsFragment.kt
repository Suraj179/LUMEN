package com.example.lumen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lumen.databinding.FragmentSensorsBinding

class SensorsFragment : Fragment() {

    private var _binding: FragmentSensorsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSensorsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupTestButtons()
        updateSensorUI()
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            updateSensorUI()
        }
    }

    // ---------------------------------------------------------
    // TEST BUTTONS
    // ---------------------------------------------------------

    private fun setupTestButtons() {

        binding.btnTestPir1.setOnClickListener {
            togglePir(1)
        }

        binding.btnTestPir2.setOnClickListener {
            togglePir(2)
        }

        binding.btnTestPir3.setOnClickListener {
            togglePir(3)
        }
    }

    private fun togglePir(sensorId: Int) {

        val sensor = SystemStateManager.sensors.find {
            it.id == sensorId &&
                    it.type == SensorType.PIR
        } ?: return

        val newState =
            if (sensor.state == PirState.ACTIVE) {
                PirState.IDLE
            } else {
                PirState.ACTIVE
            }

        SystemStateManager.setPirState(
            pirNumber = sensorId,
            pirState = newState
        )

        updateSensorUI()
    }

    // ---------------------------------------------------------
    // MAIN UI UPDATE
    // ---------------------------------------------------------

    private fun updateSensorUI() {

        updateSystemSensorStatus()

        updatePirSensor(
            sensorId = 1,
            card = binding.pir1Card,
            iconContainer = binding.pir1IconContainer,
            description = binding.txtPir1Description,
            status = binding.txtPir1Status
        )

        updatePirSensor(
            sensorId = 2,
            card = binding.pir2Card,
            iconContainer = binding.pir2IconContainer,
            description = binding.txtPir2Description,
            status = binding.txtPir2Status
        )

        updatePirSensor(
            sensorId = 3,
            card = binding.pir3Card,
            iconContainer = binding.pir3IconContainer,
            description = binding.txtPir3Description,
            status = binding.txtPir3Status
        )

        updateLdrSensor()
    }

    // ---------------------------------------------------------
    // SYSTEM MODE
    // ---------------------------------------------------------

    private fun updateSystemSensorStatus() {

        when (SystemStateManager.state.mode) {

            SystemMode.MANUAL -> {

                binding.txtSensorMode.setText(
                    R.string.sensor_mode_manual
                )

                binding.txtSensorModeDescription.setText(
                    R.string.sensor_mode_manual_description
                )
            }

            SystemMode.AUTO -> {

                binding.txtSensorMode.setText(
                    R.string.sensor_mode_auto
                )

                binding.txtSensorModeDescription.setText(
                    R.string.sensor_mode_auto_description
                )
            }
        }
    }

    // ---------------------------------------------------------
    // PIR SENSOR
    // ---------------------------------------------------------

    private fun updatePirSensor(
        sensorId: Int,
        card: View,
        iconContainer: View,
        description: android.widget.TextView,
        status: android.widget.TextView
    ) {

        val sensor = SystemStateManager.sensors.find {
            it.id == sensorId &&
                    it.type == SensorType.PIR
        }

        // Sensor does not exist
        if (sensor == null) {
            card.visibility = View.GONE
            return
        }

        // Sensor exists
        card.visibility = View.VISIBLE

        val isActive =
            SystemStateManager.state.mode == SystemMode.AUTO &&
                    SystemStateManager.state.ambient == AmbientState.DARK &&
                    sensor.state == PirState.ACTIVE

        if (isActive) {

            status.setText(R.string.sensor_active)

            val linkedLight =
                sensor.linkedLightId?.let { lightId ->
                    SystemStateManager.lights.find {
                        it.id == lightId
                    }
                }

            if (linkedLight != null) {

                description.text = getString(
                    R.string.sensor_motion_controlling,
                    linkedLight.room
                )

            } else {

                description.setText(
                    R.string.sensor_not_controlling
                )
            }

            iconContainer.isSelected = true

        } else {

            status.setText(R.string.sensor_idle)

            iconContainer.isSelected = false

            when (SystemStateManager.state.mode) {

                SystemMode.MANUAL -> {
                    description.setText(
                        R.string.sensor_not_controlling
                    )
                }

                SystemMode.AUTO -> {

                    when (SystemStateManager.state.ambient) {

                        AmbientState.DAY -> {
                            description.setText(
                                R.string.sensor_lighting_paused
                            )
                        }

                        AmbientState.DARK -> {
                            description.setText(
                                R.string.sensor_no_motion
                            )
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------
    // LDR SENSOR
    // ---------------------------------------------------------

    private fun updateLdrSensor() {

        val ldrSensor = SystemStateManager.sensors.find {
            it.type == SensorType.LDR
        }

        if (ldrSensor == null) {

            binding.ldrCard.visibility = View.GONE
            return
        }

        binding.ldrCard.visibility = View.VISIBLE

        when (SystemStateManager.state.ambient) {

            AmbientState.DAY -> {

                binding.txtLdrStatus.setText(
                    R.string.ambient_day
                )

                binding.txtLdrDescription.setText(
                    R.string.ldr_daylight
                )
            }

            AmbientState.DARK -> {

                binding.txtLdrStatus.setText(
                    R.string.ambient_dark
                )

                binding.txtLdrDescription.setText(
                    R.string.ldr_dark
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}