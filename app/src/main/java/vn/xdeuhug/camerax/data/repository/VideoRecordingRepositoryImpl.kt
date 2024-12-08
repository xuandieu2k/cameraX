package vn.xdeuhug.camerax.data.repository

import android.content.Context
import androidx.camera.video.Quality
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import vn.xdeuhug.camerax.data.source.CameraManager
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRecordingRepositoryImpl @Inject constructor(
    private val cameraManager: CameraManager
) : VideoRecordingRepository {
    override suspend fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        cameraManager.setupCamera(lifecycleOwner,previewView)
    }

    override suspend fun startRecording(outputFilePath: String): Boolean {
        return cameraManager.startRecording(outputFilePath)
    }

    override suspend fun stopRecording(): Boolean {
        return cameraManager.stopRecording()
    }

    override suspend fun switchCamera(): Boolean {
        return cameraManager.switchCamera()
    }

    override suspend fun getAvailableQualities(): List<Quality> {
        return cameraManager.getAvailableQualities()
    }
}
