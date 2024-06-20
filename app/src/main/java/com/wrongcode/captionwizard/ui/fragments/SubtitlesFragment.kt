package com.wrongcode.captionwizard.ui.fragments

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.adapters.SubtitlesFileAdapter
import com.wrongcode.captionwizard.data.SRTFile
import java.io.File

class SubtitlesFragment : Fragment() {
    private lateinit var showSubtitlesRecyclerView : RecyclerView
    private lateinit var refreshSRT : ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_subtitles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshSRT = view.findViewById(R.id.refreshSRT)
        showSubtitlesRecyclerView = view.findViewById(R.id.showSubtitlesRecyclerView)
        showSubtitlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapater = SubtitlesFileAdapter(clickListener)
        showSubtitlesRecyclerView.adapter = adapater
        var srtFilesArray: ArrayList<SRTFile> = getSRTFiles()
        adapater.setData(srtFilesArray)

        refreshSRT.setOnClickListener {
            val animator = ObjectAnimator.ofFloat(it,"rotation", 0f, 360f)
            animator.duration = 500
            animator.start()
            srtFilesArray = getSRTFiles()
            adapater.setData(srtFilesArray)
            adapater.notifyDataSetChanged()
        }
    }

    private val clickListener: (String) -> Unit = { path ->
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(requireContext(), "${context?.packageName}.provider", File(path))
        intent.setDataAndType(uri, "text/plain")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if(context?.packageManager?.let { intent.resolveActivity(it) } != null){
            context?.startActivity(Intent.createChooser(intent, "Open with"))
        }
        else{
            Toast.makeText(requireContext(),"No suitable apps to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSRTFiles() : ArrayList<SRTFile>{
        val srtFiles = ArrayList<SRTFile>()
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val myFolder = File(downloadsDir, "Caption Wizard")
        if(myFolder.exists() && myFolder.canRead()){
            myFolder.walkTopDown().filter { it.isFile && it.extension == "srt" }.forEach {
                srtFiles.add(SRTFile(it.name, it.absolutePath))
            }
        }
        return srtFiles
    }

}