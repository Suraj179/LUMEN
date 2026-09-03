package com.example.lumen

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lumen.databinding.FragmentDevicesBinding
import com.example.lumen.databinding.ItemDeviceLightBinding
import com.example.lumen.databinding.ItemDeviceSensorBinding

class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDevicesBinding.inflate(
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

        binding.btnAddDevice.setOnClickListener {
            showAddDeviceDialog()
        }

        refreshDeviceList()
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            refreshDeviceList()
        }
    }

    // =========================================================
    // REFRESH DEVICE LIST
    // =========================================================

    private fun refreshDeviceList() {

        binding.deviceList.removeAllViews()

        val lights = SystemStateManager.lights
        val sensors = SystemStateManager.sensors

        val totalDevices =
            lights.size + sensors.size

        binding.txtDeviceCount.text =
            getString(
                R.string.device_count_format,
                totalDevices
            )

        // Add lights
        lights.forEach { light ->
            createLightCard(light)
        }

        // Add sensors
        sensors.forEach { sensor ->
            createSensorCard(sensor)
        }
    }

    // =========================================================
    // LIGHT CARD
    // =========================================================

    private fun createLightCard(
        light: Light
    ) {

        val itemBinding =
            ItemDeviceLightBinding.inflate(
                layoutInflater,
                binding.deviceList,
                false
            )

        itemBinding.txtDeviceLightName.text =
            getString(
                R.string.light_name_format,
                light.id
            )

        itemBinding.txtDeviceLightRoom.text =
            light.room

        itemBinding.txtDeviceLightTopic.text =
            getString(
                R.string.topic_format,
                light.mqttTopic
            )

        itemBinding.txtDeviceLightStatus.text =
            if (light.isOn) {
                getString(R.string.on)
            } else {
                getString(R.string.off)
            }

        itemBinding.btnDeleteLight.setOnClickListener {

            showDeleteLightDialog(light)
        }

        itemBinding.btnEditLight.setOnClickListener {

            showEditLightDialog(light)
        }

        binding.deviceList.addView(
            itemBinding.root
        )
    }

    // =========================================================
    // SENSOR CARD
    // =========================================================

    private fun createSensorCard(
        sensor: Sensor
    ) {

        val itemBinding =
            ItemDeviceSensorBinding.inflate(
                layoutInflater,
                binding.deviceList,
                false
            )

        itemBinding.txtDeviceSensorName.text =
            sensor.name

        itemBinding.txtDeviceSensorType.text =
            when (sensor.type) {

                SensorType.PIR ->
                    getString(R.string.pir)

                SensorType.LDR ->
                    getString(R.string.ldr)
            }

        val gpioText =
            getString(
                R.string.gpio_format,
                sensor.gpio
            )

        if (sensor.type == SensorType.PIR) {

            val linkedLight =
                sensor.linkedLightId?.let { lightId ->
                    SystemStateManager.lights.find {
                        it.id == lightId
                    }
                }

            val linkedText =
                if (linkedLight != null) {

                    getString(
                        R.string.linked_light_format,
                        linkedLight.room
                    )

                } else {

                    getString(
                        R.string.no_linked_light
                    )
                }

            itemBinding.txtDeviceSensorInfo.text =
                "$gpioText\n$linkedText"

        } else {

            itemBinding.txtDeviceSensorInfo.text =
                gpioText
        }

        itemBinding.btnDeleteSensor.setOnClickListener {

            showDeleteSensorDialog(sensor)
        }

        binding.deviceList.addView(
            itemBinding.root
        )
    }

    // =========================================================
    // ADD DEVICE
    // =========================================================

    private fun showAddDeviceDialog() {

        val options = arrayOf(
            "Light",
            "PIR Sensor",
            "LDR"
        )

        AlertDialog.Builder(requireContext())
            .setTitle(
                getString(R.string.add_device)
            )
            .setItems(options) { _, which ->

                when (which) {

                    0 -> showAddLightDialog()

                    1 -> showAddPirDialog()

                    2 -> showAddLdrDialog()
                }
            }
            .show()
    }

    // =========================================================
    // ADD LIGHT
    // =========================================================

    private fun showAddLightDialog() {

        val input = EditText(requireContext())

        input.hint = "Room name"
        input.inputType =
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(requireContext())
            .setTitle("Add Light")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->

                val room =
                    input.text
                        .toString()
                        .trim()

                if (room.isEmpty()) {
                    return@setPositiveButton
                }

                SystemStateManager.addLight(
                    room = room
                )

                refreshDeviceList()
            }
            .show()
    }

    // =========================================================
    // ADD PIR
    // =========================================================

    private fun showAddPirDialog() {

            val lights = SystemStateManager.lights

            if (lights.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("No Lights Available")
                    .setMessage(
                        "Add a Light first before creating a PIR sensor."
                    )
                    .setPositiveButton("OK", null)
                    .show()

                return
            }

            // Find which lights are already controlled by a PIR
            val assignedLightIds =
                SystemStateManager.sensors
                    .filter { it.type == SensorType.PIR }
                    .mapNotNull { it.linkedLightId }
                    .toSet()

            val layout =
                LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(
                        40,
                        0,
                        40,
                        0
                    )
                }

            // -------------------------
            // Sensor name
            // -------------------------

            val nameInput =
                EditText(requireContext()).apply {
                    hint = "Sensor name"
                    inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS
                }

            layout.addView(nameInput)

            // -------------------------
            // GPIO
            // -------------------------

            val gpioInput =
                EditText(requireContext()).apply {
                    hint = "GPIO number"
                    inputType =
                        InputType.TYPE_CLASS_NUMBER
                }

            val gpioParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            gpioParams.topMargin = 8

            layout.addView(
                gpioInput,
                gpioParams
            )

            // -------------------------
            // Light selection label
            // -------------------------

            val lightLabel =
                android.widget.TextView(requireContext()).apply {
                    text = "Controls"
                    setTextColor(
                        resources.getColor(
                            R.color.mist,
                            null
                        )
                    )
                    textSize = 13f
                }

            val labelParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            labelParams.topMargin = 16

            layout.addView(
                lightLabel,
                labelParams
            )

            // -------------------------
            // Light dropdown
            // -------------------------

            val lightSpinner =
                android.widget.Spinner(requireContext())

            val lightNames =
                lights.map { light ->

                    if (assignedLightIds.contains(light.id)) {
                        "Light ${light.id} — ${light.room} (Already assigned)"
                    } else {
                        "Light ${light.id} — ${light.room}"
                    }
                }

            val spinnerAdapter =
                android.widget.ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    lightNames
                )

            spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            lightSpinner.adapter = spinnerAdapter

            val spinnerParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            spinnerParams.topMargin = 4

            layout.addView(
                lightSpinner,
                spinnerParams
            )

            // -------------------------
            // Add dialog
            // -------------------------

            AlertDialog.Builder(requireContext())
                .setTitle("Add PIR Sensor")
                .setView(layout)
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton("Add") { _, _ ->

                    val name =
                        nameInput.text
                            .toString()
                            .trim()

                    val gpio =
                        gpioInput.text
                            .toString()
                            .toIntOrNull()

                    if (
                        name.isEmpty() ||
                        gpio == null
                    ) {
                        return@setPositiveButton
                    }

                    val selectedPosition =
                        lightSpinner.selectedItemPosition

                    if (
                        selectedPosition < 0 ||
                        selectedPosition >= lights.size
                    ) {
                        return@setPositiveButton
                    }

                    val selectedLight =
                        lights[selectedPosition]

                    // Prevent assigning a Light that is already linked
                    if (
                        assignedLightIds.contains(
                            selectedLight.id
                        )
                    ) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Light Already Assigned")
                            .setMessage(
                                "${selectedLight.room} is already controlled by another PIR sensor."
                            )
                            .setPositiveButton("OK", null)
                            .show()

                        return@setPositiveButton
                    }

                    SystemStateManager.addPir(
                        name = name,
                        gpio = gpio,
                        linkedLightId = selectedLight.id
                    )

                    refreshDeviceList()
                }
                .show()
    }

    // =========================================================
    // ADD LDR
    // =========================================================

    private fun showAddLdrDialog() {

        if (SystemStateManager.hasLdr()) {

            AlertDialog.Builder(requireContext())
                .setTitle("LDR already exists")
                .setMessage(
                    "Only one LDR is currently supported."
                )
                .setPositiveButton("OK", null)
                .show()

            return
        }

        val input =
            EditText(requireContext())

        input.hint = "GPIO number"
        input.inputType =
            InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(requireContext())
            .setTitle("Add LDR")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->

                val gpio =
                    input.text
                        .toString()
                        .toIntOrNull()

                if (gpio == null) {
                    return@setPositiveButton
                }

                SystemStateManager.addLdr(
                    gpio = gpio
                )

                refreshDeviceList()
            }
            .show()
    }

    // =========================================================
    // DELETE LIGHT
    // =========================================================

    private fun showDeleteLightDialog(
        light: Light
    ) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Light")
            .setMessage(
                "Delete ${light.room}?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton("Delete") { _, _ ->

                SystemStateManager.deleteLight(
                    lightId = light.id
                )

                refreshDeviceList()
            }
            .show()
    }

    // =========================================================
    // DELETE SENSOR
    // =========================================================

    private fun showDeleteSensorDialog(
        sensor: Sensor
    ) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Sensor")
            .setMessage(
                "Delete ${sensor.name}?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton("Delete") { _, _ ->

                SystemStateManager.deleteSensor(
                    sensorId = sensor.id
                )

                refreshDeviceList()
            }
            .show()
    }

    // =========================================================
    // EDIT LIGHT
    // =========================================================

    private fun showEditLightDialog(
        light: Light
    ) {

        val input =
            EditText(requireContext())

        input.setText(light.room)

        input.inputType =
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Light")
            .setView(input)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton("Save") { _, _ ->

                val newRoom =
                    input.text
                        .toString()
                        .trim()

                if (newRoom.isEmpty()) {
                    return@setPositiveButton
                }

                light.room = newRoom

                refreshDeviceList()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}