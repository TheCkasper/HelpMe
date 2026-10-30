package com.helpme.app.feature.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.helpme.app.R
import com.helpme.app.databinding.FragmentReportSentBinding

class ReportSentFragment : Fragment() {

    private var _binding: FragmentReportSentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportSentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportCode = arguments?.getString("reportCode") ?: "#HP-20250418-001"
        binding.tvReportCode.text = "Número de reporte\n$reportCode"

        binding.btnViewMap.setOnClickListener {
            findNavController().navigate(R.id.action_reportSent_to_map)
        }

        binding.btnGoHome.setOnClickListener {
            findNavController().navigate(R.id.action_reportSent_to_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
