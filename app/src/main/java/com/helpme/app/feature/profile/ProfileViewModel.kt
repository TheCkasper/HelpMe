package com.helpme.app.feature.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener

class ProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository()
) : ViewModel() {

    sealed class Event {
        object Saved : Event()
        data class Error(val message: String) : Event()
    }

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?> = _event

    private var listener: ValueEventListener? = null
    private var uid: String? = null

    init {
        val currentUid = repo.currentUid
        if (currentUid == null) {
            _event.value = Event.Error("No hay sesión activa")
        } else {
            uid = currentUid
            _loading.value = true
            listener = repo.observeProfile(
                currentUid,
                onData = { _profile.value = it; _loading.value = false },
                onError = { _event.value = Event.Error(it); _loading.value = false }
            )
        }
    }

    /** Devuelve mensaje de error de validación o null si todo está bien. */
    fun validate(name: String, phone: String, dpi: String): ValidationErrors {
        return ValidationErrors(
            name = if (name.isBlank()) "El nombre es obligatorio" else null,
            phone = if (phone.isNotBlank() && phone.length < 8) "Teléfono inválido" else null,
            // DPI de Guatemala: 13 dígitos. Es opcional.
            dpi = if (dpi.isNotBlank() && !Regex("^\\d{13}$").matches(dpi)) "El DPI debe tener 13 dígitos" else null
        )
    }

    data class ValidationErrors(val name: String?, val phone: String?, val dpi: String?) {
        val hasErrors get() = name != null || phone != null || dpi != null
    }

    fun save(name: String, phone: String, dpi: String) {
        val currentUid = uid ?: return
        _loading.value = true
        repo.updateProfile(
            uid = currentUid,
            name = name.trim(),
            phone = phone.trim(),
            dpi = dpi.trim().ifBlank { null }
        ) { result ->
            _loading.value = false
            _event.value = result.fold(
                onSuccess = { Event.Saved },
                onFailure = { Event.Error(it.localizedMessage ?: "No se pudo guardar") }
            )
        }
    }

    fun eventHandled() { _event.value = null }

    override fun onCleared() {
        val u = uid
        val l = listener
        if (u != null && l != null) repo.stopObserving(u, l)
        super.onCleared()
    }
}
