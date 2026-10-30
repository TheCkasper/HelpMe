package com.helpme.app.feature.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private fun userRef(uid: String) = db.getReference("users").child(uid)

    val currentUid: String? get() = auth.currentUser?.uid
    val currentEmail: String get() = auth.currentUser?.email.orEmpty()

    /** Escucha cambios del perfil en tiempo real. Devuelve el listener para poder removerlo. */
    fun observeProfile(
        uid: String,
        onData: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var profile = snapshot.getValue(UserProfile::class.java)
                if (profile == null) {
                    profile = UserProfile(uid = uid, email = currentEmail)
                    // Initial creation in Realtime Database if absent
                    userRef(uid).child("email").setValue(currentEmail)
                    userRef(uid).child("rol").setValue("Ciudadano")
                }
                onData(profile.copy(uid = uid))
            }

            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        userRef(uid).addValueEventListener(listener)
        return listener
    }

    fun stopObserving(uid: String, listener: ValueEventListener) {
        userRef(uid).removeEventListener(listener)
    }

    /**
     * Usa updateChildren para NO pisar campos que otros módulos puedan guardar.
     * Si el DPI viene vacío se pasa null, lo que elimina el nodo.
     */
    fun updateProfile(
        uid: String,
        name: String,
        phone: String,
        dpi: String?,
        onResult: (Result<Unit>) -> Unit
    ) {
        val updates = mapOf<String, Any?>(
            "name" to name,
            "phone" to phone,
            "dpi" to dpi,
            "email" to currentEmail
        )
        userRef(uid).updateChildren(updates)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
