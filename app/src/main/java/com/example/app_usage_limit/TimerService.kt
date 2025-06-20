package com.example.app_usage_limit

import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.Service
import android.os.Handler
import android.os.Looper
import android.os.IBinder
import com.example.app_usage_limit.util.OverlayUtil


class TimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var durationMillis: Long = 0L
    private var isTimerRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null
    private var repeatCount = 0  // 반복 횟수 카운트

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TIMER" -> {
                repeatCount=0
                durationMillis = intent.getLongExtra("durationMillis", 0L)
                startForeground(1, createNotification())
                isTimerRunning=true
                startRepeatingTimer(durationMillis)
            }
            "STOP_TIMER" -> {
                Log.d("TimerService", "STOP_TIMER 수신됨")
                stopRepeatingTimer()
                isTimerRunning = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    stopForeground(Service.STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }

                stopSelf()
            }
        }
        return START_STICKY
    }
    private fun stopRepeatingTimer() {
        countDownTimer?.cancel()
        countDownTimer = null

        repeatRunnable?.let {
            handler.removeCallbacks(it)
            repeatRunnable = null
        }

        Log.d("TimerService", "타이머 중지됨 및 반복 예약 취소됨")
    }

    private fun startRepeatingTimer(duration: Long) {
        stopRepeatingTimer()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("TimerService", "남은 시간: $millisUntilFinished")
            }


            override fun onFinish() {
                if (isTimerRunning) {
                    repeatCount++
                    val totalUsedSeconds = (durationMillis / 1000) * repeatCount
                    OverlayUtil.showOverlayView(applicationContext, totalUsedSeconds)

                    repeatRunnable = Runnable {
                        if (isTimerRunning) {
                            startRepeatingTimer(durationMillis)
                        }
                    }
                    handler.postDelayed(repeatRunnable!!, 1000)
                }
            }
        }.start()
    }

    private fun createNotification(): Notification {
        val channelId = "timer_channel"
        val channelName = "Timer Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("타이머 실행 중")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
