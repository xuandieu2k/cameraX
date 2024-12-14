package vn.xdeuhug.camerax.domain.repository


import android.view.ScaleGestureDetector
import androidx.camera.core.ImageCapture
import androidx.camera.video.Quality
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import vn.xdeuhug.camerax.data.source.CameraXManager.CameraXListener
import javax.inject.Singleton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@Singleton
interface VideoRecordingRepository {
    suspend fun initializeCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView)
    suspend fun startRecording(): Boolean
    suspend fun stopRecording(): Boolean
    suspend fun switchCamera(lifecycleOwner: LifecycleOwner): Boolean
    suspend fun setAspectRatio(ratio: Int, lifecycleOwner: LifecycleOwner)
    suspend fun setQuality(quality: Quality, lifecycleOwner: LifecycleOwner)
    suspend fun toggleFlash(enable: Boolean, lifecycleOwner: LifecycleOwner)
    suspend fun pinchZoom(detector: ScaleGestureDetector, lifecycleOwner: LifecycleOwner)
    suspend fun takeAPicture(onImageSaved: ImageCapture.OnImageSavedCallback)
    suspend fun destroyCamera()
    suspend fun addListenerCameraX(listener: CameraXListener)
    suspend fun removeListenerCameraX(listener: CameraXListener)
    suspend fun bindPreview(preview: PreviewView, lifecycleOwner: LifecycleOwner)
}