package com.helpme.app.feature.auth.login

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
import com.helpme.app.databinding.FragmentLoginBinding
import com.helpme.app.domain.model.AuthResult
import com.helpme.app.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        // Eliminamos referencia a btnRegister ya que no existe en el nuevo XML.

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }
        
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(requireContext(), R.string.google_sign_in_unavailable, Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sessionState.collect { state ->
                        if (state is com.helpme.app.domain.model.SessionState.Authenticated) {
                            findNavController().navigate(R.id.action_login_to_home)
                        }
                    }
                }
                launch {
                    viewModel.loginState.collect { state ->
                        when (state) {
                            is AuthResult.Idle -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true
                            }
                            is AuthResult.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.btnLogin.isEnabled = false
                            }
                            is AuthResult.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true
                                viewModel.resetStates()
                            }
                            is AuthResult.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true
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
