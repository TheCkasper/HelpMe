package com.helpme.app.feature.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.helpme.app.R
import com.helpme.app.databinding.FragmentTypeSelectionBinding

class TypeSelectionFragment : Fragment() {

    private var _binding: FragmentTypeSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTypeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            val (typeLabel, typeId) = when (binding.rgTypes.checkedRadioButtonId) {
                R.id.rbMedical -> "Emergencia médica" to "INC_SALUD"
                R.id.rbSecurity -> "Seguridad" to "INC_SEGURIDAD"
                R.id.rbDisaster -> "Desastres naturales" to "INC_DESASTRE"
                R.id.rbInfra -> "Infraestructura" to "INC_INFRA"
                R.id.rbServices -> "Servicios básicos" to "INC_SERVICIOS"
                R.id.rbOther -> "Otros" to "INC_OTRO"
                else -> "" to ""
            }

            if (typeLabel.isBlank()) {
                Toast.makeText(requireContext(), "Selecciona un tipo de incidencia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = bundleOf("selectedTypeLabel" to typeLabel, "selectedTypeId" to typeId)
            findNavController().navigate(R.id.action_typeSelection_to_createReport, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
