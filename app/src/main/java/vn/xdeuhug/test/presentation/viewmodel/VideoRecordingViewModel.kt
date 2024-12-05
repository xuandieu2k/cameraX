package vn.xdeuhug.test.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.xdeuhug.test.domain.usecase.VideoRecordingUseCase
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@HiltViewModel
class VideoRecordingViewModel @Inject constructor(
    private val useCase: VideoRecordingUseCase
) : ViewModel() {

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    fun startRecording(outputPath: String) {
        useCase.startRecording(outputPath)
        _isRecording.postValue(true)
    }

    fun stopRecording() {
        useCase.stopRecording()
        _isRecording.postValue(false)
    }
}