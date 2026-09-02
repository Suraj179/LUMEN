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

        setupLights()
    }


    private fun setupLights() {

        val lights = listOf(

            Light(
                id = 1,
                room = "Living Room",
                pirName = "PIR 1",
                mqttTopic = "home/livingroom/light1"
            ),

            Light(
                id = 2,
                room = "Bedroom",
                pirName = "PIR 2",
                mqttTopic = "home/bedroom/light2"
            ),

            Light(
                id = 3,
                room = "Kitchen",
                pirName = "PIR 3",
                mqttTopic = "home/kitchen/light3"
            )
        )


        binding.lightContainer.removeAllViews()


        for (light in lights) {

            val lightCard = createLightCard(light)

            binding.lightContainer.addView(lightCard)
        }
    }


    private fun createLightCard(light: Light): View {

        val view = layoutInflater.inflate(
            R.layout.item_light,
            binding.lightContainer,
            false
        )


        val card = view.findViewById<MaterialCardView>(
            R.id.lightCard
        )

        val iconContainer = view.findViewById<FrameLayout>(
            R.id.lightIconContainer
        )

        val icon = view.findViewById<ImageView>(
            R.id.lightIcon
        )

        val roomName = view.findViewById<TextView>(
            R.id.txtRoomName
        )

        val status = view.findViewById<TextView>(
            R.id.txtLightStatus
        )

        val lightSwitch = view.findViewById<SwitchMaterial>(
            R.id.lightSwitch
        )


        roomName.text = light.room


        // Display initial state
        updateLightAppearance(
            light = light,
            card = card,
            iconContainer = iconContainer,
            icon = icon,
            status = status,
            lightSwitch = lightSwitch
        )


        lightSwitch.setOnCheckedChangeListener { _, isChecked ->

            light.isOn = isChecked


            updateLightAppearance(
                light = light,
                card = card,
                iconContainer = iconContainer,
                icon = icon,
                status = status,
                lightSwitch = lightSwitch
            )


            /*
             * MQTT will eventually go here.
             *
             * Example:
             *
             * mqttManager.publish(
             *     light.mqttTopic,
             *     if (isChecked) "ON" else "OFF"
             * )
             */
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

            // -------------------------
            // ON STATE
            // -------------------------

            // Card border
            card.strokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.amber
            )


            // Icon background
            iconContainer.isSelected = true


            // Bulb icon
            icon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )


            // Status text
            status.text = "ON"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )


            // Switch
            lightSwitch.isChecked = true

        } else {

            // -------------------------
            // OFF STATE
            // -------------------------

            // Card border
            card.strokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.border
            )


            // Icon background
            iconContainer.isSelected = false


            // Bulb icon
            icon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )


            // Status text
            status.text = "OFF"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )


            // Switch
            lightSwitch.isChecked = false
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}