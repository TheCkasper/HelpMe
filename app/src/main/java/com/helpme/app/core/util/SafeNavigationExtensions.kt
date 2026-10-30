package com.helpme.app.core.util

import android.os.Bundle
import androidx.navigation.NavController

fun NavController.safeNavigate(actionId: Int, args: Bundle? = null) {
    try {
        val currentDest = currentDestination
        if (currentDest != null && currentDest.getAction(actionId) != null) {
            navigate(actionId, args)
        }
    } catch (e: Exception) {
        // Prevent navigation double-click or invalid destination crashes
    }
}
