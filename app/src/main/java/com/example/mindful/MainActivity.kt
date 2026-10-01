package com.example.mindful

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requestOverlayPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { updateStatus() }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { updateStatus() }

    private lateinit var statusText: TextView
    private lateinit var enableButton: Button
    private lateinit var overlayButton: Button
    private lateinit var batteryButton: Button
    private lateinit var testPromptButton: Button
    private lateinit var stopPromptsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        enableButton = findViewById(R.id.enableButton)
        overlayButton = findViewById(R.id.overlayButton)
        batteryButton = findViewById(R.id.batteryButton)
        testPromptButton = findViewById(R.id.testPromptButton)
        stopPromptsButton = findViewById(R.id.stopPromptsButton)

        overlayButton.setOnClickListener { requestOverlayPermission() }
        batteryButton.setOnClickListener { requestBatteryExemption() }
        enableButton.setOnClickListener { startMonitoring() }
        stopPromptsButton.setOnClickListener { stopMonitoring() }
        testPromptButton.setOnClickListener { UnlockPrompt.show(this) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0,
            )
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) return
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        requestOverlayPermission.launch(intent)
    }

    private fun requestBatteryExemption() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) return
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun startMonitoring() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        MindfulPrefs.setMonitoringEnabled(this, true)
        UnlockMonitorService.start(this)
        updateStatus()
    }

    private fun stopMonitoring() {
        UnlockMonitorService.stop(this)
        Toast.makeText(this, R.string.setup_stopped, Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun updateStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        val batteryOk = (getSystemService(POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(packageName)
        val monitoring = MindfulPrefs.isMonitoringEnabled(this)

        overlayButton.visibility = if (overlayOk) View.GONE else View.VISIBLE
        batteryButton.visibility = if (batteryOk) View.GONE else View.VISIBLE
        stopPromptsButton.visibility = if (monitoring) View.VISIBLE else View.GONE

        statusText.text = when {
            !overlayOk -> getString(R.string.setup_need_overlay)
            monitoring -> getString(R.string.setup_active)
            else -> getString(R.string.setup_ready)
        }
        enableButton.isEnabled = overlayOk
        enableButton.text = if (monitoring) {
            getString(R.string.restart_monitoring)
        } else {
            getString(R.string.enable_monitoring)
        }
    }
}
