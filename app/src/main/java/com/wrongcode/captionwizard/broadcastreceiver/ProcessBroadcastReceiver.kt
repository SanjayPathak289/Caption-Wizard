package com.wrongcode.captionwizard.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.wrongcode.captionwizard.foregroundservices.ForegroundService
import com.wrongcode.captionwizard.ui.ProcessVideoActivity
import com.wrongcode.captionwizard.ui.VideoEditorActivity
import com.wrongcode.captionwizard.viewmodels.SubtitleViewModel

class ProcessBroadcastReceiver(private val loaderLayout: LinearLayout ?= null,
                               private val addSubtitles : ImageButton ?= null,
                               private val addSubtitlesProgressBar : ProgressBar ?= null) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == ForegroundService.Actions.STOP.toString()){
            loaderLayout?.visibility = View.GONE
            addSubtitles?.visibility = View.GONE
            addSubtitlesProgressBar?.visibility = View.GONE
            val isUserOnProcess = intent.getBooleanExtra("isUserOnProcess", true)
            if(isUserOnProcess){
                Intent(context, VideoEditorActivity::class.java).also {
                    it.putExtra("videoUri", intent.getStringExtra("videoUri"))
                    it.putExtra("subtitlesMap", intent.getSerializableExtra("subtitlesMap"))
                    ProcessVideoActivity.instance?.finish()
                    context?.startActivity(it)
                }
            }
        }
    }
}