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

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { updateStatus() }

    private val requestUsageAccess = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        updateStatus()
        AppSuggestionsProvider.refreshIfStale(this)
    }

    private lateinit var statusText: TextView
    private lateinit var enableButton: Button
    private lateinit var batteryButton: Button
    private lateinit var testPromptButton: Button
    private lateinit var stopPromptsButton: Button
    private lateinit var usageAccessButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        enableButton = findViewById(R.id.enableButton)
        batteryButton = findViewById(R.id.batteryButton)
        testPromptButton = findViewById(R.id.testPromptButton)
        stopPromptsButton = findViewById(R.id.stopPromptsButton)
        usageAccessButton = findViewById(R.id.usageAccessButton)

        batteryButton.setOnClickListener { requestBatteryExemption() }
        usageAccessButton.setOnClickListener { requestUsageAccess() }
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
        AppSuggestionsProvider.refreshIfStale(this)
    }

    private fun requestUsageAccess() {
        if (UsageAccess.hasUsageAccess(this)) return
        requestUsageAccess.launch(UsageAccess.settingsIntent())
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
        val batteryOk = (getSystemService(POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(packageName)
        val monitoring = MindfulPrefs.isMonitoringEnabled(this)
        val usageOk = UsageAccess.hasUsageAccess(this)

        batteryButton.visibility = if (batteryOk) View.GONE else View.VISIBLE
        usageAccessButton.visibility = if (usageOk) View.GONE else View.VISIBLE
        stopPromptsButton.visibility = if (monitoring) View.VISIBLE else View.GONE

        statusText.text = when {
            monitoring -> getString(R.string.setup_active)
            else -> getString(R.string.setup_ready)
        }
        enableButton.text = if (monitoring) {
            getString(R.string.restart_monitoring)
        } else {
            getString(R.string.enable_monitoring)
        }
    }
}
