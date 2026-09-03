package com.example.lumen

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lumen.databinding.FragmentDashboardBinding
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!


    // =========================================================
    // LIFECYCLE
    // =========================================================

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


    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            updateDashboard()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


    // =========================================================
    // MODE BUTTONS
    // =========================================================

    private fun setupModeButtons() {

        binding.modeGroup.addOnButtonCheckedListener {

                _: MaterialButtonToggleGroup,
                checkedId: Int,
                isChecked: Boolean ->

            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

            when (checkedId) {

                binding.btnManual.id -> {

                    SystemStateManager.setMode(
                        SystemMode.MANUAL
                    )
                }

                binding.btnAuto.id -> {

                    SystemStateManager.setMode(
                        SystemMode.AUTO
                    )
                }
            }

            updateDashboard()
        }
    }


    // =========================================================
    // AMBIENT BUTTONS
    // =========================================================

    private fun setupAmbientButtons() {

        binding.ambientToggle.addOnButtonCheckedListener {

                _: MaterialButtonToggleGroup,
                checkedId: Int,
                isChecked: Boolean ->

            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

            /*
             * Ambient control is only relevant
             * when the system is in AUTO mode.
             */

            if (
                SystemStateManager.state.mode !=
                SystemMode.AUTO
            ) {
                return@addOnButtonCheckedListener
            }

            when (checkedId) {

                binding.btnDay.id -> {

                    SystemStateManager.setAmbient(
                        AmbientState.DAY
                    )
                }

                binding.btnDark.id -> {

                    SystemStateManager.setAmbient(
                        AmbientState.DARK
                    )
                }
            }

            updateDashboard()
        }
    }


    // =========================================================
    // UPDATE DASHBOARD
    // =========================================================

    private fun updateDashboard() {

        updateConnectionUI()

        updateModeUI()

        updateAmbientUI()

        setupLights()
    }


    // =========================================================
    // CONNECTION UI
    // =========================================================

    private fun updateConnectionUI() {

        val connected =
            SystemStateManager.state.connected

        if (connected) {

            binding.txtSystemConnection.text =
                "● CONNECTED"

            binding.txtSystemConnection.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )

        } else {

            binding.txtSystemConnection.text =
                "● DISCONNECTED"

            binding.txtSystemConnection.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )
        }
    }


    // =========================================================
    // MODE UI
    // =========================================================

    private fun updateModeUI() {

        when (SystemStateManager.state.mode) {

            SystemMode.MANUAL -> {

                binding.modeGroup.check(
                    binding.btnManual.id
                )

                /*
                 * Ambient control is only shown
                 * in AUTO mode.
                 */

                binding.ambientCard.visibility =
                    View.GONE
            }

            SystemMode.AUTO -> {

                binding.modeGroup.check(
                    binding.btnAuto.id
                )

                binding.ambientCard.visibility =
                    View.VISIBLE
            }
        }
    }


    // =========================================================
    // AMBIENT UI
    // =========================================================

    private fun updateAmbientUI() {

        /*
         * Nothing to update when ambient card
         * is hidden in MANUAL mode.
         */

        if (
            SystemStateManager.state.mode !=
            SystemMode.AUTO
        ) {
            return
        }

        when (SystemStateManager.state.ambient) {

            AmbientState.DAY -> {

                binding.ambientToggle.check(
                    binding.btnDay.id
                )

                binding.txtAmbientState.text =
                    "DAYLIGHT"

                binding.txtAmbientDescription.text =
                    "All lights are OFF • PIR is idle"
            }

            AmbientState.DARK -> {

                binding.ambientToggle.check(
                    binding.btnDark.id
                )

                binding.txtAmbientState.text =
                    "DARK"

                binding.txtAmbientDescription.text =
                    "PIR sensors control the lights"
            }
        }
    }


    // =========================================================
    // DYNAMIC LIGHT LIST
    // =========================================================

    private fun setupLights() {

        /*
         * Remove old cards first.
         *
         * This is important because the Devices
         * screen will eventually add/delete lights.
         */

        binding.lightContainer.removeAllViews()


        /*
         * Get the current configured lights.
         */

        val lights =
            SystemStateManager.lights


        /*
         * Create one card for every configured light.
         */

        lights.forEach { light ->

            createLightCard(light)
        }
    }


    // =========================================================
    // CREATE LIGHT CARD
    // =========================================================

    private fun createLightCard(
        light: Light
    ) {

        val itemBinding =
            com.example.lumen.databinding.ItemLightBinding.inflate(
                layoutInflater,
                binding.lightContainer,
                false
            )


        /*
         * Views from item_light.xml
         */

        val card =
            itemBinding.lightCard

        val iconContainer =
            itemBinding.lightIconContainer

        val icon =
            itemBinding.lightIcon

        val roomName =
            itemBinding.txtRoomName

        val status =
            itemBinding.txtLightStatus

        val controlInfo =
            itemBinding.txtLightControlInfo

        val lightSwitch =
            itemBinding.lightSwitch


        /*
         * Set room name.
         */

        roomName.text =
            light.room


        /*
         * Set initial switch state.
         */

        lightSwitch.isChecked =
            light.isOn


        /*
         * Update card appearance.
         */

        updateLightAppearance(
            light = light,
            card = card,
            iconContainer = iconContainer,
            icon = icon,
            status = status,
            lightSwitch = lightSwitch
        )


        /*
         * Update control information.
         */

        updateLightControlInfo(
            light = light,
            controlInfo = controlInfo,
            lightSwitch = lightSwitch
        )


        /*
         * Manual switch control.
         */

        lightSwitch.setOnCheckedChangeListener {

                _: CompoundButton,
                isChecked: Boolean ->

            /*
             * AUTO mode controls the light automatically.
             *
             * Therefore the user cannot manually
             * change the light in AUTO mode.
             */

            if (
                SystemStateManager.state.mode !=
                SystemMode.MANUAL
            ) {

                return@setOnCheckedChangeListener
            }


            /*
             * Update central state.
             */

            SystemStateManager.setLightState(
                lightId = light.id,
                isOn = isChecked
            )


            /*
             * Update this card.
             */

            updateLightAppearance(
                light = light,
                card = card,
                iconContainer = iconContainer,
                icon = icon,
                status = status,
                lightSwitch = lightSwitch
            )
        }


        /*
         * Add card to the dashboard.
         */

        binding.lightContainer.addView(
            itemBinding.root
        )
    }


    // =========================================================
    // LIGHT APPEARANCE
    // =========================================================

    private fun updateLightAppearance(
        light: Light,
        card: MaterialCardView,
        iconContainer: FrameLayout,
        icon: ImageView,
        status: TextView,
        lightSwitch: SwitchMaterial
    ) {

        if (light.isOn) {

            /*
             * =========================
             * LIGHT ON
             * =========================
             */

            card.strokeColor =
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )

            iconContainer.isSelected =
                true

            icon.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.amber
                    )
                )

            status.text =
                "ON"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )

            lightSwitch.isChecked =
                true

        } else {

            /*
             * =========================
             * LIGHT OFF
             * =========================
             */

            card.strokeColor =
                ContextCompat.getColor(
                    requireContext(),
                    R.color.border
                )

            iconContainer.isSelected =
                false

            icon.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray_text
                    )
                )

            status.text =
                "OFF"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )

            lightSwitch.isChecked =
                false
        }
    }


    // =========================================================
    // LIGHT CONTROL INFORMATION
    // =========================================================

    private fun updateLightControlInfo(
        light: Light,
        controlInfo: TextView,
        lightSwitch: SwitchMaterial
    ) {

        val state =
            SystemStateManager.state


        when (state.mode) {

            // -------------------------------------------------
            // MANUAL
            // -------------------------------------------------

            SystemMode.MANUAL -> {

                controlInfo.text =
                    "Manual control"

                lightSwitch.isEnabled =
                    state.connected
            }


            // -------------------------------------------------
            // AUTO
            // -------------------------------------------------

            SystemMode.AUTO -> {

                when (state.ambient) {

                    // -----------------------------------------
                    // AUTO + DARK
                    // -----------------------------------------

                    AmbientState.DARK -> {

                        /*
                         * Find the PIR assigned to
                         * this particular light.
                         */

                        val linkedPir =
                            SystemStateManager.sensors.find {

                                it.type == SensorType.PIR &&
                                        it.linkedLightId == light.id
                            }


                        if (linkedPir != null) {

                            controlInfo.text =
                                "${linkedPir.name} controls this light"

                        } else {

                            controlInfo.text =
                                "No PIR assigned"
                        }
                    }


                    // -----------------------------------------
                    // AUTO + DAY
                    // -----------------------------------------

                    AmbientState.DAY -> {

                        controlInfo.text =
                            "Daylight • automation paused"
                    }
                }


                /*
                 * User cannot manually control
                 * lights in AUTO mode.
                 */

                lightSwitch.isEnabled =
                    false
            }
        }
    }
}