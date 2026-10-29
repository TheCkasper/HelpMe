package com.helpme.app.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.helpme.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    // Evita pisar lo que el usuario está escribiendo cuando llega una actualización de la BD
    private var filled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.profile.observe(viewLifecycleOwner) { p ->
            binding.tvEmail.setText(p.email)
            if (!filled) {
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
                is ProfileViewModel.Event.Saved ->
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                is ProfileViewModel.Event.Error ->
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                null -> return@observe
            }
            viewModel.eventHandled()
        }

        binding.btnSave.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
