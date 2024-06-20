package com.example.mindful

import android.app.AlertDialog
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PopupActivity : AppCompatActivity() {
//    private var overlayView: View? = null
//    private lateinit var windowManager: WindowManager

    override fun onCreate(savedInstanceState: Bundle?) {
        println("*************AMITA***********popup launching")
        super.onCreate(savedInstanceState)
        showPopupDialog(this)

        // Initialize WindowManager
//        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Inflate the overlay layout
//        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)

        // Define layout parameters
//        val layoutParams = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                WindowManager.LayoutParams.TYPE_PHONE
//            },
//            PixelFormat.TRANSLUCENT
//        )
//        layoutParams.gravity = Gravity.CENTER
//
//         Add the overlay view to WindowManager
//        windowManager.addView(overlayView, layoutParams)

        // Initialize the views
//        val inputField1 = findViewById<EditText>(R.id.inputField1)
//        val inputField2 = findViewById<EditText>(R.id.inputField2)
//        val inputField3 = findViewById<EditText>(R.id.inputField3)
//        val okButton = overlayView?.findViewById<Button>(R.id.okButton)

//        inputField1?.apply {
//            post {
//                requestFocus()
//                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
//            }
//        }
        // Set up close button in overlay
//        okButton?.setOnClickListener {
//            val input1 = inputField1?.text?.toString()
//            val input2 = inputField2?.text?.toString()
//            val input3 = inputField3?.text?.toString()
//            Toast.makeText(this, "Input 1: $input1, Input 2: $input2, Input 3: $input3", Toast.LENGTH_LONG).show()
//            finish()
//        }

        /*
        // Remove the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Set the layout
        setContentView(R.layout.activity_popup)

        // Initialize the views
        val inputField1 = findViewById<EditText>(R.id.inputField1)
        val inputField2 = findViewById<EditText>(R.id.inputField2)
        val okButton = findViewById<Button>(R.id.okButton)

        // Set onClickListener for the OK button
        okButton.setOnClickListener {
            val input1 = inputField1.text.toString()
            val input2 = inputField2.text.toString()
            Toast.makeText(this, "Input 1: $input1, Input 2: $input2", Toast.LENGTH_LONG).show()
            finish() // Close the activity
        }*/

    }

    private fun showPopupDialog(context: Context) {
        Log.d("PopupDialog", "*********AMITA Dialog**********")
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_popup, null)
        val inputField1 = dialogView?.findViewById<android.widget.EditText>(R.id.inputField1)
        val inputField2 = dialogView?.findViewById<android.widget.EditText>(R.id.inputField2)
        val inputField3 = dialogView?.findViewById<android.widget.EditText>(R.id.inputField3)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Wait Wait!!")
            .setView(R.layout.dialog_popup) // Set custom layout for the dialog
            .setPositiveButton("Go Ahead!!") { dialog, _ ->
//                val dialogView = (dialog as AlertDialog).findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.dialogLayout)


                // Retrieve text from input fields
                val text1 = inputField1?.text?.toString()
                val text2 = inputField2?.text?.toString()
                val text3 = inputField3?.text?.toString()

                // Handle the input
                Toast.makeText(context, "Input1: $text1, Input2: $text2, Input3: $text3", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }


//    override fun onDestroy() {
//        super.onDestroy()
//        // Remove overlay view when activity is destroyed
//        if (overlayView != null) {
//            windowManager.removeView(overlayView)
//            overlayView = null
//        }
//    }

//    override fun onResume() {
//        super.onResume()
//        val filter = IntentFilter(ScreenUnlockReceiver.SCREEN_UNLOCKED_ACTION)
//        registerReceiver(screenUnlockReceiver, filter)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        unregisterReceiver(screenUnlockReceiver)
//    }
}