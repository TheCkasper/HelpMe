package com.helpme.app.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpme.app.core.di.AppContainer
import com.helpme.app.domain.model.Report
import com.helpme.app.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ReportListViewModel(
    private val reportRepository: ReportRepository = AppContainer.reportRepository
) : ViewModel() {

    private val _allReports = MutableStateFlow<List<Report>>(emptyList())
    private val _filterStatus = MutableStateFlow("ALL") // ALL, Pendiente, Resuelto
    private val _isLoading = MutableStateFlow(true)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredReports: StateFlow<List<Report>> = combine(_allReports, _filterStatus) { reports, filter ->
        when (filter) {
            "Pendiente" -> reports.filter { it.estado.equals("Pendiente", ignoreCase = true) }
            "Resuelto" -> reports.filter { it.estado.equals("Resuelto", ignoreCase = true) }
            else -> reports
        }
    }.let { flow ->
        val state = MutableStateFlow<List<Report>>(emptyList())
        viewModelScope.launch { flow.collect { state.value = it } }
        state.asStateFlow()
    }

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                reportRepository.observeReports().collect { list ->
                    _allReports.value = list
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }
}
