package com.helpme.app.feature.reports

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.helpme.app.core.di.AppContainer
import com.helpme.app.databinding.FragmentReportDetailBinding
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.Report
import com.helpme.app.feature.map.LeafletMapBridge
import com.helpme.app.feature.map.ReportMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!

    private var reportId: String = ""
    private var currentReport: Report? = null

    private val reportRepository = AppContainer.reportRepository

    private val _updateState = MutableStateFlow<OperationResult>(OperationResult.Idle)
    private val updateState: StateFlow<OperationResult> = _updateState.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportId = arguments?.getString("reportId").orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateStatus.setOnClickListener {
            showStatusDialog()
        }

        binding.btnDeleteReport.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        checkAdminRoleAndConfigureButton()
        loadReportDetail()
        observeUpdateState()
    }

    private fun checkAdminRoleAndConfigureButton() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            binding.btnUpdateStatus.visibility = View.GONE
            binding.btnDeleteReport.visibility = View.GONE
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                val snap = db.child("users").child(uid).child("rol").get().await()
                val rol = snap.getValue(String::class.java).orEmpty()
                val isAdmin = rol.equals("Administrador", ignoreCase = true) || rol.equals("Admin", ignoreCase = true)

                binding.btnUpdateStatus.visibility = if (isAdmin) View.VISIBLE else View.GONE
                binding.btnDeleteReport.visibility = if (isAdmin) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                try {
                    val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    val snap = db.child("usuarios").child(uid).child("rol").get().await()
                    val rol = snap.getValue(String::class.java).orEmpty()
                    val isAdmin = rol.equals("Administrador", ignoreCase = true) || rol.equals("Admin", ignoreCase = true)

                    binding.btnUpdateStatus.visibility = if (isAdmin) View.VISIBLE else View.GONE
                    binding.btnDeleteReport.visibility = if (isAdmin) View.VISIBLE else View.GONE
                } catch (ex: Exception) {
                    binding.btnUpdateStatus.visibility = View.GONE
                    binding.btnDeleteReport.visibility = View.GONE
                }
            }
        }
    }

    private fun loadReportDetail() {
        if (reportId.isBlank()) return

        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val report = reportRepository.getReportById(reportId)
            binding.progressBar.visibility = View.GONE
            if (report != null) {
                currentReport = report
                bindReportData(report)
            } else {
                Toast.makeText(requireContext(), "No se pudo cargar la información del reporte", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindReportData(report: Report) {
        binding.tvReportTitle.text = if (report.tipo.isNotBlank()) report.tipo else "Incidencia"
        binding.tvReportCode.text = report.getFormattedCode()
        binding.tvTypeLabel.text = "Tipo: ${if (report.tipo.isNotBlank()) report.tipo else "General"}"
        binding.tvDateLabel.text = "Fecha: ${report.getFormattedDate()}"
        binding.tvLocationLabel.text = "Ubicación GPS: (${report.latitud}, ${report.longitud})"
        binding.tvDescription.text = if (report.descripcion.isNotBlank()) report.descripcion else "Sin descripción."

        // Setup Mini Map
        setupMiniMap(report)

        // Image Decoding
        if (report.fotoUrl.isNotBlank()) {
            try {
                val cleanBase64 = if (report.fotoUrl.contains(",")) {
                    report.fotoUrl.substringAfter(",")
                } else {
                    report.fotoUrl
                }
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    binding.ivReportPhoto.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                // Keep default image
            }
        }

        // Status Badge
        val (badgeBg, badgeText) = when (report.estado.trim().lowercase()) {
            "pendiente" -> Color.parseColor("#FFA000") to "Pendiente"
            "en proceso", "proceso" -> Color.parseColor("#1976D2") to "En proceso"
            "resuelto" -> Color.parseColor("#388E3C") to "Resuelto"
            else -> Color.parseColor("#757575") to report.estado
        }

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(badgeBg)
        }
        binding.tvStatusBadge.background = drawable
        binding.tvStatusBadge.text = badgeText
    }

    private fun setupMiniMap(report: Report) {
        if (report.latitud == 0.0 && report.longitud == 0.0) {
            binding.cardMapPreview.visibility = View.GONE
            return
        }

        binding.cardMapPreview.visibility = View.VISIBLE
        val webView = binding.webViewDetailMap
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        val bridge = LeafletMapBridge(
            onMarkerClick = {},
            onMapReady = {
                activity?.runOnUiThread {
                    LeafletMapBridge.setUserLocationInWebView(
                        webView, report.latitud, report.longitud, false
                    )
                    val marker = ReportMarker(
                        reporteId = report.reporteId,
                        tipo = report.tipo,
                        tipoId = report.tipoId,
                        descripcion = report.descripcion,
                        fotoUrl = report.fotoUrl,
                        latitud = report.latitud,
                        longitud = report.longitud,
                        estado = report.estado,
                        fechaCreacion = report.fechaCreacion
                    )
                    LeafletMapBridge.updateMarkersInWebView(webView, listOf(marker))
                }
            }
        )
        webView.addJavascriptInterface(bridge, LeafletMapBridge.BRIDGE_NAME)

        try {
            webView.loadUrl("file:///android_asset/map.html")
        } catch (e: Exception) {
            val html = LeafletMapBridge.getFreeMapHtml()
            webView.loadDataWithBaseURL("https://server.arcgisonline.com", html, "text/html", "UTF-8", null)
        }
    }

    private fun showStatusDialog() {
        val statuses = arrayOf("Pendiente", "En proceso", "Resuelto")
        AlertDialog.Builder(requireContext())
            .setTitle("Actualizar estado del reporte")
            .setItems(statuses) { _, which ->
                val selectedStatus = statuses[which]
                updateStatus(selectedStatus)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateStatus(newStatus: String) {
        if (reportId.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            _updateState.value = OperationResult.Loading
            val result = reportRepository.updateReportStatus(reportId, newStatus)
            _updateState.value = result
            if (result is OperationResult.Success) {
                loadReportDetail()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar reporte permanentemente")
            .setMessage("¿Estás seguro de que deseas eliminar este reporte? Esta acción lo borrará de la base de datos y no se podrá deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCurrentReport()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCurrentReport() {
        if (reportId.isBlank()) return

        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val result = reportRepository.deleteReport(reportId)
            binding.progressBar.visibility = View.GONE
            if (result is OperationResult.Success) {
                Toast.makeText(requireContext(), "Reporte eliminado exitosamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else if (result is OperationResult.Error) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeUpdateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                updateState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is OperationResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Estado actualizado exitosamente", Toast.LENGTH_SHORT).show()

                            currentReport?.let { report ->
                                com.helpme.app.core.notification.NotificationHelper.showStatusChangeNotification(
                                    context = requireContext(),
                                    reportId = report.reporteId,
                                    reportCode = report.getFormattedCode(),
                                    newStatus = report.estado
                                )
                            }

                            _updateState.value = OperationResult.Idle
                        }
                        is OperationResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            _updateState.value = OperationResult.Idle
                        }
                        is OperationResult.Idle -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        try {
            binding.webViewDetailMap.destroy()
        } catch (e: Exception) {
            // Ignore
        }
        super.onDestroyView()
        _binding = null
    }
}
