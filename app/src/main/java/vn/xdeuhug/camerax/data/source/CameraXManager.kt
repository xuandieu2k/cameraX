package vn.xdeuhug.camerax.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Environment
import android.provider.MediaStore
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoCapture.withOutput
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import vn.xdeuhug.camerax.presentation.service.MediaRecordingService
import vn.xdeuhug.camerax.utils.FileUtils
import java.io.File
import java.io.FileInputStream
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import javax.inject.Inject


class CameraXManager @Inject constructor(private val context: Context) {

    companion object {
        private val TAG = CameraXManager::class.simpleName
        const val BIND_USECASE: String = "bind_usecase"
    }

    private var preview: Preview? = null
    private lateinit var timer: Timer
    private var cameraProvider: ProcessCameraProvider? = null
    private var activeRecording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageCapture: ImageCapture? = null
    val listeners = HashSet<CameraXListener>(1)
    private val pendingActions: HashMap<String, Runnable> = hashMapOf()
    private var recordingState: RecordingState = RecordingState.STOPPED
    private lateinit var recordingServiceBinder: MediaRecordingService.RecordingServiceBinder
    private var duration: Int = 0
    private var timerTask: TimerTask? = null
    private var isSoundEnabled: Boolean = true
    private var currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var selectedQuality: Quality = Quality.HD
    private var aspectRatio: Int = AspectRatio.RATIO_16_9

    enum class RecordingState {
        RECORDING, PAUSED, STOPPED
    }

    class RecordingServiceBinder(private val service: MediaRecordingService) : Binder() {
        fun getService(): MediaRecordingService {
            return service
        }
    }


    fun setTimer(timer: Timer) {
        this.timer = timer
    }

    fun getRecordingState(): RecordingState {
        return recordingState
    }

    fun setRecordingServiceBinder(recordingServiceBinder: MediaRecordingService.RecordingServiceBinder) {
        this.recordingServiceBinder = recordingServiceBinder
    }

    fun getRecordingServiceBinder(): MediaRecordingService.RecordingServiceBinder {
        return this.recordingServiceBinder
    }

    fun getCameraProvider(): ProcessCameraProvider? {
        return cameraProvider
    }


    // ##### Main Function (Init/Start/Stop) ####
    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val qualitySelector = getQualitySelector()
            val recorder =
                Recorder.Builder().setAspectRatio(aspectRatio).setQualitySelector(qualitySelector)
                    .build()
            videoCapture = withOutput(recorder)
            @Suppress("DEPRECATION")
            imageCapture = ImageCapture.Builder().setTargetAspectRatio(aspectRatio).build()

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner, currentCameraSelector, videoCapture, imageCapture
                )
            } catch (exc: Exception) {
                Timber.e(CameraXManager::class.simpleName, "Use case binding failed", exc)
            }
            val action = pendingActions[BIND_USECASE]
            action?.run()
            pendingActions.remove(BIND_USECASE)
        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording() {
        val videoDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.cacheDir
        val videoFile = File(videoDir, "video_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        val mediaStoreOutputOptions = FileUtils.createMediaStoreOutputOptions(context = context)
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.e(
                CameraXManager::class.simpleName,
                "startRecording fail: Permission not allowed"
            )
            return
        }

        activeRecording =
            videoCapture?.output?.prepareRecording(context, outputOptions)?.apply {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isSoundEnabled) withAudioEnabled()
                }
            }?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        startTrackingTime()
                        updateRecordingState(RecordingState.RECORDING)
                    }

                    is VideoRecordEvent.Status -> {
                        val fileSizeInBytes = videoFile.length()
                        Timber.tag("CameraX size:")
                            .d("Dung lượng tệp hiện tại: $fileSizeInBytes bytes")
                    }

                    is VideoRecordEvent.Finalize -> {
                        val contentUri = mediaStoreOutputOptions.contentResolver.insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            mediaStoreOutputOptions.contentValues
                        )
                        contentUri?.let {
                            mediaStoreOutputOptions.contentResolver.openOutputStream(contentUri)
                                ?.use { outputStream ->
                                    FileInputStream(videoFile).use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                        }
                        updateRecordingState(RecordingState.STOPPED)
                        duration = 0
                        timerTask?.cancel()
                    }
                }
                for (listener in listeners) {
                    listener.onRecordingEvent(recordEvent)
                }
            }
        updateRecordingState(RecordingState.RECORDING)
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun destroyCamera() {
        activeRecording?.stop()
        activeRecording = null
        timerTask?.cancel()
    }


    fun takeAPicture(onImageSaved: ImageCapture.OnImageSavedCallback) {
        if (imageCapture == null) Timber.tag("Log Take A Picture").e("Image Capture is null")
        imageCapture?.takePicture(FileUtils.createPictureStoreOutput(context = context),
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    onImageSaved.onError(error)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onImageSaved.onImageSaved(outputFileResults)
                }
            })
    }


    // ###### Function of camera (zoom, switch, flash,...)
    fun switchCameraDuringRecording(lifecycleOwner: LifecycleOwner) {
        try {
            if (recordingState == RecordingState.RECORDING) {
                Timber.d(TAG, "Pausing recording before switching camera")
                activeRecording?.pause()
            }

            currentCameraSelector =
                if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

            cameraProvider?.unbind(preview, videoCapture)

            preview?.let { preview ->
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner, currentCameraSelector, preview, videoCapture
                )
            }

            if (recordingState == RecordingState.RECORDING) {
                Timber.d(TAG, "Resuming recording after switching camera")
                activeRecording?.resume()
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

    fun bindPreviewUseCase(
        surfaceProvider: Preview.SurfaceProvider?, lifecycleOwner: LifecycleOwner
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
        surfaceProvider: Preview.SurfaceProvider?, lifecycleOwner: LifecycleOwner
    ) {
        if (preview != null) {
            cameraProvider?.unbind(preview)
        }
        initPreviewUseCase()
        preview?.setSurfaceProvider(surfaceProvider)
        val cameraInfo: CameraInfo? = cameraProvider?.bindToLifecycle(
            lifecycleOwner, currentCameraSelector, preview
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
                        //
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
        @Suppress("DEPRECATION")
        preview = Preview.Builder().setTargetAspectRatio(aspectRatio).build()
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
        if (recordingState == RecordingState.RECORDING) {
            activeRecording?.pause()
            updateRecordingState(RecordingState.PAUSED)
        }

        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        cameraProvider?.let {
            val currentRecording = activeRecording

            it.unbindAll()

            preview?.let { preview ->
                it.bindToLifecycle(lifecycleOwner, currentCameraSelector, preview, videoCapture)
            }

            if (currentRecording != null && recordingState == RecordingState.PAUSED) {
                startRecording()
            }
        }
    }

    fun setAspectRatio(ratio: Int, lifecycleOwner: LifecycleOwner) {
        aspectRatio = ratio
        initializeCamera(lifecycleOwner)
    }


    fun setTargetRotation(rotation: Int, lifecycleOwner: LifecycleOwner) {
//        preview?.targetRotation = rotation
        videoCapture?.targetRotation = rotation
        imageCapture?.targetRotation = rotation
        initializeCamera(lifecycleOwner)
    }


    fun setVideoQuality(quality: Quality, lifecycleOwner: LifecycleOwner) {
        selectedQuality = quality
        initializeCamera(lifecycleOwner)
    }

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

    fun pinchZoom(detector: ScaleGestureDetector, lifecycleOwner: LifecycleOwner) {
        val camera = cameraProvider?.bindToLifecycle(lifecycleOwner, currentCameraSelector)
        camera?.let {
            val scale = it.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
            it.cameraControl.setZoomRatio(scale)
            for (listener in listeners) {
                listener.onPinchZoomCamera(scale)
            }
        }
    }

    fun addListenerCameraX(listener: CameraXListener) {
        listeners.add(listener)
    }

    fun removeListenerCameraX(listener: CameraXListener) {
        listeners.remove(listener)
    }

    private fun updateRecordingState(recordingState: RecordingState) {
        this.recordingState = recordingState
        for (listener in listeners) {
            listener.onRecordingStateChanged(recordingState)
        }
    }

    interface CameraXListener {
        fun onNewData(duration: Int)
        fun onCameraOpened()
        fun onRecordingEvent(it: VideoRecordEvent?)
        fun onPinchZoomCamera(scale: Float)
        fun onRecordingStateChanged(recordingState: RecordingState)
    }

}