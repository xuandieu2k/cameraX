package vn.xdeuhug.camerax.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.camera.core.CameraSelector
import androidx.camera.video.Quality
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber
import vn.xdeuhug.camerax.domain.model.VideoConfig
import vn.xdeuhug.camerax.domain.usecase.VideoRecordingUseCase
import vn.xdeuhug.camerax.presentation.service.VideoRecordingService
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@HiltViewModel
class VideoRecordingViewModel @Inject constructor(
    private val useCase: VideoRecordingUseCase
) : ViewModel() {

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _qualities = MutableLiveData<List<Quality>>()
    val qualities: LiveData<List<Quality>> = _qualities

    fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            useCase.initializeCamera( lifecycleOwner, previewView)
        }
    }

    fun startRecording(context: Context, outputFilePath: String) {
        viewModelScope.launch {
            val intent = Intent(context, VideoRecordingService::class.java).apply {
                action = VideoRecordingService.ACTION_START_RECORDING
                putExtra("OUTPUT_FILE_PATH", outputFilePath)
            }
            ContextCompat.startForegroundService(context, intent)
            Timber.tag("Log Data").d("Start recording intent sent."+useCase.isRecording.value)
            _isRecording.value = true
//            _isRecording.value = useCase.startRecording(outputFilePath)
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            _isRecording.value = !useCase.stopRecording()
        }
    }

    fun switchCamera() {
        viewModelScope.launch {
            useCase.switchCamera()
        }
    }

    fun loadAvailableQualities() {
        viewModelScope.launch {
            _qualities.value = useCase.getAvailableQualities()
        }
    }
}
