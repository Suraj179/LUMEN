package com.example.lumen

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.example.lumen.databinding.FragmentDashboardBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val state
        get() = SystemStateManager.state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(
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

        setupModeButtons()
        setupAmbientButtons()

        updateDashboard()
    }

    private fun setupModeButtons() {

        binding.btnManual.setOnClickListener {

            SystemStateManager.setMode(SystemMode.MANUAL)

            updateDashboard()
        }

        binding.btnAuto.setOnClickListener {

            SystemStateManager.setMode(SystemMode.AUTO)

            updateDashboard()
        }
    }

    private fun setupAmbientButtons() {

        binding.btnDay.setOnClickListener {

            if (state.mode != SystemMode.AUTO) {
                return@setOnClickListener
            }

            SystemStateManager.setAmbient(AmbientState.DAY)

            updateDashboard()
        }

        binding.btnDark.setOnClickListener {

            if (state.mode != SystemMode.AUTO) {
                return@setOnClickListener
            }

            SystemStateManager.setAmbient(AmbientState.DARK)

            updateDashboard()
        }
    }

    private fun updateDashboard() {

        updateConnectionStatus()
        updateModeUI()
        updateAmbientUI()
        setupLights()
    }

    private fun updateConnectionStatus() {

        if (state.connected) {

            binding.txtSystemConnection.text = "● CONNECTED"

            binding.txtSystemConnection.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )

        } else {

            binding.txtSystemConnection.text = "● DISCONNECTED"

            binding.txtSystemConnection.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )
        }
    }

    private fun updateModeUI() {

        when (state.mode) {

            SystemMode.MANUAL -> {

                binding.btnManual.isChecked = true
                binding.btnAuto.isChecked = false

                binding.ambientCard.visibility = View.GONE
            }

            SystemMode.AUTO -> {

                binding.btnManual.isChecked = false
                binding.btnAuto.isChecked = true

                binding.ambientCard.visibility = View.VISIBLE
            }
        }
    }

    private fun updateAmbientUI() {

        if (state.mode != SystemMode.AUTO) {
            return
        }

        when (state.ambient) {

            AmbientState.DAY -> {

                binding.btnDay.isChecked = true
                binding.btnDark.isChecked = false

                binding.txtAmbientState.text = "DAYLIGHT"

                binding.txtAmbientDescription.text =
                    "All lights are OFF • PIR is idle"
            }

            AmbientState.DARK -> {

                binding.btnDay.isChecked = false
                binding.btnDark.isChecked = true

                binding.txtAmbientState.text = "DARK"

                binding.txtAmbientDescription.text =
                    "PIR sensors control the lights"
            }
        }
    }

    private fun setupLights() {

        binding.lightContainer.removeAllViews()

        val lights = listOf(

            Light(
                id = 1,
                room = "Living Room",
                pirName = "PIR 1",
                mqttTopic = "home/livingroom/light1",
                isOn = state.livingRoomOn
            ),

            Light(
                id = 2,
                room = "Bedroom",
                pirName = "PIR 2",
                mqttTopic = "home/bedroom/light2",
                isOn = state.bedroomOn
            ),

            Light(
                id = 3,
                room = "Kitchen",
                pirName = "PIR 3",
                mqttTopic = "home/kitchen/light3",
                isOn = state.kitchenOn
            )
        )

        lights.forEach { light ->

            val card = createLightCard(light)

            binding.lightContainer.addView(card)
        }
    }

    private fun createLightCard(light: Light): View {

        val view = layoutInflater.inflate(
            R.layout.item_light,
            binding.lightContainer,
            false
        )

        val card =
            view.findViewById<MaterialCardView>(R.id.lightCard)

        val iconContainer =
            view.findViewById<FrameLayout>(R.id.lightIconContainer)

        val icon =
            view.findViewById<ImageView>(R.id.lightIcon)

        val roomName =
            view.findViewById<TextView>(R.id.txtRoomName)

        val status =
            view.findViewById<TextView>(R.id.txtLightStatus)

        val controlInfo =
            view.findViewById<TextView>(R.id.txtLightControlInfo)

        val lightSwitch =
            view.findViewById<SwitchMaterial>(R.id.lightSwitch)

        roomName.text = light.room

        updateLightAppearance(
            light,
            card,
            iconContainer,
            icon,
            status,
            lightSwitch
        )

        updateLightControlInfo(
            light,
            controlInfo,
            lightSwitch
        )

        lightSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (state.mode != SystemMode.MANUAL) {
                return@setOnCheckedChangeListener
            }

            light.isOn = isChecked

            when (light.id) {

                1 -> state.livingRoomOn = isChecked

                2 -> state.bedroomOn = isChecked

                3 -> state.kitchenOn = isChecked
            }

            updateLightAppearance(
                light,
                card,
                iconContainer,
                icon,
                status,
                lightSwitch
            )
        }

        return view
    }

    private fun updateLightAppearance(
        light: Light,
        card: MaterialCardView,
        iconContainer: FrameLayout,
        icon: ImageView,
        status: TextView,
        lightSwitch: SwitchMaterial
    ) {

        if (light.isOn) {

            card.strokeColor =
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )

            iconContainer.isSelected = true

            icon.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.amber
                    )
                )

            status.text = "ON"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )

            lightSwitch.isChecked = true

        } else {

            card.strokeColor =
                ContextCompat.getColor(
                    requireContext(),
                    R.color.border
                )

            iconContainer.isSelected = false

            icon.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray_text
                    )
                )

            status.text = "OFF"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )

            lightSwitch.isChecked = false
        }
    }

    private fun updateLightControlInfo(
        light: Light,
        controlInfo: TextView,
        lightSwitch: SwitchMaterial
    ) {

        when (state.mode) {

            SystemMode.MANUAL -> {

                controlInfo.text = "Manual control"

                lightSwitch.isEnabled = state.connected
            }

            SystemMode.AUTO -> {

                if (state.ambient == AmbientState.DARK) {

                    controlInfo.text =
                        "${light.pirName} controls this light"

                } else {

                    controlInfo.text =
                        "Daylight • automation paused"
                }

                lightSwitch.isEnabled = false
            }
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}