package vn.xdeuhug.camerax.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import vn.xdeuhug.camerax.presentation.view.VideoRecordingActivity
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 12 / 2024
 */
@AndroidEntryPoint
class VideoRecordingService : LifecycleService() {

    private lateinit var wakeLock: PowerManager.WakeLock

    @Inject
    lateinit var repository: VideoRecordingRepository

    override fun onCreate() {
        super.onCreate()
        Timber.tag("Service VideoRecordingService Create").d("Create")
        acquireWakeLock()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleCommand(intent)
        return START_STICKY // Service sẽ tự khởi động lại nếu bị dừng
    }

    private fun handleCommand(intent: Intent?) {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val outputFilePath = intent.getStringExtra("OUTPUT_FILE_PATH")
                if (outputFilePath.isNullOrEmpty()) {
                    Timber.e("Output file path is null or empty!")
                    stopSelf()
                    return
                }
                startVideoRecording(outputFilePath)
            }

            ACTION_STOP_RECORDING -> stopVideoRecording()
        }
    }

    private fun startVideoRecording(outputFilePath: String) {
        lifecycleScope.launch {
            val success = repository.startRecording(outputFilePath)
            if (!success) {
                stopSelf() // Nếu thất bại, tự dừng service
            }
        }
    }

    private fun stopVideoRecording() {
        lifecycleScope.launch {
            repository.stopRecording()
            stopSelf()
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val channelId = "video_recording_service"
        val channelName = "Video Recording"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recording video in background")
            .setSmallIcon(R.drawable.ic_recording)
            .setOngoing(true)  // Đảm bảo notification không bị xóa
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }


//    private fun createNotification(): Notification {
//        val channelId = "video_recording_channel"
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Tạo kênh thông báo nếu API >= 26
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Video Recording Service",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        return NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Video Recording")
//            .setContentText("Recording video in progress...")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setOngoing(true)
//            .build()
//    }


    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, VideoRecordingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "VideoRecordingService::WakelockTag"
        )
        wakeLock.acquire()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    companion object {
        const val ACTION_START_RECORDING = "com.example.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.action.STOP_RECORDING"
    }
}
