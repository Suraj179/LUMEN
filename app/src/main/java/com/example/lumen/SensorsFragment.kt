package com.example.lumen

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.example.lumen.databinding.FragmentSensorsBinding

class SensorsFragment : Fragment() {

    private var _binding: FragmentSensorsBinding? = null
    private val binding get() = _binding!!

    private val state
        get() = SystemStateManager.state

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

    private fun setupTestButtons() {

        binding.btnTestPir1.setOnClickListener {

            if (state.mode != SystemMode.AUTO ||
                state.ambient != AmbientState.DARK
            ) {
                return@setOnClickListener
            }

            state.pir1 =
                if (state.pir1 == PirState.IDLE)
                    PirState.ACTIVE
                else
                    PirState.IDLE

            updateSensorUI()
        }

        binding.btnTestPir2.setOnClickListener {

            if (state.mode != SystemMode.AUTO ||
                state.ambient != AmbientState.DARK
            ) {
                return@setOnClickListener
            }

            state.pir2 =
                if (state.pir2 == PirState.IDLE)
                    PirState.ACTIVE
                else
                    PirState.IDLE

            updateSensorUI()
        }

        binding.btnTestPir3.setOnClickListener {

            if (state.mode != SystemMode.AUTO ||
                state.ambient != AmbientState.DARK
            ) {
                return@setOnClickListener
            }

            state.pir3 =
                if (state.pir3 == PirState.IDLE)
                    PirState.ACTIVE
                else
                    PirState.IDLE

            updateSensorUI()
        }
    }

    private fun updateSensorUI() {

        updateSystemStatus()

        updatePir(
            state.pir1,
            binding.pir1IconContainer,
            binding.txtPir1Status,
            binding.txtPir1Description,
            "Living Room"
        )

        updatePir(
            state.pir2,
            binding.pir2IconContainer,
            binding.txtPir2Status,
            binding.txtPir2Description,
            "Bedroom"
        )

        updatePir(
            state.pir3,
            binding.pir3IconContainer,
            binding.txtPir3Status,
            binding.txtPir3Description,
            "Kitchen"
        )

        updateLdr()
    }

    private fun updateSystemStatus() {

        when (state.mode) {

            SystemMode.MANUAL -> {

                binding.txtSensorMode.text = "MANUAL"

                binding.txtSensorModeDescription.text =
                    "PIR sensors are not controlling lights"
            }

            SystemMode.AUTO -> {

                if (state.ambient == AmbientState.DARK) {

                    binding.txtSensorMode.text = "AUTO • DARK"

                    binding.txtSensorModeDescription.text =
                        "PIR sensors are controlling the lights"

                } else {

                    binding.txtSensorMode.text = "AUTO • DAY"

                    binding.txtSensorModeDescription.text =
                        "Daylight detected • PIR lighting control paused"
                }
            }
        }
    }

    private fun updatePir(
        pirState: PirState,
        iconContainer: FrameLayout,
        status: TextView,
        description: TextView,
        room: String
    ) {

        /*
         * PIR is only meaningful when:
         *
         * AUTO + DARK
         */

        val pirIsActive =
            state.mode == SystemMode.AUTO &&
                    state.ambient == AmbientState.DARK &&
                    pirState == PirState.ACTIVE

        if (pirIsActive) {

            iconContainer.isSelected = true

            status.text = "ACTIVE"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )

            description.text =
                "Motion detected • controlling $room"

        } else {

            iconContainer.isSelected = false

            status.text = "IDLE"

            status.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_text
                )
            )

            when (state.mode) {

                SystemMode.MANUAL -> {

                    description.text =
                        "Not controlling lights"
                }

                SystemMode.AUTO -> {

                    if (state.ambient == AmbientState.DAY) {

                        description.text =
                            "Lighting control paused"

                    } else {

                        description.text =
                            "No motion detected"
                    }
                }
            }
        }
    }

    private fun updateLdr() {

        when (state.ambient) {

            AmbientState.DAY -> {

                binding.txtLdrStatus.text = "DAY"

                binding.txtLdrStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                binding.txtLdrDescription.text =
                    "Daylight detected"
            }

            AmbientState.DARK -> {

                binding.txtLdrStatus.text = "DARK"

                binding.txtLdrStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.amber
                    )
                )

                binding.txtLdrDescription.text =
                    "Dark environment detected"
            }
        }
    }

    override fun onResume() {

        super.onResume()

        /*
         * Refresh the screen whenever the user
         * returns to the Sensors fragment.
         */

        updateSensorUI()
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}