package com.helpme.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.helpme.app.core.notification.GlobalNotificationListener
import com.helpme.app.core.notification.NotificationForegroundService

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            AlertDialog.Builder(this)
                .setTitle("Permiso de Notificaciones Requerido")
                .setMessage("HelpMe necesita el permiso de notificaciones para alertarte en tiempo real sobre nuevas emergencias comunitarias publicadas en tu zona.")
                .setPositiveButton("Conceder Permiso") { _, _ ->
                    checkNotificationPermission()
                }
                .setNegativeButton("Continuar sin alertas", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        checkNotificationPermission()
        startNotificationService()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = if (
                destination.id == R.id.homeFragment ||
                destination.id == R.id.reportListFragment ||
                destination.id == R.id.reportCreateFragment ||
                destination.id == R.id.reportDetailFragment ||
                destination.id == R.id.mapFragment ||
                destination.id == R.id.profileFragment
            ) View.VISIBLE else View.GONE
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        handleNotificationIntent(intent)
    }

    private fun startNotificationService() {
        try {
            val serviceIntent = Intent(this, NotificationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            GlobalNotificationListener.startListening(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val reportId = intent?.getStringExtra("navigate_to_report_id")
        if (!reportId.isNullOrBlank()) {
            intent.removeExtra("navigate_to_report_id")
            val bundle = bundleOf("reportId" to reportId)
            navController.navigate(R.id.reportDetailFragment, bundle)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
