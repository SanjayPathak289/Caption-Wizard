package com.wrongcode.captionwizard.ui

import TimerManager
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.adapters.FramesAdapter
import com.wrongcode.captionwizard.adapters.SubtitlesAdapter
import com.wrongcode.captionwizard.broadcastreceiver.NotificationCancelReceiver
import com.wrongcode.captionwizard.broadcastreceiver.ProcessBroadcastReceiver
import com.wrongcode.captionwizard.data.Repository
import com.wrongcode.captionwizard.extensions.slideDown
import com.wrongcode.captionwizard.extensions.slideUp
import com.wrongcode.captionwizard.foregroundservices.CreateSRT
import com.wrongcode.captionwizard.foregroundservices.ForegroundService
import com.wrongcode.captionwizard.models.Subtitle
import com.wrongcode.captionwizard.viewmodels.SubtitleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil


@AndroidEntryPoint
class VideoEditorActivity : AppCompatActivity() {
    @Inject
    lateinit var repository: Repository
    private val subtitleViewModel by viewModels<SubtitleViewModel>()
    private var videoDuration = 0L
    private var totalWidth = 0
    private var factor = 0.00
    private var inverseFactor = 0.00
    private lateinit var handler : Handler
    private lateinit var editorVideoView : VideoView
    private lateinit var framesRecyclerView : RecyclerView
    private lateinit var subtitlesRecyclerView : RecyclerView
    private lateinit var editingOptionsLayout : LinearLayout
    private lateinit var editButton : LinearLayout
    private lateinit var relativeLayout : RelativeLayout
    private val framesAdapter : FramesAdapter by lazy { FramesAdapter() }
    private val subtitlesAdapter : SubtitlesAdapter by lazy { SubtitlesAdapter(this, resources.displayMetrics.widthPixels/2) }
    private lateinit var playPauseBtn : ImageButton
    private var dist = 0
    private var isVideoPlaying = false
    private var wasScrolled = false
    private var whereSeekTo = 0
    private var subtitleArr = ArrayList<Subtitle>()
    private lateinit var videoEditorLoader : ProgressBar
    private lateinit var videoEditorLayout : LinearLayout
    private lateinit var backBtn : ImageButton
    private lateinit var currentVideoTime : TextView
    private lateinit var totalVideoTime : TextView
    private lateinit var exportBtn : ImageButton
    private lateinit var uriString : String
    private lateinit var addSubtitles : ImageButton
    private lateinit var addSubtitlesProgressBar : ProgressBar
    private val timerManager = TimerManager()
    private lateinit var subtitleTV : TextView
    var prevIndex : Int = 0


    private val videoTimeHandler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L
    private val updateTimeTask = object : Runnable{
        override fun run() {
            currentVideoTime.text = getTimeFromMillis(editorVideoView.currentPosition.toLong())
            videoTimeHandler.postDelayed(this, updateInterval)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_editor)
        uriString = intent.getStringExtra("videoUri").toString()
        val videoUri = Uri.parse(uriString)
        videoEditorLoader = findViewById(R.id.videoEditorLoader)
        videoEditorLayout = findViewById(R.id.videoEditorLayout)
        playPauseBtn = findViewById(R.id.playPauseButton)
        currentVideoTime = findViewById(R.id.currentVideoTime)
        totalVideoTime = findViewById(R.id.totalVideoTime)
        exportBtn = findViewById(R.id.exportBtn)
        editorVideoView = findViewById(R.id.editorVideoView)
        editorVideoView.setVideoURI(videoUri)
        backBtn = findViewById(R.id.backBtn)
        addSubtitles = findViewById(R.id.addSubtitles)
        addSubtitlesProgressBar = findViewById(R.id.addSubtitlesProgressBar)
        subtitleTV = findViewById(R.id.subtitleTV)
        backBtn.setOnClickListener {
            this.onBackPressed()
        }
        framesRecyclerView = findViewById(R.id.framesRecyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val screenWidth = resources.displayMetrics.widthPixels
        val offset = screenWidth/2
        val itemD = FramesAdapter.MarginItemDecoration(offset)
        framesRecyclerView.addItemDecoration(itemD)

        framesRecyclerView.layoutManager = layoutManager

        framesRecyclerView.adapter = framesAdapter
//        CoroutineScope(Dispatchers.IO).launch {
        extractFramesFromVideo(videoUri)
//        }


        exportBtn.setOnClickListener { view ->
            showExportMenu(view)
        }


        subtitlesRecyclerView = findViewById(R.id.subtitlesRecyclerView)
        val subtitleLayoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
        subtitlesRecyclerView.layoutManager = subtitleLayoutManager
        subtitlesRecyclerView.adapter = subtitlesAdapter

        val itemD2 = SubtitlesAdapter.MarginItemDecoration(offset)
        subtitlesRecyclerView.addItemDecoration(itemD2)

        uriString.let { subtitleViewModel.readSubtitleByVideoId(it) }
        subtitleViewModel.subtitleOfVideoId.observe(this) {
            if(it[0].subtitleList.isNotEmpty()) {
                val l = it[0].subtitleList
                subtitlesAdapter.setData(l)
                subtitleArr = l
                exportBtn.visibility = View.VISIBLE
                subtitlesAdapter.notifyDataSetChanged()
            }
            else{
                addSubtitles.visibility = View.VISIBLE
            }

        }

        val filter = IntentFilter(ForegroundService.Actions.STOP.toString())
        registerReceiver(ProcessBroadcastReceiver(addSubtitles = addSubtitles, addSubtitlesProgressBar = addSubtitlesProgressBar),filter)

        val uiFilter = IntentFilter("stopByUser")
        registerReceiver(
            NotificationCancelReceiver(timer = timerManager,
            uri = videoUri,
                addSubtitle = addSubtitles,
                addSubtitlesProgressBar = addSubtitlesProgressBar), uiFilter, RECEIVER_NOT_EXPORTED)
        addSubtitles.setOnClickListener {
            addSubtitles.visibility = View.GONE
            val isCaptionServiceRunning = isServiceRunning(this@VideoEditorActivity, ForegroundService::class.java)
            if(!isCaptionServiceRunning){
                val duration = getMediaLength(videoUri)
                startTimer(duration)
                CoroutineScope(Dispatchers.IO).launch {
                    Intent(this@VideoEditorActivity, ForegroundService::class.java).also {
                        it.action = ForegroundService.Actions.START.toString()
                        it.putExtra("uriToService", uriString)
                        startService(it)
                    }
                }
            }
            addSubtitlesProgressBar.visibility = View.VISIBLE
        }
        editingOptionsLayout = findViewById(R.id.editingOptionsLayout)
        editButton = findViewById(R.id.editButton)

//        relativeLayout = findViewById(R.id.relativeLayout)
//        relativeLayout.setOnClickListener {
//            editingOptionsLayout.slideDown(100L, 0)
//        }

        val sync = ScrollSynchronizer(this)
        val l = ArrayList<RecyclerView>()
        l.add(framesRecyclerView)
        l.add(subtitlesRecyclerView)
        sync.addAll(l)

        editorVideoView.setOnPreparedListener{
//            editorVideoView.start()
            handler = Handler()
            handler.post(updateSeekbar)
        }
        editorVideoView.setOnCompletionListener {
            framesRecyclerView.scrollToPosition(framesAdapter.itemCount-1)
            playPauseBtn.setImageResource(R.drawable.ic_play)
            videoTimeHandler.removeCallbacks(updateTimeTask)
            currentVideoTime.text = totalVideoTime.text.substring(1)
            prevIndex = 0
        }
        subtitlesAdapter.onItemClick = {(pos,word) ->
            if(!editingOptionsLayout.isVisible){
                editingOptionsLayout.visibility = View.VISIBLE
                editingOptionsLayout.slideUp(200L,0)
            }
            else{
                editingOptionsLayout.visibility = View.GONE
                editingOptionsLayout.slideDown(200L,0)
            }
            editButton.setOnClickListener {
                val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetStyle)
                bottomSheetDialog.setContentView(R.layout.edit_subtitle)

                val editSubtitleText = bottomSheetDialog.findViewById<EditText>(R.id.editSubtitleText)
                editSubtitleText?.setText(word, TextView.BufferType.EDITABLE)

                bottomSheetDialog.show()
                val editSubtitleDone = bottomSheetDialog.findViewById<ImageView>(R.id.editSubtitleDone)
                editSubtitleDone?.setOnClickListener{
                    val editedSub = editSubtitleText?.text
                    subtitleArr[pos].word = editedSub.toString()
                    subtitleViewModel.updateSubtitleList(videoUri.toString(), subtitleArr)
                    bottomSheetDialog.dismiss()
                }
            }
        }

        val rootView = findViewById<View>(android.R.id.content)
        rootView.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN && editingOptionsLayout.isVisible){
                if(!isTouchInsideView(event.rawX, event.rawY, editingOptionsLayout)){
                    editingOptionsLayout.slideDown(200L,0)
                    editingOptionsLayout.visibility = View.INVISIBLE
                }
            }
            false
        }

        playPauseBtn.setOnClickListener {
            isVideoPlaying = !isVideoPlaying
            if(isVideoPlaying){
                playPauseBtn.setImageResource(R.drawable.ic_pause)
                editorVideoView.start()
                updateTimeTask.run()
            }
            else{
                playPauseBtn.setImageResource(R.drawable.ic_play)
                editorVideoView.pause()
                videoTimeHandler.removeCallbacks(updateTimeTask)
            }


        }
        framesRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (editorVideoView.isPlaying) {
                        editorVideoView.pause()
                        playPauseBtn.setImageResource(R.drawable.ic_play)
                    }
                }
//                MotionEvent.ACTION_UP -> {
//                    if (!editorVideoView.isPlaying) {
//                        editorVideoView.start()
//                    }
//                }
            }
            false
        }
        subtitlesRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (editorVideoView.isPlaying) {
                        editorVideoView.pause()
                        playPauseBtn.setImageResource(R.drawable.ic_play)
                    }
                }
//                MotionEvent.ACTION_UP -> {
//                    if (!editorVideoView.isPlaying) {
//                        editorVideoView.start()
//                    }
//                }
            }
            false
        }


    }
    fun findCenterItem(recyclerView: RecyclerView): Int? {
        val centerX = resources.displayMetrics.widthPixels/2.toFloat()
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val childX = child.x
            val childWidth = child.width

            // Check if the center point of the screen is within the bounds of the child view
            if (centerX in childX..(childX + childWidth)) {
                return recyclerView.getChildAdapterPosition(child)
            }
        }
        return null
    }
    private fun startTimer(duration : Long){
        val manager = getSystemService(NotificationManager::class.java)
        timerManager.startTimer(duration, onTick = { progress ->
            val notification = createNotification(progress)
            manager?.notify(1, notification)
        }, onFinish = {
            timerManager.cancelTimer()
        })
    }
    private fun createNotification(progress: Int): Notification {
        val cancelIntent = Intent(this, ForegroundService::class.java).apply {
            action = ForegroundService.Actions.STOP.toString()
            putExtra("isCancelledByUser", true)
        }
        val cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Generating Captions")
            .setContentText(progress.toString())
            .setOngoing(true)
            .addAction(R.drawable.ic_cancel,"Cancel", cancelPendingIntent)
            .setProgress(100, progress, false)
            .build()
    }

    private fun getMediaLength(uri : Uri) : Long{
        val mediaRetriever = MediaMetadataRetriever()
        mediaRetriever.setDataSource(this@VideoEditorActivity ,uri)
        val time = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return time!!.toLong()
    }
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    private fun showExportMenu(view: View?) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.export_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.exportSRT -> {
                    subtitleViewModel.readSubtitleByVideoId(uriString)
                    val subtitlesArrList = subtitleViewModel.subtitleOfVideoId.value?.get(0)?.subtitleList
                    CoroutineScope(Dispatchers.IO).launch {
                        if (subtitlesArrList != null) {
                            CreateSRT(subtitlesArrList).createSRTFile()
                        }
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@VideoEditorActivity, "Exported to /Downloads", Toast.LENGTH_LONG).show()
                        }
                    }
                    true

                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoTimeHandler.removeCallbacks(updateTimeTask)
    }
    fun getTimeFromMillis(millis : Long) : String{
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
    }

    private fun isTouchInsideView(
        x: Float,
        y: Float,
        editingOptionsLayout: LinearLayout?
    ): Boolean {
        val location = IntArray(2)
        editingOptionsLayout?.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]
        return (x > viewX && x < viewX + editingOptionsLayout!!.width && y > viewY && y < viewY + editingOptionsLayout.height)
    }
    private fun extractFramesFromVideo(uri : Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaRetreiver = MediaMetadataRetriever()
                mediaRetreiver.setDataSource(this@VideoEditorActivity, uri)
                val duration =
                    mediaRetreiver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong() ?: 0
                videoDuration = duration
                totalVideoTime.text = "/" + getTimeFromMillis(duration)
                val interval = 1000L
                val frameCount = ceil((duration.toDouble() / interval.toDouble())).toInt()
                val frames = ArrayList<Bitmap>()
                Log.d("doing@@@", "DOingAgain...")
                for (i in 0 until frameCount) {
                    val timeUs = i * interval * 1000
                    val bitmapFrame = mediaRetreiver.getFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    bitmapFrame?.let {
                        frames.add(it)
                    }
                }
                mediaRetreiver.release()
                mediaRetreiver.close()
                runOnUiThread {
                    framesAdapter.setData(frames)
                    framesAdapter.notifyDataSetChanged()
                    totalWidth = framesAdapter.itemCount * 70
                    factor = (videoDuration / totalWidth).toDouble()
                    inverseFactor = (totalWidth / videoDuration).toDouble()
                    println(inverseFactor)
                    subtitlesAdapter.setFactor(1 / factor)
                    subtitlesAdapter.notifyDataSetChanged()
                    videoEditorLoader.visibility = View.INVISIBLE
                    videoEditorLayout.background = null
                }

            } catch (e: Exception) {
                Log.d("error@@@", e.message.toString())
            }

        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//        super.onBackPressed()
    }

    var prevDist = 0.00
    private val updateSeekbar = object : Runnable {
        override fun run() {
            if (editorVideoView.isPlaying) {
                val s = ScrollSynchronizer(this@VideoEditorActivity)
                framesRecyclerView.removeOnScrollListener(s.scrollListener)

                if(wasScrolled){
                    editorVideoView.seekTo(whereSeekTo)
                    wasScrolled = false
                }

                val currentPosition = editorVideoView.currentPosition
                if(factor != 0.0){
                    val moveTo = (1/factor)*currentPosition
                    framesRecyclerView.scrollBy(((moveTo - prevDist)*resources.displayMetrics.density).toInt(),0)
                    subtitlesRecyclerView.scrollBy(((moveTo-prevDist)*resources.displayMetrics.density).toInt(),0)
                    dist = (moveTo*resources.displayMetrics.density).toInt()
                    prevDist = moveTo
                }
            }

            handler.postDelayed(this, 100)
        }
    }

    inner class ScrollSynchronizer(context: Context){
        val boundRecyclerViews : ArrayList<RecyclerView> = ArrayList()
        var verticalOffset = 0

        val scrollListener = object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val centerItemPosition = findCenterItem(recyclerView)
                if (centerItemPosition != null) {
                    if(centerItemPosition >= prevIndex && subtitleArr.isNotEmpty()){
                        subtitleTV.text = subtitleArr[centerItemPosition].word
                        prevIndex = centerItemPosition
                    }
                    else{
                        subtitleTV.text = ""
                    }
                }
                if(editingOptionsLayout.isVisible){
                    editingOptionsLayout.slideDown(200L,0)
                    editingOptionsLayout.visibility = View.INVISIBLE
                }
                if(!editorVideoView.isPlaying){
                    dist += dx
                    val dpDist = dist/context.resources.displayMetrics.density
                    val moveBy = dpDist * factor
                    prevDist = dpDist.toDouble()
                    wasScrolled = true
                    whereSeekTo = moveBy.toInt()
                    editorVideoView.seekTo(moveBy.toInt())
                    val otherRvs = boundRecyclerViews.filter { it != recyclerView }
                    otherRvs.forEach { targetView -> targetView.removeOnScrollListener(this) }
                    otherRvs.forEach { targetView -> targetView.scrollBy(dx, dy) }
                    otherRvs.forEach { targetView -> targetView.addOnScrollListener(this) }
                }


            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    verticalOffset = recyclerView.computeVerticalScrollOffset()
                    val s = findCenterItem(recyclerView)
                    s?.let {
                        prevIndex = s
                    }
                }

            }
        }
        fun add(view: RecyclerView) {
            with (view) {
                if (boundRecyclerViews.contains(view)) {
                    removeOnScrollListener(scrollListener)
                    boundRecyclerViews.remove(this)
                }
                addOnScrollListener(scrollListener)
                boundRecyclerViews.add(this)
            }
        }
        fun scrollToLastOffset(rv: RecyclerView) {
            rv.removeOnScrollListener(scrollListener)
            val currentOffset = rv.computeVerticalScrollOffset()
            if (currentOffset != verticalOffset) {
                rv.scrollBy(0, verticalOffset - currentOffset)
            }
            rv.addOnScrollListener(scrollListener)
        }

        fun disable() = boundRecyclerViews.forEach { it.removeOnScrollListener(scrollListener) }

        fun enable() = boundRecyclerViews.forEach { it.addOnScrollListener(scrollListener) }

        fun addAll(recyclerViews: List<RecyclerView>) = recyclerViews.forEach { add(it) }
    }
}