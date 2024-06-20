package com.wrongcode.captionwizard.foregroundservices

import android.os.Environment
import android.util.Log
import com.wrongcode.captionwizard.models.Subtitle
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.random.Random

class CreateSRT(private val subtitleArrList : ArrayList<Subtitle>) {
    fun createSRTFile() {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val newFolder = File(downloadsDir, "Caption Wizard")
            if(!newFolder.exists()){
                if(!newFolder.mkdirs()){
                    return
                }
            }
            val outputFile = File( newFolder, generateRandomName(6))
            try {
                if (outputFile.createNewFile()) {
                    outputFile.createNewFile()
                    val writer = BufferedWriter(FileWriter(outputFile))
                    var count = 1 // Counter for subtitle sequence number
                    for (subtitle in subtitleArrList) {
                        //Format the start and end times
                        val startTime: String = formatTime(subtitle.start)
                        val endTime: String = formatTime(subtitle.end)

                        // Write subtitle sequence number
                        writer.write(count.toString())
                        writer.newLine()
                        // Write start and end times
                        writer.write("$startTime --> $endTime")
                        writer.newLine()

                        // Write the subtitle text
                        writer.write(subtitle.word)
                        writer.newLine()
                        writer.newLine()
                        count++
                    }
                    writer.flush()
                    writer.close()
                }
                else{
                    println("Error")
                }
            }
            catch (e: Error) {
                e.message?.let { Log.d("file@@@", it) }
                }
            finally {
                println("Exported!")
            }
    }

    private fun formatTime(timeInSeconds: Double): String {
        val hours = (timeInSeconds / 3600).toInt()
        val minutes = (timeInSeconds % 3600 / 60).toInt()
        val seconds = (timeInSeconds % 60).toInt()
        val milliseconds = (timeInSeconds * 1000 % 1000).toInt()
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds)
    }

    private fun generateRandomName(length: Int): String {
        val charPool: List<Char> = ('0'..'9') + ('A'..'Z') + ('a'..'z')
        var s = (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
        s = "output$s.srt"
        return s
    }
}