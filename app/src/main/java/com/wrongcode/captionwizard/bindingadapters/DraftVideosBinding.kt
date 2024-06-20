package com.wrongcode.captionwizard.bindingadapters

import android.media.Image
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wrongcode.captionwizard.adapters.DraftVideosAdapter
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity

class DraftVideosBinding {
    companion object{
        @BindingAdapter("viewVisibility","setData", requireAll = true)
        @JvmStatic
        fun setDataAndVisibility(view : RecyclerView, draftVideos : List<SubtitleEntity>?, adapter: DraftVideosAdapter?) {

            if (draftVideos != null) {
                if (draftVideos.isNotEmpty()) {
                    view.visibility = View.VISIBLE
                    adapter?.setData(draftVideos)
                    adapter?.notifyDataSetChanged()
                }
            }
        }

        @BindingAdapter("videoId")
        @JvmStatic
        fun setImage(view: View, uriString: String){
            val uri = Uri.parse(uriString)
            try {
                val mediaRetriever = MediaMetadataRetriever()
                mediaRetriever.setDataSource(view.context, uri)
                val time = 1000000L
                val bitmap = mediaRetriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST)
                val duration = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMillis = duration?.toLongOrNull() ?: 0L
                val durationSeconds = durationMillis / 1000
                val minutes = durationSeconds/60
                val seconds = durationSeconds%60
                mediaRetriever.release()
                mediaRetriever.close()
                when(view){
                    is ImageView -> {
                        view.setImageBitmap(bitmap)
                    }
                    is TextView -> {
                        view.text = String.format("%02d:%02d", minutes, seconds)
                    }
                }
            }
            catch (e : Exception){
                println(e.message)
            }

        }
    }
}