//package vn.xdeuhug.camerax.data.repository
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.MediaStoreOutputOptions
//import androidx.camera.video.Quality
//import androidx.camera.video.QualitySelector
//import androidx.camera.video.Recorder
//import androidx.camera.video.Recording
//import androidx.camera.video.VideoCapture
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import timber.log.Timber
//
///**
// * @Author: NGUYEN XUAN DIEU
// * @Date: 09 / 12 / 2024
// */
//class CameraRepository(private val context: Context) {
//
//    private var cameraProvider: ProcessCameraProvider? = null
//    private var preview: Preview? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//
//    fun initializeCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider?) {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//        cameraProviderFuture.addListener({
//            cameraProvider = cameraProviderFuture.get()
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.FHD))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            preview = Preview.Builder().build().apply {
//                setSurfaceProvider(surfaceProvider)
//            }
//
//            try {
//                cameraProvider?.unbindAll()
//                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
//            } catch (e: Exception) {
//                Timber.e("Camera binding failed: ${e.message}")
//            }
//        }, ContextCompat.getMainExecutor(context))
//    }
//
//    fun startRecording(outputOptions: MediaStoreOutputOptions, audioEnabled: Boolean): Recording? {
//        return videoCapture?.output
//            ?.prepareRecording(context, outputOptions)
//            ?.apply {
//                if (ActivityCompat.checkSelfPermission(
//                        context,
//                        Manifest.permission.RECORD_AUDIO
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//
//                    if (audioEnabled) withAudioEnabled()
//                }
//            }
//            ?.start(ContextCompat.getMainExecutor(context)) {
//                //
//            }
//    }
//
//    fun stopRecording(recording: Recording?) {
//        recording?.stop()
//    }
//
//    fun unbindAll() {
//        cameraProvider?.unbindAll()
//    }
//}
