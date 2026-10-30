package com.helpme.app.feature.auth.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.helpme.app.domain.model.SessionState
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
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    showErrorDialog(
                        "Error de Token Google",
                        "Google autenticó la cuenta pero no devolvió un Web ID Token. Verifica que el servicio de Google Sign-In esté activo en Firebase Console."
                    )
                }
            } catch (e: ApiException) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                showErrorDialog(
                    "Error de Google (${e.statusCode})",
                    getGoogleErrorMessage(e.statusCode)
                )
            }
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
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
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                client.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(client.signInIntent)
                }
            } else {
                Toast.makeText(requireContext(), "Google Sign-In no inicializado.", Toast.LENGTH_LONG).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sessionState.collect { state ->
                        if (state is SessionState.Authenticated) {
                            navigateToHomeSafely()
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
                                navigateToHomeSafely()
                                viewModel.resetStates()
                            }
                            is AuthResult.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true
                                showErrorDialog("Error al iniciar sesión", state.message)
                                viewModel.resetStates()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToHomeSafely() {
        try {
            if (findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(R.id.action_login_to_home)
            }
        } catch (e: Exception) {
            // Already navigated
        }
    }

    private fun setupGoogleSignIn() {
        try {
            val webClientId = "413805580217-tmi6lsosj8oilcukt62sger7597eemrv.apps.googleusercontent.com"

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        } catch (e: Exception) {
            googleSignInClient = null
        }
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun getGoogleErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            10 -> "Código 10 (DEVELOPER_ERROR): Ocurre si el proveedor Google Sign-In no está habilitado en Firebase Console o la huella SHA-1 no coincide."
            12500 -> "Código 12500 (SIGN_IN_FAILED): La cuenta de Google rechazó la autenticación. Intenta con otro correo de Gmail."
            7 -> "Código 7 (NETWORK_ERROR): Error de conexión a Internet en el dispositivo."
            else -> "Código $statusCode: No se pudo completar la autenticación con Google."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
