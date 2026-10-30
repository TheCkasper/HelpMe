package com.helpme.app.feature.profile

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.helpme.app.R
import com.helpme.app.core.di.AppContainer
import com.helpme.app.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private var filled = false
    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFormEnabled(false)

        viewModel.profile.observe(viewLifecycleOwner) { p ->
            binding.tvProfileEmail.text = p.email
            if (!filled) {
                binding.tvProfileName.text = if (p.name.isNotBlank()) p.name else "Usuario HelpMe"
                binding.etName.setText(p.name)
                binding.etPhone.setText(p.phone)
                binding.etDpi.setText(p.dpi.orEmpty())
                filled = true
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !loading
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ProfileViewModel.Event.Saved -> {
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    isEditing = false
                    setFormEnabled(false)
                    val updatedName = binding.etName.text?.toString().orEmpty()
                    if (updatedName.isNotBlank()) {
                        binding.tvProfileName.text = updatedName
                    }
                }
                is ProfileViewModel.Event.Error ->
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                null -> return@observe
            }
            viewModel.eventHandled()
        }

        binding.btnSave.setOnClickListener {
            if (!isEditing) {
                isEditing = true
                setFormEnabled(true)
            } else {
                val name = binding.etName.text?.toString().orEmpty()
                val phone = binding.etPhone.text?.toString().orEmpty()
                val dpi = binding.etDpi.text?.toString().orEmpty()

                val errors = viewModel.validate(name, phone, dpi)
                binding.tilName.error = errors.name
                binding.tilPhone.error = errors.phone
                binding.tilDpi.error = errors.dpi

                if (!errors.hasErrors) viewModel.save(name, phone, dpi)
            }
        }

        binding.btnMyReports.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_report_list)
        }

        binding.btnChangePassword.setOnClickListener {
            handleChangePassword()
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }

        binding.btnHelpSupport.setOnClickListener {
            Toast.makeText(requireContext(), "HelpMe Soporte v1.0 - Red de ayuda ciudadana", Toast.LENGTH_LONG).show()
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                AppContainer.authRepository.signOut()
                findNavController().navigate(R.id.action_profile_to_login)
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.etDpi.isEnabled = enabled
        binding.btnSave.text = if (enabled) "Guardar cambios" else "Modificar datos"
    }

    private fun handleChangePassword() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        val isGoogleUser = user.providerData.any { it.providerId == "google.com" }

        if (isGoogleUser) {
            AlertDialog.Builder(requireContext())
                .setTitle("Acceso con Google (Gmail)")
                .setMessage("Iniciaste sesión con tu cuenta de Google. No es necesario cambiar contraseña ya que tu acceso está protegido de forma segura con tu cuenta de Gmail.")
                .setPositiveButton("Entendido", null)
                .show()
        } else {
            showUpdatePasswordDialog(user)
        }
    }

    private fun showUpdatePasswordDialog(user: FirebaseUser) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }

        val etNewPassword = TextInputEditText(requireContext()).apply {
            hint = "Nueva contraseña (mínimo 8 caracteres)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etConfirmPassword = TextInputEditText(requireContext()).apply {
            hint = "Confirmar nueva contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etNewPassword)
        layout.addView(etConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar contraseña")
            .setView(layout)
            .setPositiveButton("Actualizar") { _, _ ->
                val newPass = etNewPassword.text?.toString().orEmpty()
                val confirmPass = etConfirmPassword.text?.toString().orEmpty()

                if (newPass.length < 8) {
                    Toast.makeText(requireContext(), "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPass != confirmPass) {
                    Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                user.updatePassword(newPass)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "¡Contraseña actualizada exitosamente en Firebase!", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
