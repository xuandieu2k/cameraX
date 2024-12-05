package vn.xdeuhug.test.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.core.app.NotificationCompat
import vn.xdeuhug.test.R

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRecordingService : Service() {

    private val binder = LocalBinder()
    private var isRecording = false
    private var videoCapture: VideoCapture<Recorder>? = null

    inner class LocalBinder : Binder() {
        fun getService(): VideoRecordingService = this@VideoRecordingService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    @SuppressLint("ForegroundServiceType")
    fun startRecording(outputPath: String) {
        // Implement start recording logic here
        isRecording = true
        startForeground(
            1,
            NotificationCompat.Builder(this, "video_channel")
                .setContentTitle("Recording Video")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        )
    }

    fun stopRecording() {
        isRecording = false
        @Suppress("DEPRECATION")
        stopForeground(true)
    }

    fun isRecording(): Boolean = isRecording
}