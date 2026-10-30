package com.helpme.app.feature.auth.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.helpme.app.R
import com.helpme.app.databinding.FragmentLoginBinding
import com.helpme.app.domain.model.AuthResult
import com.helpme.app.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    private var googleSignInClient: GoogleSignInClient? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    viewModel.loginWithGoogle(idToken)
                } else {
                    Toast.makeText(requireContext(), "No se obtuvo token de Google. Verifica habilitar Google en Firebase.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Error de Google (${e.statusCode}): Habilita Google en Firebase Console", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleSignIn()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }

        binding.btnGoogle.setOnClickListener {
            val client = googleSignInClient
            if (client != null) {
                googleSignInLauncher.launch(client.signInIntent)
            } else {
                Toast.makeText(requireContext(), "Google Sign-In: Habilita el proveedor Google en Firebase Console.", Toast.LENGTH_LONG).show()
            }
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

    private fun setupGoogleSignIn() {
        try {
            val webClientIdResId = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
            val webClientId = if (webClientIdResId != 0) getString(webClientIdResId) else ""

            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()

            if (webClientId.isNotBlank()) {
                gsoBuilder.requestIdToken(webClientId)
            }

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gsoBuilder.build())
        } catch (e: Exception) {
            googleSignInClient = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
