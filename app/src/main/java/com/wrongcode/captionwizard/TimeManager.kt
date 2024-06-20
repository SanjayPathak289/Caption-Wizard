import android.os.CountDownTimer

class TimerManager {
    var timer: CountDownTimer? = null

    fun startTimer(duration: Long, onTick: (progress: Int) -> Unit, onFinish: () -> Unit) {
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(remainingMillis: Long) {
                val progress = ((100 * (duration - remainingMillis)) / duration).toInt()
                onTick(progress)
            }

            override fun onFinish() {
                onFinish()
                timer = null
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }
}
