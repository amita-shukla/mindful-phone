package com.example.mindful

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PopupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        setContentView(R.layout.unlock_prompt)
        UnlockPrompt.bindPrompt(findViewById(R.id.promptRoot)) {
            finish()
        }
    }

    override fun onDestroy() {
        UnlockPrompt.dismiss()
        super.onDestroy()
    }
}
