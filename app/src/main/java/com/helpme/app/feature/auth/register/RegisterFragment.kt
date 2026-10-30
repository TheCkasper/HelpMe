package com.helpme.app.feature.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.helpme.app.R
import com.helpme.app.databinding.FragmentRegisterBinding
import com.helpme.app.domain.model.AuthResult
import com.helpme.app.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            viewModel.register(name, email, password, confirm)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sessionState.collect { state ->
                        if (state is com.helpme.app.domain.model.SessionState.Authenticated) {
                            findNavController().navigate(R.id.action_register_to_home)
                        }
                    }
                }
                launch {
                    viewModel.registerState.collect { state ->
                        when (state) {
                            is AuthResult.Idle -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnRegister.isEnabled = true
                            }
                            is AuthResult.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.btnRegister.isEnabled = false
                            }
                            is AuthResult.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnRegister.isEnabled = true
                                findNavController().navigate(R.id.action_register_to_home)
                                viewModel.resetStates()
                            }
                            is AuthResult.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnRegister.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetStates()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
