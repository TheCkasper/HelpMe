package com.helpme.app.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpme.app.domain.model.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

class MapViewModel(
    private val repository: MapRepository = FirebaseMapRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.observeReports()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar los reportes de Firebase"
                    )
                }
                .collect { reports ->
                    val availableTypes = extractAvailableTypes(reports)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allReports = reports,
                        availableTypes = availableTypes,
                        errorMessage = null
                    )
                    applyCurrentFilters()
                }
        }
    }

    fun setPeriod(period: PeriodType) {
        val currentFilter = _uiState.value.filterState
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(selectedPeriod = period))
        applyCurrentFilters()
    }

    fun setReferenceDate(dateMs: Long) {
        val currentFilter = _uiState.value.filterState
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(referenceDateMs = dateMs))
        applyCurrentFilters()
    }

    fun setStatusFilter(status: StatusFilter) {
        val currentFilter = _uiState.value.filterState
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(selectedStatus = status))
        applyCurrentFilters()
    }

    fun setActivityFilter(activity: ActivityFilter) {
        val currentFilter = _uiState.value.filterState
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(selectedActivity = activity))
        applyCurrentFilters()
    }

    fun toggleTypeSelection(tipoId: String) {
        val currentFilter = _uiState.value.filterState
        val selected = currentFilter.selectedTypeIds.toMutableSet()
        if (selected.contains(tipoId)) {
            selected.remove(tipoId)
        } else {
            selected.add(tipoId)
        }
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(selectedTypeIds = selected))
        applyCurrentFilters()
    }

    fun selectAllTypes() {
        val currentFilter = _uiState.value.filterState
        val allIds = _uiState.value.availableTypes.map { it.first }.toSet()
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(selectedTypeIds = allIds))
        applyCurrentFilters()
    }

    fun deselectAllTypes() {
        val currentFilter = _uiState.value.filterState
        _uiState.value = _uiState.value.copy(filterState = currentFilter.copy(selectedTypeIds = emptySet()))
        applyCurrentFilters()
    }

    fun selectReportMarker(reportId: String) {
        val report = _uiState.value.allReports.find { it.reporteId == reportId }
        _uiState.value = _uiState.value.copy(selectedReport = report)
    }

    fun clearSelectedReport() {
        _uiState.value = _uiState.value.copy(selectedReport = null)
    }

    private fun applyCurrentFilters() {
        val currentState = _uiState.value
        val allReports = currentState.allReports
        val filter = currentState.filterState

        val (startMs, endMs) = getPeriodInterval(filter.selectedPeriod, filter.referenceDateMs)

        val filtered = allReports.filter { report ->
            // 1. Period filter (fechaCreacion in [startMs, endMs))
            val dateMatches = report.fechaCreacion in startMs until endMs

            // 2. Status filter
            val statusMatches = when (filter.selectedStatus) {
                StatusFilter.ALL -> true
                StatusFilter.PENDING -> report.estado.equals("Pendiente", ignoreCase = true)
                StatusFilter.IN_PROCESS -> report.estado.equals("En proceso", ignoreCase = true)
                StatusFilter.RESOLVED -> report.estado.equals("Resuelto", ignoreCase = true)
            }

            // 3. Activity filter
            val activityMatches = when (filter.selectedActivity) {
                ActivityFilter.ALL -> true
                ActivityFilter.ACTIVE_ONLY -> report.estado.equals("Pendiente", ignoreCase = true) ||
                        report.estado.equals("En proceso", ignoreCase = true)
                ActivityFilter.INACTIVE_ONLY -> report.estado.equals("Resuelto", ignoreCase = true)
            }

            // 4. Type filter
            val typeMatches = if (filter.selectedTypeIds.isEmpty()) {
                true // If no specific types are toggled, all types are included by default
            } else {
                val key = if (report.tipoId.isNotBlank()) report.tipoId else report.tipo
                filter.selectedTypeIds.contains(key)
            }

            dateMatches && statusMatches && activityMatches && typeMatches
        }

        // Map markers (only filtered reports with valid coordinates)
        val markers = filtered.filter { isValidCoordinate(it.latitud, it.longitud) }
            .map { report ->
                ReportMarker(
                    reporteId = report.reporteId,
                    tipo = if (report.tipo.isNotBlank()) report.tipo else "Incidencia",
                    tipoId = if (report.tipoId.isNotBlank()) report.tipoId else report.tipo,
                    descripcion = report.descripcion,
                    fotoUrl = report.fotoUrl,
                    latitud = report.latitud,
                    longitud = report.longitud,
                    estado = report.estado,
                    fechaCreacion = report.fechaCreacion
                )
            }

        // Statistics computation
        val statsSummary = calculateStats(filtered, currentState.availableTypes)

        // Keep selected report if still in filtered list, else clear
        val currentSelected = currentState.selectedReport
        val updatedSelected = if (currentSelected != null && filtered.any { it.reporteId == currentSelected.reporteId }) {
            filtered.find { it.reporteId == currentSelected.reporteId }
        } else {
            null
        }

        _uiState.value = currentState.copy(
            filteredReports = filtered,
            mapMarkers = markers,
            statsSummary = statsSummary,
            selectedReport = updatedSelected
        )
    }

    private fun calculateStats(
        filteredReports: List<Report>,
        availableTypes: List<Pair<String, String>>
    ): StatsSummary {
        var pendingCount = 0
        var inProcessCount = 0
        var resolvedCount = 0
        var noLocationCount = 0

        val groupedMap = mutableMapOf<String, GroupedTypeStat>()

        // Initialize entries for all known available types
        for ((typeId, typeName) in availableTypes) {
            groupedMap[typeId] = GroupedTypeStat(tipoId = typeId, tipoName = typeName)
        }

        for (report in filteredReports) {
            val isPending = report.estado.equals("Pendiente", ignoreCase = true)
            val isInProcess = report.estado.equals("En proceso", ignoreCase = true)
            val isResolved = report.estado.equals("Resuelto", ignoreCase = true)

            if (isPending) pendingCount++
            if (isInProcess) inProcessCount++
            if (isResolved) resolvedCount++

            if (!isValidCoordinate(report.latitud, report.longitud)) {
                noLocationCount++
            }

            val key = if (report.tipoId.isNotBlank()) report.tipoId else report.tipo
            val name = if (report.tipo.isNotBlank()) report.tipo else key
            val currentGroup = groupedMap[key] ?: GroupedTypeStat(tipoId = key, tipoName = name)

            val updatedGroup = currentGroup.copy(
                pendingCount = currentGroup.pendingCount + if (isPending) 1 else 0,
                inProcessCount = currentGroup.inProcessCount + if (isInProcess) 1 else 0,
                resolvedCount = currentGroup.resolvedCount + if (isResolved) 1 else 0,
                totalCount = currentGroup.totalCount + 1
            )
            groupedMap[key] = updatedGroup
        }

        val typeStatsList = groupedMap.values.filter { it.totalCount > 0 || availableTypes.any { t -> t.first == it.tipoId } }
            .sortedByDescending { it.totalCount }

        return StatsSummary(
            totalReportsInPeriod = filteredReports.size,
            pendingCount = pendingCount,
            inProcessCount = inProcessCount,
            resolvedCount = resolvedCount,
            activeCount = pendingCount + inProcessCount,
            inactiveCount = resolvedCount,
            noLocationCount = noLocationCount,
            typeStats = typeStatsList
        )
    }

    private fun extractAvailableTypes(reports: List<Report>): List<Pair<String, String>> {
        val map = mutableMapOf<String, String>()
        for (r in reports) {
            val key = if (r.tipoId.isNotBlank()) r.tipoId else r.tipo
            val value = if (r.tipo.isNotBlank()) r.tipo else key
            if (key.isNotBlank()) {
                map[key] = value
            }
        }
        if (map.isEmpty()) {
            // Default categories catalog as fallback
            return listOf(
                "infraestructura" to "Infraestructura",
                "seguridad" to "Seguridad",
                "servicios_publicos" to "Servicios Públicos",
                "medio_ambiente" to "Medio Ambiente",
                "otro" to "Otro"
            )
        }
        return map.toList().sortedBy { it.second }
    }

    private fun isValidCoordinate(lat: Double, lng: Double): Boolean {
        if (lat.isNaN() || lng.isNaN() || lat.isInfinite() || lng.isInfinite()) return false
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) return false
        if (lat == 0.0 && lng == 0.0) return false
        return true
    }

    private fun getPeriodInterval(periodType: PeriodType, refDateMs: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = refDateMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (periodType) {
            PeriodType.DAY -> {
                val startMs = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val endMs = cal.timeInMillis
                Pair(startMs, endMs)
            }
            PeriodType.WEEK -> {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
                val startMs = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val endMs = cal.timeInMillis
                Pair(startMs, endMs)
            }
            PeriodType.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val startMs = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val endMs = cal.timeInMillis
                Pair(startMs, endMs)
            }
        }
    }
}
