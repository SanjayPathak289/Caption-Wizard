package com.wrongcode.captionwizard.adapters

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.wrongcode.captionwizard.R
import kotlin.properties.Delegates

class FramesAdapter : RecyclerView.Adapter<FramesAdapter.FramesViewHolder>() {
    private var framesArray = ArrayList<Bitmap>()

    class FramesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frameImageView = itemView.findViewById<ImageView>(R.id.frameImageView)
    }

    class MarginItemDecoration(private val offset : Int) : RecyclerView.ItemDecoration(){
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val lastItemPosition = parent.adapter?.itemCount?.minus(1) ?: 0
            if(position == 0){
                outRect.left = offset
            }
            if(position == lastItemPosition){
                outRect.right = offset
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FramesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_frame_layout, parent, false)
        return FramesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return framesArray.size
    }

    override fun onBindViewHolder(holder: FramesViewHolder, position: Int) {
        holder.frameImageView.setImageBitmap(framesArray[position])
    }

    fun setData(newframeArray : ArrayList<Bitmap>){
        this.framesArray = newframeArray
    }

}