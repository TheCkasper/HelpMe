package com.helpme.app.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.helpme.app.R
import com.helpme.app.core.di.AppContainer
import com.helpme.app.databinding.FragmentHomeBinding
import com.helpme.app.domain.model.SessionState
import com.helpme.app.feature.auth.AuthViewModel
import com.helpme.app.feature.reports.adapter.ReportAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private val reportRepository = AppContainer.reportRepository
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeSession()
        loadRecentReports()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter { report ->
            val bundle = bundleOf("reportId" to report.reporteId)
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }
        binding.rvRecentReports.adapter = reportAdapter
    }

    private fun setupListeners() {
        binding.cardCreateReport.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_report_create)
        }

        binding.cardViewMap.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_report_list)
        }
    }

    private fun observeSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessionState.collect { state ->
                    when (state) {
                        is SessionState.Unauthenticated -> {
                            findNavController().navigate(R.id.action_home_to_login)
                        }
                        is SessionState.Authenticated -> {
                            loadUserName(state.uid)
                        }
                        is SessionState.Loading -> {
                            binding.tvWelcome.text = getString(R.string.loading)
                        }
                    }
                }
            }
        }
    }

    private fun loadUserName(uid: String) {
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
        db.child("users").child(uid).child("name").get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: ""
                if (name.isNotBlank()) {
                    binding.tvWelcome.text = "Bienvenido, $name"
                } else {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val fallback = user?.displayName?.ifBlank { null } ?: user?.email?.ifBlank { null } ?: uid
                    binding.tvWelcome.text = "Bienvenido, $fallback"
                }
            }
            .addOnFailureListener {
                binding.tvWelcome.text = "Bienvenido"
            }
    }

    private fun loadRecentReports() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reportRepository.observeReports().collect { list ->
                    binding.progressBar.visibility = View.GONE
                    // Take top 3 recent reports
                    reportAdapter.submitList(list.take(3))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
