package com.wrongcode.captionwizard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.data.SRTFile

class SubtitlesFileAdapter(private val onItemClickListener: (String) -> Unit) : RecyclerView.Adapter<SubtitlesFileAdapter.SubtitlesFileViewHolder>() {
    private var subtitleFilesList = ArrayList<SRTFile>()
    inner class SubtitlesFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val srtFileName = itemView.findViewById<TextView>(R.id.srtFileName)
        init {
            itemView.setOnClickListener {
                onItemClickListener(subtitleFilesList[adapterPosition].path)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitlesFileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_subtitle_file_layout, parent, false)
        return SubtitlesFileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return subtitleFilesList.size
    }

    override fun onBindViewHolder(holder: SubtitlesFileViewHolder, position: Int) {
        holder.srtFileName.text = subtitleFilesList[position].name
    }
    fun setData(newSubtitlesFileList : ArrayList<SRTFile>){
        subtitleFilesList = newSubtitlesFileList
    }
}