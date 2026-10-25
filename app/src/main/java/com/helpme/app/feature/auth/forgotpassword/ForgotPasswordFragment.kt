package com.helpme.app.feature.auth.forgotpassword

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
import com.helpme.app.R
import com.helpme.app.databinding.FragmentForgotPasswordBinding
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnReset.setOnClickListener {
            val email = binding.etEmail.text.toString()
            viewModel.resetPassword(email)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetState.collect { state ->
                    when (state) {
                        is OperationResult.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnReset.isEnabled = true
                        }
                        is OperationResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnReset.isEnabled = false
                        }
                        is OperationResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnReset.isEnabled = true
                            Toast.makeText(requireContext(), R.string.reset_link_sent, Toast.LENGTH_SHORT).show()
                            viewModel.resetStates()
                        }
                        is OperationResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnReset.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetStates()
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
