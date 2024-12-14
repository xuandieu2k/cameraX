package vn.xdeuhug.camerax.domain.usecase

import android.view.ScaleGestureDetector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import vn.xdeuhug.camerax.data.source.CameraXManager.CameraXListener
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import vn.xdeuhug.camerax.utils.Resource
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRecordingUseCase @Inject constructor(private val repository: VideoRecordingRepository) {

    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ): Resource<Boolean> {
        try {
            repository.initializeCamera(lifecycleOwner, previewView)
            return Resource.Success(true)
        } catch (ex: Exception) {
            return Resource.Error(ex.toString())
        }
    }

    suspend fun startRecording(): Resource<Boolean> {
        try {
            repository.startRecording()
            return Resource.Success(true)
        } catch (ex: Exception) {
            return Resource.Error(ex.toString())
        }
    }

    suspend fun stopRecording(): Resource<Boolean> {
        try {
            repository.stopRecording()
            return Resource.Success(true)
        } catch (ex: Exception) {
            return Resource.Error(ex.toString())
        }
    }

    suspend fun switchCamera(lifecycleOwner: LifecycleOwner): Resource<Boolean> {
        try {
            repository.switchCamera(lifecycleOwner)
            return Resource.Success(true)
        } catch (ex: Exception) {
            return Resource.Error(ex.toString())
        }
    }

    suspend fun pinchZoom(
        detector: ScaleGestureDetector,
        lifecycleOwner: LifecycleOwner
    ): Resource<Boolean> {
        try {
            repository.pinchZoom(detector, lifecycleOwner)
            return Resource.Success(true)
        } catch (ex: Exception) {
            return Resource.Error(ex.toString())
        }
    }

    suspend fun addListenerCameraX(listener: CameraXListener) {
        repository.addListenerCameraX(listener)
    }

    suspend fun removeListenerCameraX(listener: CameraXListener) {
        repository.removeListenerCameraX(listener)
    }

    suspend fun destroyCamera() {
        repository.destroyCamera()
    }

    suspend fun takeAPicture(onImageSaved: ImageCapture.OnImageSavedCallback) {
        repository.takeAPicture(onImageSaved)
    }

    suspend fun bindPreview(preview: PreviewView, lifecycleOwner: LifecycleOwner) {
        repository.bindPreview(preview, lifecycleOwner)
    }
}