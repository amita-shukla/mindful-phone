package com.example.mindful


import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast

class ScreenUnlockReceiver : BroadcastReceiver() {

//    companion object {
//        const val SCREEN_UNLOCKED_ACTION = "com.example.mindful.SCREEN_UNLOCKED"
//    }

    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.ACTION_USER_UNLOCKED || intent.action == Intent.ACTION_USER_PRESENT) {
//            val broadcastIntent = Intent(SCREEN_UNLOCKED_ACTION)
//            context.sendBroadcast(broadcastIntent)

//             Screen is turned on and device is unlocked
//             Start your PopupActivity here
//            val popupIntent = Intent(context, PopupActivity::class.java)
//            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(popupIntent)
//        }
        Toast.makeText(context,"AMITA***********************", Toast.LENGTH_LONG).show()

        Log.d("ScreenUnlockReceiver","***********AMITA************* SCREEN" + intent.action)
       // showPopupDialog(context)
        // Enqueue work to start PopupActivity
        val workRequest = OneTimeWorkRequestBuilder<PopupWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun showPopupDialog(context: Context?) {
        Log.d("PopupDialog", "*********AMITA Dialog**********")
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_popup, null)
        val inputField1 = dialogView?.findViewById<android.widget.EditText>(R.id.inputField1)
        val inputField2 = dialogView?.findViewById<android.widget.EditText>(R.id.inputField2)
        val inputField3 = dialogView?.findViewById<android.widget.EditText>(R.id.inputField3)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Popup")
            .setMessage("Enter your details")
            .setView(R.layout.dialog_popup) // Set custom layout for the dialog
            .setPositiveButton("OK") { dialog, _ ->
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

    /*fun register(context: Context) {
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
*/


}