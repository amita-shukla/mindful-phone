package com.example.mindful

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.mindful.ui.theme.MindfulTheme
import android.content.Context
import android.net.Uri
import android.os.PowerManager
import android.widget.Toast
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build



// MainActivity runs in the background without showing UI
class MainActivity : AppCompatActivity() {

    private val requestOverlayPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay Permission granted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Overlay Permission not granted!! ", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var screenUnlockReceiver: ScreenUnlockReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.RECEIVE_BOOT_COMPLETED),1);
//        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),1);
        registerScreenUnlockReceiver()

        // battery
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if(pm.isIgnoringBatteryOptimizations(packageName)){
            println("AMITA .... BATTERY***********************")
        }

        requestOverlayPermission()
        finish() // close the activity immediately


//        setContentView(R.layout.activity_main)
//
//        val openPopupButton = findViewById<Button>(R.id.openPopupButton)
//        openPopupButton.setOnClickListener {
//            val intent = Intent(this, PopupActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            requestOverlayPermission.launch(intent)
        }
    }

    private fun registerScreenUnlockReceiver() {
        screenUnlockReceiver = ScreenUnlockReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenUnlockReceiver, filter)
    }

}

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Cyan) {
        Text(
            text = "Hello, my name is $name!",
            modifier = modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MindfulTheme {
        Greeting("Amita")
    }
}*/
