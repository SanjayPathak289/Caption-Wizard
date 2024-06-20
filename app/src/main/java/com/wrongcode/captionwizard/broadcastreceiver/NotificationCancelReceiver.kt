package com.wrongcode.captionwizard.broadcastreceiver

import TimerManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File

class NotificationCancelReceiver(private val processVideoProgressBar : ProgressBar ?= null,
                                 private val loaderLayout : LinearLayout ?= null,
                                 private val backgroundLoaderView : View ?= null,
                                 private val loaderTimerCount : TextView ?= null,
                                 private var timer : TimerManager,
                                 private val uri : Uri,
                                 private val addSubtitle : ImageButton ?= null,
                                 private val addSubtitlesProgressBar : ProgressBar ?= null,
    ) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == "stopByUser"){
            processVideoProgressBar?.progress = 0
            loaderLayout?.visibility = View.INVISIBLE
            loaderTimerCount?.text = "0%"
            backgroundLoaderView?.visibility = View.INVISIBLE
            addSubtitle?.visibility = View.VISIBLE
            addSubtitlesProgressBar?.visibility = View.GONE
            timer.cancelTimer()
            deleteAudio(context)
        }
    }
    private fun deleteAudio(context: Context?) {
        val s = uri.toString()
        val lastSlashIndex = s.lastIndexOf('/')
        val lastDotIndex = s.lastIndexOf('.')
        var d = ""
        if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
            // Extract the substring between '/' and '.'
            d = s.substring(lastSlashIndex + 1, lastDotIndex)
        }

        val fileName = context?.filesDir?.absolutePath + d+".wav"
        val file = File(fileName)
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
}