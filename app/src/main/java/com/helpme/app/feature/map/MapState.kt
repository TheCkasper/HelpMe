package com.helpme.app.feature.map

import com.helpme.app.domain.model.Report

enum class PeriodType(val label: String) {
    DAY("Día"),
    WEEK("Semana"),
    MONTH("Mes")
}

enum class StatusFilter(val label: String, val officialValue: String?) {
    ALL("Todos", null),
    PENDING("Pendiente", "Pendiente"),
    IN_PROCESS("En proceso", "En proceso"),
    RESOLVED("Resuelto", "Resuelto")
}

enum class ActivityFilter(val label: String) {
    ALL("Todas las incidencias"),
    ACTIVE_ONLY("Solo incidencias activas"),
    INACTIVE_ONLY("Solo incidencias no activas")
}

data class MapFilterState(
    val selectedPeriod: PeriodType = PeriodType.DAY,
    val referenceDateMs: Long = System.currentTimeMillis(),
    val selectedStatus: StatusFilter = StatusFilter.ALL,
    val selectedActivity: ActivityFilter = ActivityFilter.ALL,
    val selectedTypeIds: Set<String> = emptySet()
)

data class GroupedTypeStat(
    val tipoId: String,
    val tipoName: String,
    val pendingCount: Int = 0,
    val inProcessCount: Int = 0,
    val resolvedCount: Int = 0,
    val totalCount: Int = 0
)

data class StatsSummary(
    val totalReportsInPeriod: Int = 0,
    val pendingCount: Int = 0,
    val inProcessCount: Int = 0,
    val resolvedCount: Int = 0,
    val activeCount: Int = 0,
    val inactiveCount: Int = 0,
    val noLocationCount: Int = 0,
    val typeStats: List<GroupedTypeStat> = emptyList()
)

data class ReportMarker(
    val reporteId: String,
    val tipo: String,
    val tipoId: String,
    val descripcion: String,
    val fotoUrl: String,
    val latitud: Double,
    val longitud: Double,
    val estado: String,
    val fechaCreacion: Long
)

data class MapUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allReports: List<Report> = emptyList(),
    val filteredReports: List<Report> = emptyList(),
    val mapMarkers: List<ReportMarker> = emptyList(),
    val filterState: MapFilterState = MapFilterState(),
    val statsSummary: StatsSummary = StatsSummary(),
    val availableTypes: List<Pair<String, String>> = emptyList(), // (tipoId, tipoName)
    val selectedReport: Report? = null
)
