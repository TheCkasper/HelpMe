package com.helpme.app.feature.home

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
import com.helpme.app.databinding.FragmentHomeBinding
import com.helpme.app.domain.model.SessionState
import com.helpme.app.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnProfile.setOnClickListener { showNotImplemented() }
        binding.btnNewReport.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_createReport)
        }
        binding.btnListReports.setOnClickListener { showNotImplemented() }
        binding.btnMapStats.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }
        
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessionState.collect { state ->
                    when (state) {
                        is SessionState.Unauthenticated -> {
                            findNavController().navigate(R.id.action_home_to_login)
                        }
                        is SessionState.Authenticated -> {
                            binding.tvWelcome.text = "Bienvenido, UID: ${state.uid}"
                        }
                        is SessionState.Loading -> {
                            binding.tvWelcome.text = "Cargando..."
                        }
                    }
                }
            }
        }
    }

    private fun showNotImplemented() {
        Toast.makeText(requireContext(), "Módulo en desarrollo", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
