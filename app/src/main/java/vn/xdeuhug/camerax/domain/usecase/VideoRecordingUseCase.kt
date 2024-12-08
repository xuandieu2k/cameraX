package vn.xdeuhug.camerax.domain.usecase

import android.content.Context
import androidx.camera.video.Quality
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRecordingUseCase @Inject constructor(private val repository: VideoRecordingRepository) {

    private val _isRecording: MutableLiveData<Boolean> = MutableLiveData(false)
    val isRecording: LiveData<Boolean> get() = _isRecording

    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        repository.setupCamera( lifecycleOwner, previewView)
    }

    suspend fun startRecording(outputFilePath: String): Boolean {
        return repository.startRecording(outputFilePath)
    }

    suspend fun stopRecording(): Boolean {
        _isRecording.value = repository.stopRecording()
        return _isRecording.value ?: false
    }

    suspend fun switchCamera(): Boolean {
        return repository.switchCamera()
    }

    suspend fun getAvailableQualities(): List<Quality> {
        return repository.getAvailableQualities()
    }
}