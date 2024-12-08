package vn.xdeuhug.camerax.domain.repository


import androidx.camera.video.Quality
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import javax.inject.Singleton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@Singleton
interface VideoRecordingRepository {
    suspend fun setupCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView)
    suspend fun startRecording(outputFilePath: String): Boolean
    suspend fun stopRecording(): Boolean
    suspend fun switchCamera(): Boolean
    suspend fun getAvailableQualities(): List<Quality>
}