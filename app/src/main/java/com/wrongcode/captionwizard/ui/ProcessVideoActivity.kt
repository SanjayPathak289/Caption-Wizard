package com.wrongcode.captionwizard.ui

import TimerManager
import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.google.android.material.button.MaterialButton
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.broadcastreceiver.NotificationCancelReceiver
import com.wrongcode.captionwizard.broadcastreceiver.ProcessBroadcastReceiver
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity
import com.wrongcode.captionwizard.foregroundservices.ForegroundService
import com.wrongcode.captionwizard.viewmodels.SubtitleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProcessVideoActivity : AppCompatActivity() {
    private lateinit var userFileUri : Uri
    private lateinit var userVideoView : VideoView
    private lateinit var startAutoCaptions : MaterialButton
    private lateinit var processVideoProgressBar : ProgressBar
    private lateinit var editVideo : MaterialButton
    private lateinit var backgroundLoaderView : View
    private lateinit var loaderTimerCount : TextView
    private lateinit var cancelBtn : Button
    private lateinit var loaderLayout : LinearLayout
    private val timerManager = TimerManager()
    private lateinit var minimizeBtn : Button
    private val subtitleViewModel by viewModels<SubtitleViewModel>()
    private lateinit var backBtn : ImageButton
    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: ProcessVideoActivity? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_video)
        instance = this

        val uriString = intent.getStringExtra("uri")
        userFileUri = Uri.parse(uriString)
        userVideoView = findViewById(R.id.userVideoView)
        userVideoView.setVideoURI(userFileUri)
        val mediaController = MediaController(this@ProcessVideoActivity)
        userVideoView.setMediaController(mediaController)
        processVideoProgressBar = findViewById(R.id.processVideoProgressBar)
        startAutoCaptions = findViewById(R.id.startAutoCaptions)
        editVideo = findViewById(R.id.editVideo)
        backgroundLoaderView = findViewById(R.id.background_loader_view)
        loaderTimerCount = findViewById(R.id.loaderTimerCount)
        cancelBtn = findViewById(R.id.cancelBtn)
        loaderLayout = findViewById(R.id.loaderLayout)
        minimizeBtn = findViewById(R.id.minimizeBtn)
        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener {
            this.onBackPressed()
        }

        startAutoCaptions.setOnClickListener {
//            subtitleViewModel.updateIsUserOnProcess(true)
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                    , 1)

                }
            }
//            else{
                val isCaptionServiceRunning = isServiceRunning(this@ProcessVideoActivity, ForegroundService::class.java)
                if(!isCaptionServiceRunning){
                    loaderLayout.visibility = View.VISIBLE
                    backgroundLoaderView.visibility = View.VISIBLE
                    val duration = getMediaLength(userFileUri)
                    startTimer(duration * 1.5.toLong())
                    CoroutineScope(Dispatchers.IO).launch {
                        Intent(this@ProcessVideoActivity, ForegroundService::class.java).also {
                            it.action = ForegroundService.Actions.START.toString()
                            it.putExtra("uriToService", uriString)
                            startService(it)
                        }
                    }
                }
//            }
        }
        editVideo.setOnClickListener {
            val isCaptionServiceRunning = isServiceRunning(this@ProcessVideoActivity, ForegroundService::class.java)
            if(!isCaptionServiceRunning){
                val subtitleEntity = uriString?.let { it1 -> SubtitleEntity(it1, ArrayList()) }
                if (subtitleEntity != null) {
                    subtitleViewModel.insertSubtitleList(subtitleEntity)
                }
                Intent(this, VideoEditorActivity::class.java).also {
                    it.putExtra("videoUri", uriString)
                    this.startActivity(it)
                }
            }
        }

        cancelBtn.setOnClickListener {
            Intent(this@ProcessVideoActivity, ForegroundService::class.java).also {
                it.action = ForegroundService.Actions.STOP.toString()
                it.putExtra("isCancelledByUser", true)
                startService(it)
            }
            processVideoProgressBar.progress = 0
            loaderTimerCount.text = "0%"
            deleteAudio()
            timerManager.cancelTimer()
        }

        minimizeBtn.setOnClickListener {
//            subtitleViewModel.updateIsUserOnProcess(false)
            sendVariableToForegroundService(false)
            finish()
        }



        val filter = IntentFilter(ForegroundService.Actions.STOP.toString())
        registerReceiver(ProcessBroadcastReceiver(loaderLayout = loaderLayout),filter)

        val uiFilter = IntentFilter("stopByUser")
        registerReceiver(NotificationCancelReceiver(processVideoProgressBar = processVideoProgressBar,
            loaderLayout = loaderLayout,
            backgroundLoaderView = backgroundLoaderView,
            loaderTimerCount = loaderTimerCount,
            timer = timerManager,
            uri = userFileUri), uiFilter, RECEIVER_NOT_EXPORTED)
    }

    private fun sendVariableToForegroundService(value: Boolean) {
        Intent(this, ForegroundService::class.java).also {
            it.action = ForegroundService.Actions.UPDATE.toString()
            it.putExtra("isUserOnProcess", value)
            startService(it)
        }
    }

    private fun deleteAudio() {
        val s = userFileUri.toString()
        val lastSlashIndex = s.lastIndexOf('/')
        val lastDotIndex = s.lastIndexOf('.')
        var d = ""
        if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
            d = s.substring(lastSlashIndex + 1, lastDotIndex)
        }

        val fileName = filesDir.absolutePath + d+".wav"
        val file = File(fileName)
        println(fileName)
        if(file.exists()){
            val deleted = file.delete()
            if(deleted){
                println("Audio deleted")
            }
            else{
                println("Audio Not deleted")
            }
        }
        else{
            println("Audio Doesnot exists")
        }
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }


    private fun startTimer(duration : Long){
        val manager = getSystemService(NotificationManager::class.java)
        timerManager.startTimer(duration, onTick = { progress ->
            processVideoProgressBar.progress = progress
            processVideoProgressBar.max = 100
            loaderTimerCount.text = "${progress}%"
            val notification = createNotification(progress)
            manager?.notify(1, notification)
        }, onFinish = {
            processVideoProgressBar.progress = 100
            loaderTimerCount.text = "100%"
            timerManager.cancelTimer()
        })
    }
    private fun createNotification(progress: Int): Notification {
        val cancelIntent = Intent(this, ForegroundService::class.java).apply {
            action = ForegroundService.Actions.STOP.toString()
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

    private fun getMediaLength(uri : Uri) : Long{
        val mediaRetriever = MediaMetadataRetriever()
        mediaRetriever.setDataSource(this@ProcessVideoActivity ,uri)
        val time = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return time!!.toLong()
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d("permission@@@", "onRequestPermissionsResult Block")
        }
    }

}
