package vn.xdeuhug.camerax.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.ScaleGestureDetector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.video.Quality
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.data.source.CameraXManager
import vn.xdeuhug.camerax.presentation.view.MainActivity
import java.util.Timer
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

@AndroidEntryPoint
class MediaRecordingService : LifecycleService() {

    @Inject
    lateinit var cameraXManager: CameraXManager

    companion object {
        const val CHANNEL_ID: String = "media_recorder_service"
        const val CHANNEL_NAME: String = "Media recording service"
        const val ONGOING_NOTIFICATION_ID: Int = 2345
        const val ACTION_START_WITH_PREVIEW: String = "start_recording"
        const val BIND_USECASE: String = "bind_usecase"
    }

    class RecordingServiceBinder(private val service: MediaRecordingService) : Binder() {
        fun getService(): MediaRecordingService {
            return service
        }
    }

    private var aspectRatio: Int = androidx.camera.core.AspectRatio.RATIO_4_3

    override fun onCreate() {
        super.onCreate()
        cameraXManager.setRecordingServiceBinder(RecordingServiceBinder(this))
        cameraXManager.setTimer(Timer())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_WITH_PREVIEW -> {
                if (cameraXManager.getCameraProvider() == null) {
                    initializeCamera()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun initializeCamera() {
        cameraXManager.initializeCamera(this)
    }

    fun startRecording() {
        cameraXManager.startRecording()
    }


    fun stopRecording() {
        cameraXManager.stopRecording()
    }

    fun bindPreviewUseCase(surfaceProvider: Preview.SurfaceProvider?) {
        cameraXManager.bindPreviewUseCase(surfaceProvider, this)
    }

    fun bindPreview(surfaceProvider: Preview.SurfaceProvider) {
        cameraXManager.bindPreview(surfaceProvider, this)
    }

    fun unbindPreview() {
        cameraXManager.unbindPreview()
    }

    fun startRunningInForeground() {
        val parentStack = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(Intent(this, MainActivity::class.java))
        val randomID = ThreadLocalRandom.current().nextInt(0, 1000)
        val resultPendingIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                parentStack.getPendingIntent(
                    randomID, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                parentStack.getPendingIntent(randomID, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.video_recording))
            .setContentText(getText(R.string.video_recording_in_background))
            .setSmallIcon(R.drawable.ic_recording).setContentIntent(resultPendingIntent).build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(ONGOING_NOTIFICATION_ID, notification)
        } else {
            startForeground(
                ONGOING_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        }

    }

    fun setAspectRatio(ratio: Int) {
        cameraXManager.setAspectRatio(ratio, this)
    }


    fun switchCamera() {
        cameraXManager.switchCamera(this)
    }


    fun setTargetRotation(rotation: Int) {
        cameraXManager.setTargetRotation(rotation, this)
    }


    fun setVideoQuality(quality: Quality) {
        cameraXManager.setVideoQuality(quality, this)
    }

    fun takeAPicture(onImageSaved: ImageCapture.OnImageSavedCallback) {
        cameraXManager.takeAPicture(onImageSaved)
    }


    fun toggleFlash(enable: Boolean) {
        cameraXManager.toggleFlash(enable, this)
    }

    fun setZoomLevel(zoomLevel: Float) {
        cameraXManager.setZoomLevel(zoomLevel, this)
    }

    fun pinchZoom(detector: ScaleGestureDetector) {
        cameraXManager.pinchZoom(detector, this)
    }

    // Stop recording and remove SurfaceView
    override fun onDestroy() {
        super.onDestroy()
        cameraXManager.destroyCamera()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return cameraXManager.getRecordingServiceBinder()
    }

    fun addListener(listener: CameraXManager.CameraXListener) {
        cameraXManager.listeners.add(listener)
    }

    fun removeListener(listener: CameraXManager.CameraXListener) {
        cameraXManager.listeners.remove(listener)
    }

    fun getRecordingState(): CameraXManager.RecordingState {
        return cameraXManager.getRecordingState()
    }
}