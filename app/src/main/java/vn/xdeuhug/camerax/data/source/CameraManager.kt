package vn.xdeuhug.camerax.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 12 / 2024
 */
class CameraManager @Inject constructor(private val context: Context) {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var activeRecording: Recording? = null

    fun setupCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.FHD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                Timber.e("Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording(outputFilePath: String): Boolean {
        if (videoCapture == null) {
            Timber.e("VideoCapture is not initialized yet.")
            return false
        }

        val file = File(outputFilePath)
        val outputOptions = FileOutputOptions.Builder(file).build()

        activeRecording = videoCapture?.output?.prepareRecording(context, outputOptions)
            ?.apply {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> Timber.tag("Log Recording:").d("Recording started")
                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                            Timber.tag("Log Recording:").d("Recording finalized successfully")
                        } else {
                            Timber.tag("Log Recording:").e("Recording failed with error: ${recordEvent.error}")
                        }
                    }
                }
            }
        return true
    }

    fun stopRecording(): Boolean {
        activeRecording?.stop()
        activeRecording = null
        return true
    }


    fun switchCamera(): Boolean {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        return true
    }

    fun getAvailableQualities(): List<Quality> {
        return listOf(Quality.HD, Quality.FHD, Quality.UHD)
    }

    fun getContext(): Context {
        return context
    }
}
