package com.example.lumen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lumen.databinding.FragmentSensorsBinding
import com.example.lumen.databinding.ItemSensorLdrBinding
import com.example.lumen.databinding.ItemSensorPirBinding

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

        refreshSensors()
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            refreshSensors()
        }
    }

    // =========================================================
    // REFRESH EVERYTHING
    // =========================================================

    private fun refreshSensors() {

        binding.sensorList.removeAllViews()

        updateSystemStatus()

        val sensors =
            SystemStateManager.sensors

        sensors.forEach { sensor ->

            when (sensor.type) {

                SensorType.PIR -> {
                    createPirCard(sensor)
                }

                SensorType.LDR -> {
                    createLdrCard(sensor)
                }
            }
        }
    }

    // =========================================================
    // SYSTEM STATUS
    // =========================================================

    private fun updateSystemStatus() {

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

    // =========================================================
    // CREATE PIR CARD
    // =========================================================

    private fun createPirCard(
        sensor: Sensor
    ) {

        val itemBinding =
            ItemSensorPirBinding.inflate(
                layoutInflater,
                binding.sensorList,
                false
            )

        itemBinding.txtSensorName.text =
            sensor.name

        itemBinding.txtSensorGpio.text =
            getString(
                R.string.gpio_format,
                sensor.gpio
            )

        updatePirCard(
            sensor,
            itemBinding
        )

        itemBinding.btnTestSensor.setOnClickListener {

            togglePir(sensor)

        }

        binding.sensorList.addView(
            itemBinding.root
        )
    }

    // =========================================================
    // UPDATE PIR CARD
    // =========================================================

    private fun updatePirCard(
        sensor: Sensor,
        itemBinding: ItemSensorPirBinding
    ) {

        val active =
            SystemStateManager.state.mode == SystemMode.AUTO &&
                    SystemStateManager.state.ambient == AmbientState.DARK &&
                    sensor.state == PirState.ACTIVE

        if (active) {

            itemBinding.txtSensorStatus.setText(
                R.string.sensor_active
            )
            itemBinding.txtSensorStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.amber)
            )
            itemBinding.txtSensorStatus.setBackgroundResource(R.drawable.bg_status_on)

            itemBinding.sensorIconContainer.isSelected =
                true

            val linkedLight =
                sensor.linkedLightId?.let { lightId ->

                    SystemStateManager.lights.find {
                        it.id == lightId
                    }
                }

            if (linkedLight != null) {

                itemBinding.txtSensorDescription.text =
                    getString(
                        R.string.sensor_motion_controlling,
                        linkedLight.room
                    )

            } else {

                itemBinding.txtSensorDescription.setText(
                    R.string.sensor_not_controlling
                )
            }

        } else {

            itemBinding.txtSensorStatus.setText(
                R.string.sensor_idle
            )
            itemBinding.txtSensorStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.gray_text) // Replace with your OFF text color
            )
            itemBinding.txtSensorStatus.setBackgroundResource(R.drawable.bg_status_off)

            itemBinding.sensorIconContainer.isSelected =
                false

            when (SystemStateManager.state.mode) {

                SystemMode.MANUAL -> {

                    itemBinding.txtSensorDescription.setText(
                        R.string.sensor_not_controlling
                    )
                }

                SystemMode.AUTO -> {

                    when (SystemStateManager.state.ambient) {

                        AmbientState.DAY -> {

                            itemBinding.txtSensorDescription.setText(
                                R.string.sensor_lighting_paused
                            )
                        }

                        AmbientState.DARK -> {

                            itemBinding.txtSensorDescription.setText(
                                R.string.sensor_no_motion
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // CREATE LDR CARD
    // =========================================================

    private fun createLdrCard(
        sensor: Sensor
    ) {

        val itemBinding =
            ItemSensorLdrBinding.inflate(
                layoutInflater,
                binding.sensorList,
                false
            )

        itemBinding.txtLdrName.text =
            sensor.name

        itemBinding.txtLdrGpio.text =
            getString(
                R.string.gpio_format,
                sensor.gpio
            )

        updateLdrCard(
            itemBinding
        )

        binding.sensorList.addView(
            itemBinding.root
        )
    }

    // =========================================================
    // UPDATE LDR CARD
    // =========================================================

    private fun updateLdrCard(
        itemBinding: ItemSensorLdrBinding
    ) {

        when (SystemStateManager.state.ambient) {

            AmbientState.DAY -> {

                itemBinding.txtLdrStatus.setText(
                    R.string.ambient_day
                )

                itemBinding.txtLdrDescription.setText(
                    R.string.ldr_daylight
                )
            }

            AmbientState.DARK -> {

                itemBinding.txtLdrStatus.setText(
                    R.string.ambient_dark
                )

                itemBinding.txtLdrDescription.setText(
                    R.string.ldr_dark
                )
            }
        }
    }

    // =========================================================
    // TEST PIR
    // =========================================================

    private fun togglePir(
        sensor: Sensor
    ) {

        val newState =
            if (sensor.state == PirState.ACTIVE) {
                PirState.IDLE
            } else {
                PirState.ACTIVE
            }

        SystemStateManager.setPirState(
            pirNumber = sensor.id,
            pirState = newState
        )

        refreshSensors()
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}