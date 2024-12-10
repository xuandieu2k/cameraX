package vn.xdeuhug.camerax.data.source

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture.withOutput
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import vn.xdeuhug.camerax.presentation.view.MediaRecordingService
import vn.xdeuhug.camerax.presentation.view.MediaRecordingService.Companion.BIND_USECASE
import vn.xdeuhug.camerax.presentation.view.MediaRecordingService.DataListener
import vn.xdeuhug.camerax.presentation.view.MediaRecordingService.RecordingServiceBinder
import vn.xdeuhug.camerax.presentation.view.MediaRecordingService.RecordingState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 12 / 2024
 */
class CameraXManager @Inject constructor(private val context: Context) {

    companion object {
        private val TAG = CameraXManager::class.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private var preview: Preview? = null
    private lateinit var timer: Timer
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var recordingServiceBinder: RecordingServiceBinder
    private var activeRecording: Recording? = null
    private var videoCapture: androidx.camera.video.VideoCapture<Recorder>? = null
    val listeners = HashSet<DataListener>(1)
    private val pendingActions: HashMap<String, Runnable> = hashMapOf()
    private var recordingState: RecordingState = RecordingState.STOPPED
    private var duration: Int = 0
    private var timerTask: TimerTask? = null
    private var isSoundEnabled: Boolean = true
    private var currentCameraSelector: CameraSelector =
        CameraSelector.DEFAULT_BACK_CAMERA // Select back camera as a default
    private var selectedQuality: Quality = Quality.HD

    fun setTimer(timer: Timer) {
        this.timer = timer
    }


    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()
            val qualitySelector = getQualitySelector()
            val recorder = Recorder.Builder().setQualitySelector(qualitySelector).build()
            videoCapture = withOutput(recorder)

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()
                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(lifecycleOwner, currentCameraSelector, videoCapture)
            } catch (exc: Exception) {
                Timber.e(MediaRecordingService::class.simpleName, "Use case binding failed", exc)
            }
            val action = pendingActions[BIND_USECASE]
            action?.run()
            pendingActions.remove(BIND_USECASE)
        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording() {
        val mediaStoreOutputOptions = createMediaStoreOutputOptions()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        activeRecording = videoCapture?.output?.prepareRecording(context, mediaStoreOutputOptions)
            ?.apply {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isSoundEnabled) withAudioEnabled()
                }
            }
            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        startTrackingTime()
                        recordingState = RecordingState.RECORDING
                    }

                    is VideoRecordEvent.Finalize -> {
                        recordingState = RecordingState.STOPPED
                        duration = 0
                        timerTask?.cancel()
                    }
                }
                for (listener in listeners) {
                    listener.onRecordingEvent(recordEvent)
                }
            }
        recordingState = RecordingState.RECORDING
    }

    fun switchCameraDuringRecording(lifecycleOwner: LifecycleOwner) {
        try {
            // Kiểm tra trạng thái ghi hình hiện tại
            if (recordingState == RecordingState.RECORDING) {
                Timber.d(TAG, "Pausing recording before switching camera")
                activeRecording?.pause() // Tạm dừng ghi
            }

            // Chuyển đổi camera
            currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            // Unbind các use cases cũ (chỉ Preview và VideoCapture)
            cameraProvider?.unbind(preview, videoCapture)

            // Bind lại Preview và VideoCapture với camera mới
            preview?.let { preview ->
                cameraProvider?.bindToLifecycle(lifecycleOwner, currentCameraSelector, preview, videoCapture)
            }

            // Tiếp tục ghi nếu trạng thái trước đó là RECORDING
            if (recordingState == RecordingState.RECORDING) {
                Timber.d(TAG, "Resuming recording after switching camera")
                activeRecording?.resume() // Tiếp tục ghi
            }
        } catch (e: Exception) {
            Timber.e(TAG, "Error switching camera during recording: ${e.message}")
        }
    }



    private fun startTrackingTime() {
        timerTask = object : TimerTask() {
            override fun run() {
                if (recordingState == RecordingState.RECORDING) {
                    duration += 1
                    for (listener in listeners) {
                        listener.onNewData(duration)
                    }
                }
            }
        }
        timer.schedule(timerTask, 1000, 1000)
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    private fun createMediaStoreOutputOptions(): MediaStoreOutputOptions {
        val name =
            "CameraX-recording-" + SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(
                System.currentTimeMillis()
            ) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Recorded Videos")
            }
        }
        return MediaStoreOutputOptions.Builder(
            context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
    }

    fun bindPreviewUseCase(
        surfaceProvider: Preview.SurfaceProvider?,
        lifecycleOwner: LifecycleOwner
    ) {
        activeRecording?.pause()
        if (cameraProvider != null) {
            bindInternal(surfaceProvider, lifecycleOwner)
        } else {
            pendingActions[BIND_USECASE] = Runnable {
                bindInternal(surfaceProvider, lifecycleOwner)
            }
        }
    }

    private fun bindInternal(
        surfaceProvider: Preview.SurfaceProvider?,
        lifecycleOwner: LifecycleOwner
    ) {
        if (preview != null) {
            cameraProvider?.unbind(preview)
        }
        initPreviewUseCase()
        preview?.setSurfaceProvider(surfaceProvider)
        val cameraInfo: CameraInfo? = cameraProvider?.bindToLifecycle(
            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview
        )?.cameraInfo
        observeCameraState(cameraInfo, context, lifecycleOwner)
    }

    private fun observeCameraState(
        cameraInfo: androidx.camera.core.CameraInfo?,
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) {
        cameraInfo?.cameraState?.observe(lifecycleOwner) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                    }

                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        for (listener in listeners) {
                            listener.onCameraOpened()
                        }
                    }

                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                    }

                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                    }

                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Toast.makeText(
                            context, "Stream config error. Restart application", Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(
                            context,
                            "Camera in use. Close any apps that are using the camera",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                    }

                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {

                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Toast.makeText(
                            context, "Camera disabled", Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(
                            context, "Fatal error", Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(
                            context, "Do not disturb mode enabled", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun initPreviewUseCase() {
        preview?.setSurfaceProvider(null)
        preview = Preview.Builder().build()
    }

    fun bindPreview(surfaceProvider: Preview.SurfaceProvider, lifecycleOwner: LifecycleOwner) {
        preview?.setSurfaceProvider(surfaceProvider)
        cameraProvider?.bindToLifecycle(
            lifecycleOwner, currentCameraSelector, preview, videoCapture
        )
    }

    fun unbindPreview() {
        // Just remove the surface provider. I discovered that for some reason if you unbind the Preview usecase the camera willl stop recording the video.
        preview?.setSurfaceProvider(null)
    }

    fun switchCamera(lifecycleOwner: LifecycleOwner) {
        // Tạm dừng ghi video
        if (recordingState == RecordingState.RECORDING) {
            activeRecording?.pause()
            recordingState = RecordingState.PAUSED
        }

        // Chuyển camera
        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        cameraProvider?.let {
            val currentRecording = activeRecording // Lưu trạng thái ghi hiện tại

            // Unbind tất cả các use cases trước
            it.unbindAll()

            // Bind lại Preview và VideoCapture với camera mới
            preview?.let { preview ->
                it.bindToLifecycle(lifecycleOwner, currentCameraSelector, preview, videoCapture)
            }

            // Tiếp tục ghi video sau khi chuyển camera
            if (currentRecording != null && recordingState == RecordingState.PAUSED) {
                startRecording() // Bắt đầu ghi lại video
            }
        }
    }


//    fun setTargetRotation(rotation: Int) {
//        preview?.targetRotation = rotation
//        videoCapture?.targetRotation = rotation
//        initializeCamera()
//    }
//
//
//    fun setVideoQuality(quality: Quality) {
//        selectedQuality = quality
//        initializeCamera()
//    }

    private fun getQualitySelector(): QualitySelector {
        return QualitySelector.from(selectedQuality)
    }


    fun toggleFlash(enable: Boolean, lifecycleOwner: LifecycleOwner) {
        val camera = cameraProvider?.bindToLifecycle(lifecycleOwner, currentCameraSelector)
        camera?.cameraControl?.enableTorch(enable)
    }

    fun isSoundEnabled(): Boolean {
        return isSoundEnabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun setZoomLevel(zoomLevel: Float, lifecycleOwner: LifecycleOwner) {
        val camera = cameraProvider?.bindToLifecycle(lifecycleOwner, currentCameraSelector)
        val cameraControl = camera?.cameraControl
        val cameraInfo = camera?.cameraInfo

        val maxZoom = cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
        val minZoom = cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
        val clampedZoom = zoomLevel.coerceIn(minZoom, maxZoom)

        cameraControl?.setZoomRatio(clampedZoom)
    }

    fun getRecordingState(): RecordingState {
        return recordingState
    }

    fun setRecordingServiceBinder(recordingServiceBinder: RecordingServiceBinder) {
        this.recordingServiceBinder = recordingServiceBinder
    }

    fun getRecordingServiceBinder(): RecordingServiceBinder {
        return this.recordingServiceBinder
    }

    fun getCameraProvider(): ProcessCameraProvider? {
        return cameraProvider
    }

    fun destroyCamera() {
        activeRecording?.stop()
        timerTask?.cancel()
    }

}