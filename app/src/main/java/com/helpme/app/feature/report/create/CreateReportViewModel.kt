package com.helpme.app.feature.report.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpme.app.core.di.AppContainer
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.Report
import com.helpme.app.domain.model.SessionState
import com.helpme.app.domain.repository.AuthRepository
import com.helpme.app.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateReportViewModel(
    private val reportRepository: ReportRepository = AppContainer.reportRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<OperationResult>(OperationResult.Idle)
    val createState: StateFlow<OperationResult> = _createState.asStateFlow()

    fun submitReport(
        tipo: String,
        tipoId: String,
        descripcion: String,
        latitud: Double,
        longitud: Double,
        fotoBase64OrUrl: String
    ) {
        if (tipo.isBlank() || tipoId.isBlank()) {
            _createState.value = OperationResult.Error("Selecciona un tipo de incidencia")
            return
        }
        if (descripcion.trim().length < 5) {
            _createState.value = OperationResult.Error("La descripción debe tener al menos 5 caracteres")
            return
        }
        if (latitud == 0.0 && longitud == 0.0) {
            _createState.value = OperationResult.Error("Captura o ingresa las coordenadas de la ubicación")
            return
        }

        viewModelScope.launch {
            _createState.value = OperationResult.Loading

            val session = authRepository.observeSession().first()
            val uid = when (session) {
                is SessionState.Authenticated -> session.uid
                else -> ""
            }

            if (uid.isBlank()) {
                _createState.value = OperationResult.Error("Debes tener una sesión activa para enviar un reporte")
                return@launch
            }

            var userName = ""
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                val snap = db.child("users").child(uid).child("name").get().await()
                userName = snap.getValue(String::class.java) ?: ""
            } catch (e: Exception) {
                // Ignore
            }

            val newReport = Report(
                tipo = tipo,
                tipoId = tipoId,
                descripcion = descripcion.trim(),
                fotoUrl = fotoBase64OrUrl,
                latitud = latitud,
                longitud = longitud,
                estado = "Pendiente",
                usuarioId = uid,
                nombreUsuario = userName,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis()
            )

            val result = reportRepository.createReport(newReport)
            _createState.value = result
        }
    }

    fun resetState() {
        _createState.value = OperationResult.Idle
    }
}
