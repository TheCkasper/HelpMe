package com.helpme.app.feature.map

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MapFragment : Fragment() {

    private val viewModel: MapViewModel by viewModels()
    private lateinit var uiBuilder: MapUiBuilder
    private var isMapReady = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchUserLocationAndCenterMap()
        } else {
            useDefaultCobanLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiBuilder = MapUiBuilder(requireContext())
        return uiBuilder.rootScrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure reference date is set to actual current date
        viewModel.setReferenceDate(System.currentTimeMillis())

        setupWebView()
        setupSpinners()
        setupButtons()
        observeUiState()
    }

    private fun setupWebView() {
        val webView = uiBuilder.webViewMap
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 HelpMeApp/1.0"

        val mapBridge = LeafletMapBridge(
            onMarkerClick = { reportId ->
                activity?.runOnUiThread {
                    viewModel.selectReportMarker(reportId)
                }
            },
            onMapReady = {
                activity?.runOnUiThread {
                    isMapReady = true
                    checkLocationAndCenterMap()
                    val state = viewModel.uiState.value
                    LeafletMapBridge.updateMarkersInWebView(uiBuilder.webViewMap, state.mapMarkers)
                }
            }
        )

        webView.addJavascriptInterface(mapBridge, LeafletMapBridge.BRIDGE_NAME)

        try {
            webView.loadUrl("file:///android_asset/map.html")
        } catch (e: Exception) {
            val htmlContent = LeafletMapBridge.getFreeMapHtml()
            webView.loadDataWithBaseURL("https://server.arcgisonline.com", htmlContent, "text/html", "UTF-8", null)
        }
    }

    private fun checkLocationAndCenterMap() {
        val finePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarsePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (finePermission == PackageManager.PERMISSION_GRANTED || coarsePermission == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocationAndCenterMap()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchUserLocationAndCenterMap() {
        val fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            useDefaultCobanLocation()
            return
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            useDefaultCobanLocation()
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            useDefaultCobanLocation()
            return
        }

        var location: Location? = null
        try {
            if (isGpsEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (location == null && isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: SecurityException) {
            // Permission denied
        }

        if (location != null) {
            LeafletMapBridge.setUserLocationInWebView(
                uiBuilder.webViewMap,
                location.latitude,
                location.longitude,
                isUserLocation = true
            )
        } else {
            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            try {
                locationManager.requestSingleUpdate(
                    provider,
                    object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            LeafletMapBridge.setUserLocationInWebView(
                                uiBuilder.webViewMap,
                                loc.latitude,
                                loc.longitude,
                                isUserLocation = true
                            )
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
                        override fun onProviderEnabled(p0: String) {}
                        override fun onProviderDisabled(p0: String) {}
                    },
                    null
                )
            } catch (e: SecurityException) {
                useDefaultCobanLocation()
            } catch (e: Exception) {
                useDefaultCobanLocation()
            }
        }
    }

    private fun useDefaultCobanLocation() {
        if (isMapReady) {
            LeafletMapBridge.setUserLocationInWebView(
                uiBuilder.webViewMap,
                LeafletMapBridge.DEFAULT_COBAN_LAT,
                LeafletMapBridge.DEFAULT_COBAN_LNG,
                isUserLocation = false
            )
        }
    }

    private fun setupSpinners() {
        val statusOptions = StatusFilter.entries.map { it.label }
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        uiBuilder.spinnerStatus.adapter = statusAdapter

        uiBuilder.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = StatusFilter.entries[position]
                viewModel.setStatusFilter(selectedStatus)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val activityOptions = ActivityFilter.entries.map { it.label }
        val activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityOptions)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        uiBuilder.spinnerActivity.adapter = activityAdapter

        uiBuilder.spinnerActivity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedActivity = ActivityFilter.entries[position]
                viewModel.setActivityFilter(selectedActivity)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        // Back to main menu
        uiBuilder.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Center on user location
        uiBuilder.btnMyLocation.setOnClickListener {
            checkLocationAndCenterMap()
        }

        uiBuilder.btnPeriodDay.setOnClickListener { viewModel.setPeriod(PeriodType.DAY) }
        uiBuilder.btnPeriodWeek.setOnClickListener { viewModel.setPeriod(PeriodType.WEEK) }
        uiBuilder.btnPeriodMonth.setOnClickListener { viewModel.setPeriod(PeriodType.MONTH) }

        uiBuilder.btnSelectDate.setOnClickListener {
            val currentRef = viewModel.uiState.value.filterState.referenceDateMs
            showDatePicker(currentRef)
        }

        uiBuilder.btnRetry.setOnClickListener { viewModel.loadReports() }
        uiBuilder.btnCloseReportCard.setOnClickListener { viewModel.clearSelectedReport() }
    }

    private fun showDatePicker(currentMs: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = currentMs }
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }
                viewModel.setReferenceDate(selectedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: MapUiState) {
        uiBuilder.loadingBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        if (state.errorMessage != null) {
            uiBuilder.errorCard.visibility = View.VISIBLE
            uiBuilder.tvErrorMsg.text = state.errorMessage
        } else {
            uiBuilder.errorCard.visibility = View.GONE
        }

        val selectedPeriod = state.filterState.selectedPeriod
        stylePeriodButtonActive(uiBuilder.btnPeriodDay, selectedPeriod == PeriodType.DAY)
        stylePeriodButtonActive(uiBuilder.btnPeriodWeek, selectedPeriod == PeriodType.WEEK)
        stylePeriodButtonActive(uiBuilder.btnPeriodMonth, selectedPeriod == PeriodType.MONTH)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date(state.filterState.referenceDateMs))
        uiBuilder.btnSelectDate.text = "Fecha de referencia: $dateStr"

        uiBuilder.updateTypesSwitches(
            availableTypes = state.availableTypes,
            selectedTypeIds = state.filterState.selectedTypeIds,
            onToggle = { tipoId -> viewModel.toggleTypeSelection(tipoId) }
        )

        val stats = state.statsSummary
        uiBuilder.tvMetricTotal.text = stats.totalReportsInPeriod.toString()
        uiBuilder.tvMetricActive.text = stats.activeCount.toString()
        uiBuilder.tvMetricResolved.text = stats.inactiveCount.toString()
        uiBuilder.tvMetricPending.text = stats.pendingCount.toString()
        uiBuilder.tvMetricInProcess.text = stats.inProcessCount.toString()
        uiBuilder.tvMetricNoLocation.text = stats.noLocationCount.toString()

        uiBuilder.populateStatsTable(stats)

        if (isMapReady) {
            LeafletMapBridge.updateMarkersInWebView(uiBuilder.webViewMap, state.mapMarkers)
        }
        uiBuilder.tvEmptyMapNotice.visibility = if (state.mapMarkers.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE

        val report = state.selectedReport
        if (report != null) {
            uiBuilder.selectedReportCard.visibility = View.VISIBLE
            uiBuilder.tvReportTitle.text = if (report.tipo.isNotBlank()) report.tipo else "Incidencia"
            uiBuilder.tvReportDesc.text = if (report.descripcion.isNotBlank()) report.descripcion else "Sin descripción"

            val createdDateStr = dateFormat.format(Date(report.fechaCreacion))
            uiBuilder.tvReportDate.text = "Fecha de creación: $createdDateStr"
            uiBuilder.tvReportCoords.text = "Ubicación GPS: (${report.latitud}, ${report.longitud})"

            val estadoLower = report.estado.lowercase().trim()
            val (badgeBg, badgeText) = when (estadoLower) {
                "pendiente" -> Color.parseColor("#FFA000") to "Pendiente"
                "en proceso", "proceso" -> Color.parseColor("#1976D2") to "En proceso"
                "resuelto" -> Color.parseColor("#388E3C") to "Resuelto"
                else -> Color.parseColor("#757575") to report.estado
            }
            uiBuilder.tvReportBadge.apply {
                text = badgeText
                setBackgroundColor(badgeBg)
            }
        } else {
            uiBuilder.selectedReportCard.visibility = View.GONE
        }
    }

    private fun stylePeriodButtonActive(btn: com.google.android.material.button.MaterialButton, isActive: Boolean) {
        if (isActive) {
            btn.setBackgroundColor(Color.parseColor("#1A237E"))
            btn.setTextColor(Color.WHITE)
        } else {
            btn.setBackgroundColor(Color.parseColor("#E0E0E0"))
            btn.setTextColor(Color.parseColor("#424242"))
        }
    }

    override fun onDestroyView() {
        uiBuilder.webViewMap.destroy()
        super.onDestroyView()
    }
}
