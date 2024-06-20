package com.wrongcode.captionwizard.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity
import com.wrongcode.captionwizard.databinding.DraftVideoBinding
import com.wrongcode.captionwizard.ui.VideoEditorActivity
import com.wrongcode.captionwizard.viewmodels.SubtitleViewModel

class DraftVideosAdapter(private val context: Context, private val subtitleViewModel: SubtitleViewModel): RecyclerView.Adapter<DraftVideosAdapter.DraftVideoViewHolder>() {

    private var draftVideosList = emptyList<SubtitleEntity>()
    class DraftVideoViewHolder(val binding : DraftVideoBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(subtitleEntity: SubtitleEntity){
            binding.subtitleEntity = subtitleEntity
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftVideoViewHolder {
        val view = DraftVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DraftVideoViewHolder(view)

    }

    override fun getItemCount(): Int {
        return draftVideosList.size
    }

    override fun onBindViewHolder(holder: DraftVideoViewHolder, position: Int) {

        holder.bind(draftVideosList[position])
        holder.binding.draftVideoCardView.setOnClickListener{
            Intent(context, VideoEditorActivity::class.java).also {
                it.putExtra("videoUri", draftVideosList[position].videoId)
                context.startActivity(it)
            }
        }
        holder.binding.moreBtn.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.binding.moreBtn)
            popupMenu.inflate(R.menu.more_menu)
            popupMenu.setOnMenuItemClickListener{ item ->
                when(item.itemId){
                    R.id.editDraftVideo -> {
                        Intent(context, VideoEditorActivity::class.java).also {
                            it.putExtra("videoUri", draftVideosList[position].videoId)
                            context.startActivity(it)
                        }
                        true
                    }
                    R.id.deleteDraftVideo-> {
                        subtitleViewModel.deleteSubtitleVideo(draftVideosList[position])
                        true
                    }

                    else -> {true}
                }
            }
            popupMenu.show()
        }

    }
    fun setData(newDraftVideoList : List<SubtitleEntity>){
        draftVideosList = newDraftVideoList
    }
}