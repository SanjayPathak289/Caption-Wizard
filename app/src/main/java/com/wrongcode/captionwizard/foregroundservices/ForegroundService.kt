package com.wrongcode.captionwizard.foregroundservices

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.data.Repository
import com.wrongcode.captionwizard.ui.ProcessVideoActivity
import com.wrongcode.captionwizard.ui.VideoEditorActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class ForegroundService: Service() {
    @Inject lateinit var repository: Repository

    private lateinit var userFileUri: Uri
    private var isUserOnProcess = true
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString() -> start(intent)
            Actions.UPDATE.toString() -> {
                isUserOnProcess = intent.getBooleanExtra("isUserOnProcess", true)
            }
            Actions.STOP.toString() -> {
                val isCancelledByUser = intent.getBooleanExtra("isCancelledByUser", false)
                Log.d("isCancelledByUser", isCancelledByUser.toString())
                stopForeground(true)
                stopSelf()
                if(isCancelledByUser){
                    val cancelIntent = Intent("stopByUser")
                    sendBroadcast(cancelIntent)
                }
                else{
                    val finishedNotification = createFinishedNotification()
                    val manager = getSystemService(NotificationManager::class.java)
                    manager?.notify(1, finishedNotification)
                    val stopIntent = Intent(Actions.STOP.toString())
                    val videoURIFromGenerateSubtitle = intent.getStringExtra("videoUri")
                    stopIntent.putExtra("videoUri", videoURIFromGenerateSubtitle)
                    stopIntent.putExtra("subtitlesMap", intent.getSerializableExtra("subtitlesMap"))
                    stopIntent.putExtra("isUserOnProcess", isUserOnProcess)
                    sendBroadcast(stopIntent)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(intent: Intent?) {
        val context = this@ForegroundService
        serviceScope.launch {
            userFileUri = Uri.parse(intent?.getStringExtra("uriToService"))
            startForeground(1,createNotification(0))
            ExtractAudio(repository, userFileUri, context).extractAudio(this)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun createNotification(progress: Int): Notification {
        val cancelIntent = Intent(this, ForegroundService::class.java).apply {
            action = Actions.STOP.toString()
            putExtra("isCancelledByUser", true)
        }
        val cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Generating Captions")
            .setContentText(progress.toString())
            .setOngoing(true)
            .addAction(R.drawable.ic_cancel,"Cancel", cancelPendingIntent)
            .setProgress(100, progress, false)
            .build()
    }
    private fun createFinishedNotification() : Notification{
        val intent = Intent(this, VideoEditorActivity::class.java).apply {
            putExtra("videoUri", userFileUri.toString())
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("Generating Captions")
            .setContentText("Completed")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    enum class Actions{
        START, STOP, UPDATE
    }


}