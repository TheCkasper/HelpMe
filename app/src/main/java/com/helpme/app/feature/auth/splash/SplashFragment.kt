package com.helpme.app.feature.auth.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.helpme.app.R
import com.helpme.app.domain.model.SessionState
import com.helpme.app.feature.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1200) // Show splash for 1.2s
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessionState.collect { session ->
                    when (session) {
                        is SessionState.Authenticated -> {
                            findNavController().navigate(R.id.action_splash_to_home)
                        }
                        is SessionState.Unauthenticated -> {
                            findNavController().navigate(R.id.action_splash_to_login)
                        }
                        is SessionState.Loading -> {
                            // Wait for loading
                        }
                    }
                }
            }
        }
    }
}
