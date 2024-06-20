package com.wrongcode.captionwizard.foregroundservices

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.wrongcode.captionwizard.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import java.io.Serializable
import javax.inject.Inject
import kotlin.random.Random

class ExtractAudio(private val repository: Repository, private val uri : Uri, private val context: Context) {
    fun extractAudio(scope: CoroutineScope){
         scope.launch{
             val filePath = FFmpegKitConfig.getSafParameterForRead(context,uri)
             val s = uri.toString()
             val lastSlashIndex = s.lastIndexOf('/')
             val lastDotIndex = s.lastIndexOf('.')
             var d = ""
             if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
                  d = s.substring(lastSlashIndex + 1, lastDotIndex)
             }

             val fileName = context.filesDir.absolutePath + d+".wav"
             val session = FFmpegKit.execute("-i $filePath -ac 1 -vn -acodec pcm_s16le -b:a 64k -ar 16000 $fileName")
             if (ReturnCode.isSuccess(session.returnCode)){
                Log.i("FFmpeg", "Audio merged successfully")
                 scope.launch{
                     try {
                         val model = InitModel(context).getModel()
                         Log.d("model@@@",model.toString())
                         GenerateSubtitle(repository).startGeneratingSubtitle(context,model, fileName, uri, this)
                     }
                     catch (e:Exception){
                         Log.d("model@@@", e.message.toString())
                     }
                 }

             }
             else if(ReturnCode.isCancel(session.returnCode)){
                 println("Error merging audio: ${session.returnCode}")
             }
        }

    }
}