package com.wrongcode.captionwizard.adapters

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.models.Subtitle
import com.wrongcode.captionwizard.models.SubtitleList
import kotlin.math.sin

class SubtitlesAdapter(private val context: Context, private val center : Int)  : RecyclerView.Adapter<SubtitlesAdapter.SubtitleViewHolder>() {
//    private var subtitlesArray = ArrayList<String>()
//    private var timeStampsArray = ArrayList<Pair<Double,Double>>()
    private var subtitleList = ArrayList<Subtitle>()
    var onItemClick: ((Pair<Int, String>) -> Unit)? = null
    private var factor = 0.00

    class SubtitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val subtitlesTextView = itemView.findViewById<TextView>(R.id.subtitlesTextView)
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
                outRect.right = 2*offset
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_subtitle_layout, parent, false)
        return  SubtitleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return subtitleList.size
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        val singleSub = subtitleList[position]
        val word = singleSub.word
        holder.subtitlesTextView.text = word
        val startTime = singleSub.start
        val endTime = singleSub.end

        val textViewWidth = (endTime - startTime)*1000*factor
        holder.subtitlesTextView.width = (textViewWidth * context.resources.displayMetrics.density).toInt()

        if (position+1 < subtitleList.size){
            val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            var marginInDP = 0.00
            marginInDP = (subtitleList[position+1].start - singleSub.end)*1000*factor
            val marginInPx = marginInDP*context.resources.displayMetrics.density
            layoutParams.rightMargin = marginInPx.toInt()
            holder.itemView.layoutParams = layoutParams
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(Pair(position,word))
        }
    }
    fun setData(newSubtitleList : ArrayList<Subtitle>){
        subtitleList = newSubtitleList
    }
    fun setFactor(f : Double){
        factor = f
    }
}