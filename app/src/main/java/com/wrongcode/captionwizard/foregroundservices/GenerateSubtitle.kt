package com.wrongcode.captionwizard.foregroundservices

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wrongcode.captionwizard.data.Repository
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity
import com.wrongcode.captionwizard.models.Subtitle
import com.wrongcode.captionwizard.models.SubtitleList
import com.wrongcode.captionwizard.viewmodels.SubtitleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import javax.inject.Inject

class GenerateSubtitle (private val repository: Repository){

//    private var m = HashMap<Pair<Double,Double>, String>()
    private val generatedSubtitleArr = ArrayList<Subtitle>()
    fun startGeneratingSubtitle(context: Context, model : Model, filePath : String, fileUri : Uri, scope: CoroutineScope){
        scope.launch {
            try {
                val recognizer = Recognizer(model, 16000f)
                recognizer.setWords(true)
                Log.d("result@@@", "Result...")
                val file = File(filePath);
                val ais = FileInputStream(file)
                var nbytes: Int
                val b = ByteArray(1024)
                val outputStream = ByteArrayOutputStream()
                while (ais.read(b).also { nbytes = it } != -1) {
                    outputStream.write(b, 0, nbytes)
                }
//                while (nbytes != -1) {
//                    nbytes = ais.read(b)
//                    if(nbytes != -1){
//                        outputStream.write(b, 0, nbytes)
//                    }
//                }
                ais.close()
                val waveByteArray = outputStream.toByteArray()
                recognizer.acceptWaveForm(waveByteArray, waveByteArray.size)
                val modelResult = recognizer.result
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
                Log.d("result@@@", modelResult)

                createSubtitle(modelResult)
//                val subtitleList = SubtitleList(fileUri.toString(), generatedSubtitleArr)
                val subtitleEntity = SubtitleEntity(fileUri.toString(), generatedSubtitleArr)
                repository.local.insertSubtitle(subtitleEntity)

            } catch (e: IOException) {
                Log.d("error@@@", e.message.toString())
            } finally {
                Log.d("final@@@", "finally...")
                Intent(context, ForegroundService::class.java).also {
                    it.action = ForegroundService.Actions.STOP.toString()
                    it.putExtra("videoUri", fileUri.toString())
                    it.putExtra("isCancelledByUser", false)
                    context.startService(it)
                }
            }
        }

    }

    private fun createSubtitle(result : String){
//        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JSONObject(result)
            val parsedArray = jsonObject.getJSONArray("result")
            var i = 0
//            var temp = StringBuilder()
//            var wordCount = 0
//            var startTime: Double? = null
            while (i < parsedArray.length()) {
                val wordObject = parsedArray.getJSONObject(i)
                val word = wordObject.getString("word")
                val startTime = wordObject.getDouble("start")
                val endTime = wordObject.getDouble("end")

                val subtitle = Subtitle(startTime,endTime, word)
                generatedSubtitleArr.add(subtitle)

                val p = Pair(startTime, endTime)
//                m[p] = word
                i++
//                if (startTime == null) {
//                    startTime = wordObject.getDouble("start")
//                }
//                temp.append(word).append(" ")
//                wordCount++
//                if (wordCount == 5 || i == parsedArray.length() - 1) {
//                    val endTime = wordObject.getDouble("end")
//                    val p = Pair(startTime, endTime)
//                    m[p] = temp.toString()
//                    temp = StringBuilder()
//                    wordCount = 0
//                    startTime = null
//                    if (i == parsedArray.length() - 1) {
//                        i++
//                    }
//                }
//                i++
            }
//        }
    }




}