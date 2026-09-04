package com.example.lumen

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lumen.databinding.FragmentDevicesBinding
import com.example.lumen.databinding.ItemDeviceLightBinding
import com.example.lumen.databinding.ItemDeviceSensorBinding
import androidx.core.content.ContextCompat
import android.view.Window
class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentDevicesBinding.inflate(
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
        super.onViewCreated(
            view,
            savedInstanceState
        )

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
    // DEVICE LIST
    // =========================================================

    private fun refreshDeviceList() {

        binding.deviceList.removeAllViews()

        val lights =
            SystemStateManager.lights

        val sensors =
            SystemStateManager.sensors

        val totalDevices =
            lights.size + sensors.size

        binding.txtDeviceCount.text =
            getString(
                R.string.device_count_format,
                totalDevices
            )

        lights.forEach { light ->
            createLightCard(light)
        }

        sensors.forEach { sensor ->
            createSensorCard(sensor)
        }
    }

    // =========================================================
    // LIGHT CARD
    // =========================================================

    private fun createLightCard(light: Light) {
        val itemBinding = ItemDeviceLightBinding.inflate(
            layoutInflater,
            binding.deviceList,
            false
        )

        itemBinding.txtDeviceLightName.text = getString(
            R.string.light_name_format,
            light.id
        )

        itemBinding.txtDeviceLightRoom.text = light.room

        itemBinding.txtDeviceLightTopic.text = getString(
            R.string.topic_format,
            light.mqttTopic
        )

        // Update text, text color, and background based on light.isOn
        if (light.isOn) {
            itemBinding.txtDeviceLightStatus.text = getString(R.string.on)
            itemBinding.txtDeviceLightStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.amber)
            )
            itemBinding.txtDeviceLightStatus.setBackgroundResource(R.drawable.bg_status_on)
        } else {
            itemBinding.txtDeviceLightStatus.text = getString(R.string.off)
            itemBinding.txtDeviceLightStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.gray_text) // Replace with your OFF text color
            )
            itemBinding.txtDeviceLightStatus.setBackgroundResource(R.drawable.bg_status_off) // Replace with your OFF background drawable
        }

        itemBinding.btnEditLight.setOnClickListener {
            showEditLightDialog(light)
        }

        itemBinding.btnDeleteLight.setOnClickListener {
            showDeleteLightDialog(light)
        }

        binding.deviceList.addView(itemBinding.root)
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

            // PIR can be edited
            itemBinding.btnEditSensor.visibility =
                View.VISIBLE

            itemBinding.btnEditSensor.setOnClickListener {
                showEditPirDialog(sensor)
            }

        } else {

            itemBinding.txtDeviceSensorInfo.text =
                gpioText

            // LDR cannot currently be edited
            itemBinding.btnEditSensor.visibility =
                View.GONE
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

        val options =
            arrayOf(
                "Light",
                "PIR Sensor",
                "LDR"
            )

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(
                getString(
                    R.string.add_device
                )
            )
            .setItems(options) { _, which ->

                when (which) {

                    0 ->
                        showAddLightDialog()

                    1 ->
                        showAddPirDialog()

                    2 ->
                        showAddLdrDialog()
                }
            }
            .create()
        dialog.show()
        styleDialog(dialog)
    }

    // =========================================================
    // ADD LIGHT
    // =========================================================

    private fun showAddLightDialog() {

        val input =
            EditText(requireContext()).apply {

                hint = "Room name"

                inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS

                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mist
                    )
                )

                setHintTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray_text
                    )
                )
            }

        applyDialogPadding(input)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Light")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->

                val room =
                    input.text
                        .toString()
                        .trim()

                if (room.isEmpty()) {

                    showValidationError(
                        "Room name cannot be empty."
                    )

                    return@setPositiveButton
                }

                SystemStateManager.addLight(
                    room = room
                )

                refreshDeviceList()
            }
            .create()

        dialog.show()

        styleDialog(dialog)
    }
    // =========================================================
    // ADD PIR
    // =========================================================

    private fun showAddPirDialog() {

        val lights =
            SystemStateManager.lights

        if (lights.isEmpty()) {

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(
                    "No Lights Available"
                )
                .setMessage(
                    "Add a Light first before creating a PIR sensor."
                )
                .setPositiveButton(
                    "OK",
                    null
                )
                .create()
            dialog.show()
            styleDialog(dialog)

            return
        }

        val assignedLightIds =
            SystemStateManager.sensors
                .filter {
                    it.type == SensorType.PIR
                }
                .mapNotNull {
                    it.linkedLightId
                }
                .toSet()

        val layout =
            LinearLayout(requireContext()).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24.dp,
                    8.dp,
                    24.dp,
                    8.dp
                )
            }

        val nameInput =
            EditText(requireContext()).apply {

                hint = "Sensor name"

                inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }

        val gpioInput =
            EditText(requireContext()).apply {

                hint = "GPIO number"

                inputType =
                    InputType.TYPE_CLASS_NUMBER
            }

        val lightLabel =
            TextView(requireContext()).apply {

                text = "Controls"

                setTextColor(
                    resources.getColor(
                        R.color.mist,
                        null
                    )
                )

                textSize = 13f
            }

        val lightSpinner =
            Spinner(requireContext())

        val lightNames =
            lights.map { light ->

                if (
                    assignedLightIds.contains(
                        light.id
                    )
                ) {

                    "Light ${light.id} — ${light.room} (Already assigned)"

                } else {

                    "Light ${light.id} — ${light.room}"
                }
            }

        val spinnerAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                lightNames
            )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        lightSpinner.adapter =
            spinnerAdapter

        layout.addView(
            nameInput
        )

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

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(
                "Add PIR Sensor"
            )
            .setView(layout)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Add"
            ) { _, _ ->

                val name =
                    nameInput.text
                        .toString()
                        .trim()

                val gpio =
                    gpioInput.text
                        .toString()
                        .toIntOrNull()

                if (name.isEmpty()) {

                    showValidationError(
                        "Sensor name cannot be empty."
                    )

                    return@setPositiveButton
                }

                if (gpio == null) {

                    showValidationError(
                        "Enter a valid GPIO number."
                    )

                    return@setPositiveButton
                }

                if (!isValidEsp32Gpio(gpio)) {

                    showValidationError(
                        "GPIO $gpio is not a valid ESP32 GPIO."
                    )

                    return@setPositiveButton
                }

                if (
                    SystemStateManager.isGpioInUse(
                        gpio
                    )
                ) {

                    showValidationError(
                        "GPIO $gpio is already being used by another sensor."
                    )

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

                if (
                    SystemStateManager.isLightAssignedToPir(
                        selectedLight.id
                    )
                ) {

                    showValidationError(
                        "${selectedLight.room} is already assigned to another PIR sensor."
                    )

                    return@setPositiveButton
                }

                SystemStateManager.addPir(
                    name = name,
                    gpio = gpio,
                    linkedLightId =
                        selectedLight.id
                )

                refreshDeviceList()
            }
            .create()
        dialog.show()
        styleDialog(dialog)
    }

    // =========================================================
    // ADD LDR
    // =========================================================

    private fun showAddLdrDialog() {

        if (
            SystemStateManager.hasLdr()
        ) {

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(
                    "LDR already exists"
                )
                .setMessage(
                    "Only one LDR is currently supported."
                )
                .setPositiveButton(
                    "OK",
                    null
                )
                .create()
            dialog.show()
            styleDialog(dialog)

            return
        }

        val input =
            EditText(requireContext()).apply {

                hint = "GPIO number"

                inputType =
                    InputType.TYPE_CLASS_NUMBER

                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mist
                    )
                )
                setHintTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray_text
                    )
                )

            }
        applyDialogPadding(input)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add LDR")
            .setView(input)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Add"
            ) { _, _ ->

                val gpio =
                    input.text
                        .toString()
                        .toIntOrNull()

                if (gpio == null) {

                    showValidationError(
                        "Enter a valid GPIO number."
                    )

                    return@setPositiveButton
                }

                if (!isValidEsp32Gpio(gpio)) {

                    showValidationError(
                        "GPIO $gpio is not a valid ESP32 GPIO."
                    )

                    return@setPositiveButton
                }

                if (
                    SystemStateManager.isGpioInUse(
                        gpio
                    )
                ) {

                    showValidationError(
                        "GPIO $gpio is already being used by another sensor."
                    )

                    return@setPositiveButton
                }

                SystemStateManager.addLdr(
                    gpio = gpio
                )

                refreshDeviceList()
            }
            .create()
        dialog.show()
        styleDialog(dialog)
    }

    // =========================================================
    // EDIT LIGHT
    // =========================================================

    private fun showEditLightDialog(
        light: Light
    ) {

        val input =
            EditText(requireContext()).apply {

                setText(light.room)

                inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mist
                    )
                )
            }
        applyDialogPadding(input)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Light")
            .setView(input)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                val newRoom =
                    input.text
                        .toString()
                        .trim()

                if (newRoom.isEmpty()) {

                    showValidationError(
                        "Room name cannot be empty."
                    )

                    return@setPositiveButton
                }

                light.room =
                    newRoom

                refreshDeviceList()
            }
            .create()
        dialog.show()
        styleDialog(dialog)
    }

    // =========================================================
    // EDIT PIR
    // =========================================================

    private fun showEditPirDialog(
        sensor: Sensor
    ) {

        val lights =
            SystemStateManager.lights

        if (lights.isEmpty()) {
            return
        }

        val layout =
            LinearLayout(requireContext()).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24.dp,
                    8.dp,
                    24.dp,
                    8.dp
                )
            }

        // -------------------------
        // Sensor name
        // -------------------------

        val nameInput =
            EditText(requireContext()).apply {

                setText(sensor.name)

                inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }

        // -------------------------
        // GPIO
        // -------------------------

        val gpioInput =
            EditText(requireContext()).apply {

                setText(
                    sensor.gpio.toString()
                )

                inputType =
                    InputType.TYPE_CLASS_NUMBER
            }

        // -------------------------
        // Light selection
        // -------------------------

        val lightLabel =
            TextView(requireContext()).apply {

                text = "Controls"

                setTextColor(
                    resources.getColor(
                        R.color.mist,
                        null
                    )
                )

                textSize = 13f
            }

        val lightSpinner =
            Spinner(requireContext())

        val assignedLightIds =
            SystemStateManager.sensors
                .filter {
                    it.type == SensorType.PIR
                }
                .filter {
                    it.id != sensor.id
                }
                .mapNotNull {
                    it.linkedLightId
                }
                .toSet()

        val lightNames =
            lights.map { light ->

                when {

                    light.id == sensor.linkedLightId ->
                        "Light ${light.id} — ${light.room} (Current)"

                    assignedLightIds.contains(
                        light.id
                    ) ->
                        "Light ${light.id} — ${light.room} (Already assigned)"

                    else ->
                        "Light ${light.id} — ${light.room}"
                }
            }

        val spinnerAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                lightNames
            )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        lightSpinner.adapter =
            spinnerAdapter

        // Select current light
        val currentPosition =
            lights.indexOfFirst {
                it.id == sensor.linkedLightId
            }

        if (currentPosition >= 0) {

            lightSpinner.setSelection(
                currentPosition
            )
        }

        layout.addView(
            nameInput
        )

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

        layout.addView(
            lightSpinner
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(
                "Edit PIR Sensor"
            )
            .setView(layout)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                val name =
                    nameInput.text
                        .toString()
                        .trim()

                val gpio =
                    gpioInput.text
                        .toString()
                        .toIntOrNull()

                if (name.isEmpty()) {

                    showValidationError(
                        "Sensor name cannot be empty."
                    )

                    return@setPositiveButton
                }

                if (gpio == null) {

                    showValidationError(
                        "Enter a valid GPIO number."
                    )

                    return@setPositiveButton
                }

                if (!isValidEsp32Gpio(gpio)) {

                    showValidationError(
                        "GPIO $gpio is not a valid ESP32 GPIO."
                    )

                    return@setPositiveButton
                }

                if (
                    SystemStateManager.isGpioInUse(
                        gpio,
                        excludingSensorId =
                            sensor.id
                    )
                ) {

                    showValidationError(
                        "GPIO $gpio is already being used by another sensor."
                    )

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

                if (
                    SystemStateManager.isLightAssignedToPir(
                        selectedLight.id,
                        excludingSensorId =
                            sensor.id
                    )
                ) {

                    showValidationError(
                        "${selectedLight.room} is already assigned to another PIR sensor."
                    )

                    return@setPositiveButton
                }

                SystemStateManager.updatePir(
                    sensorId = sensor.id,
                    name = name,
                    gpio = gpio,
                    linkedLightId =
                        selectedLight.id
                )

                refreshDeviceList()
            }
            .create()
        dialog.show()
        styleDialog(dialog)
    }

    // =========================================================
    // DELETE LIGHT
    // =========================================================

    private fun showDeleteLightDialog(
        light: Light
    ) {

        val dialog= AlertDialog.Builder(requireContext())
            .setTitle("Delete Light")
            .setMessage(
                "Delete ${light.room}?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                SystemStateManager.deleteLight(
                    lightId = light.id
                )

                refreshDeviceList()
            }
            .create()
        dialog.show()
        styleDialog(dialog)
        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        )?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.danger
            )
        )
    }

    // =========================================================
    // DELETE SENSOR
    // =========================================================

    private fun showDeleteSensorDialog(
        sensor: Sensor
    ) {

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Sensor")
            .setMessage(
                "Delete ${sensor.name}?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                SystemStateManager.deleteSensor(
                    sensorId = sensor.id
                )

                refreshDeviceList()
            }
            .create()
        dialog.show()
        styleDialog(dialog)
        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        )?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.danger
            )
        )
    }

    // =========================================================
    // GPIO VALIDATION
    // =========================================================

    private fun isValidEsp32Gpio(
        gpio: Int
    ): Boolean {

        // GPIO 34-39 are input-only.
        // They are valid for sensors such as LDR,
        // but not for output devices.

        return gpio in 0..39
    }

    // =========================================================
    // VALIDATION MESSAGE
    // =========================================================

    private fun showValidationError(
        message: String
    ) {

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Invalid Device")
            .setMessage(message)
            .setPositiveButton(
                "OK",
                null
            )
            .create()

        dialog.show()

        styleDialog(dialog)
    }

    private fun applyDialogPadding(view: View) {

        view.setPadding(
            24.dp,
            8.dp,
            24.dp,
            8.dp
        )
    }

    private fun styleDialog(dialog: AlertDialog) {

        val window = dialog.window ?: return

        // Rounded background
        window.setBackgroundDrawableResource(
            R.drawable.bg_dialog
        )

        // Dialog width = 90% of screen
        val width =
            (resources.displayMetrics.widthPixels * 0.90).toInt()

        window.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Title
        val titleId = resources.getIdentifier(
            "alertTitle",
            "id",
            requireContext().packageName
        )

        dialog.findViewById<TextView>(titleId)?.apply {
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.mist
                )
            )

            textSize = 20f
        }

        // Positive button
        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        )?.apply {

            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )
        }

        // Negative button
        dialog.getButton(
            AlertDialog.BUTTON_NEGATIVE
        )?.apply {

            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.mist
                )
            )
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
