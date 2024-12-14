package vn.xdeuhug.camerax.data.repository

import android.view.ScaleGestureDetector
import androidx.camera.core.ImageCapture
import androidx.camera.video.Quality
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import vn.xdeuhug.camerax.data.source.CameraXManager
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRecordingRepositoryImpl @Inject constructor(
    private val cameraManager: CameraXManager
) : VideoRecordingRepository {

    override suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        //
    }

    override suspend fun startRecording(): Boolean {
        try {
            cameraManager.startRecording()
            return true
        } catch (ex: Exception) {
            Timber.tag("VideoRecordingRepositoryImpl").e(ex)
            return false
        }
    }

    override suspend fun stopRecording(): Boolean {
        try {
            cameraManager.stopRecording()
            return true
        } catch (ex: Exception) {
            Timber.tag("VideoRecordingRepositoryImpl").e(ex)
            return false
        }
    }

    override suspend fun switchCamera(lifecycleOwner: LifecycleOwner): Boolean {
        try {
            cameraManager.switchCamera(lifecycleOwner)
            return true
        } catch (ex: Exception) {
            Timber.tag("VideoRecordingRepositoryImpl").e(ex)
            return false
        }
    }

    override suspend fun setAspectRatio(ratio: Int, lifecycleOwner: LifecycleOwner) {
        cameraManager.setAspectRatio(ratio, lifecycleOwner)
    }

    override suspend fun setQuality(quality: Quality, lifecycleOwner: LifecycleOwner) {
        cameraManager.setVideoQuality(quality, lifecycleOwner)
    }

    override suspend fun toggleFlash(enable: Boolean, lifecycleOwner: LifecycleOwner) {
        cameraManager.toggleFlash(enable, lifecycleOwner)
    }

    override suspend fun pinchZoom(detector: ScaleGestureDetector, lifecycleOwner: LifecycleOwner) {
        cameraManager.pinchZoom(detector, lifecycleOwner)
    }

    override suspend fun takeAPicture(onImageSaved: ImageCapture.OnImageSavedCallback) {
        cameraManager.takeAPicture(onImageSaved)
    }

    override suspend fun destroyCamera() {
        cameraManager.destroyCamera()
    }

    override suspend fun addListenerCameraX(listener: CameraXManager.CameraXListener) {
        cameraManager.addListenerCameraX(listener)
    }

    override suspend fun removeListenerCameraX(listener: CameraXManager.CameraXListener) {
        cameraManager.removeListenerCameraX(listener)
    }

    override suspend fun bindPreview(preview: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraManager.bindPreview(preview.surfaceProvider, lifecycleOwner)
    }

}
