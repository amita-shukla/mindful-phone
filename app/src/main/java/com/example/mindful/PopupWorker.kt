package com.example.mindful

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters

class PopupWorker (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        println("********AMITA launching worker************")
        // Start PopupActivity
        val popupIntent = Intent(applicationContext, PopupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        applicationContext.startActivity(popupIntent)
//        showPopupDialog(this.applicationContext)
        return Result.success()
    }

}