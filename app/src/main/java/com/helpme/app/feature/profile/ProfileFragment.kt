package com.helpme.app.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = TextView(requireContext()).apply {
            text = "Perfil (Módulo de Marcos - En construcción)"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
        return view
    }
}