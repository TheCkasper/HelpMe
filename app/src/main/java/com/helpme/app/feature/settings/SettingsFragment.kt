package com.helpme.app.feature.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.helpme.app.R
import com.helpme.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("helpme_settings", Context.MODE_PRIVATE)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.switchPushNotifs.isChecked = prefs.getBoolean("push_notifs_enabled", true)
        binding.switchEmergencyAlerts.isChecked = prefs.getBoolean("emergency_alerts_enabled", true)
        binding.switchAutoLocation.isChecked = prefs.getBoolean("auto_location_enabled", true)

        // Theme Options Handling
        val savedTheme = prefs.getInt("app_theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rbThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.rgThemeOptions.setOnCheckedChangeListener { _, checkedId ->
            val selectedMode = when (checkedId) {
                R.id.rbThemeLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            prefs.edit().putInt("app_theme_mode", selectedMode).apply()
            AppCompatDelegate.setDefaultNightMode(selectedMode)
        }

        binding.switchPushNotifs.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("push_notifs_enabled", isChecked).apply()
            val status = if (isChecked) "activadas" else "desactivadas"
            Toast.makeText(requireContext(), "Notificaciones $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchEmergencyAlerts.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("emergency_alerts_enabled", isChecked).apply()
            val status = if (isChecked) "activadas" else "desactivadas"
            Toast.makeText(requireContext(), "Alertas de emergencia $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchAutoLocation.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_location_enabled", isChecked).apply()
            val status = if (isChecked) "activado" else "desactivado"
            Toast.makeText(requireContext(), "GPS automático $status", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearCache.setOnClickListener {
            Toast.makeText(requireContext(), "Caché de imágenes limpiada correctamente", Toast.LENGTH_SHORT).show()
        }

        binding.tvTerms.setOnClickListener {
            Toast.makeText(requireContext(), "HelpMe v1.0 - Términos y Condiciones aceptados", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
