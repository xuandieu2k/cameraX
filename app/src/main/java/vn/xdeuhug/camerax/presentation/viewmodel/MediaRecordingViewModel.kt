//package vn.xdeuhug.camerax.presentation.viewmodel
//
//import android.annotation.SuppressLint
//import androidx.camera.core.Preview
//import vn.xdeuhug.camerax.presentation.view.MediaRecordingService
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import java.util.Timer
//import java.util.TimerTask
//
///**
// * @Author: NGUYEN XUAN DIEU
// * @Date: 09 / 12 / 2024
// */
//class MediaRecordingViewModel : ViewModel() {
//
//    private val _recordingState = MutableLiveData<MediaRecordingService.RecordingState>()
//    val recordingState: LiveData<MediaRecordingService.RecordingState> get() = _recordingState
//
//    private val _duration = MutableLiveData<Int>()
//    val duration: LiveData<Int> get() = _duration
//
//    private val _soundEnabled = MutableLiveData<Boolean>()
//    val soundEnabled: LiveData<Boolean> get() = _soundEnabled
//
//    private var recordingService: MediaRecordingService? = null
//    private var timer: Timer? = null
//
//    fun attachService(service: MediaRecordingService) {
//        recordingService = service
//        _soundEnabled.postValue(service.isSoundEnabled())
//        _recordingState.postValue(service.recordingState)
//    }
//
//    fun detachService() {
//        recordingService = null
//    }
//
//    fun initializeCamera(surfaceProvider: Preview.SurfaceProvider?){
//        recordingService?.initializeCamera(surfaceProvider)
//    }
//
//    fun toggleRecording() {
//        if (recordingService?.recordingState == MediaRecordingService.RecordingState.RECORDING) {
//            recordingService?.stopRecording()
//            stopTimer()
//            _recordingState.postValue(MediaRecordingService.RecordingState.STOPPED)
//        } else {
//            recordingService?.startRecording()
//            startTimer()
//            _recordingState.postValue(MediaRecordingService.RecordingState.RECORDING)
//        }
//    }
//
//    fun toggleMute() {
//        recordingService?.let {
//            val enabled = !it.isSoundEnabled()
//            it.setSoundEnabled(enabled)
//            _soundEnabled.postValue(enabled)
//        }
//    }
//
//    private fun startTimer() {
//        timer = Timer()
//        timer?.schedule(object : TimerTask() {
//            private var elapsed = 0
//            override fun run() {
//                elapsed++
//                _duration.postValue(elapsed)
//            }
//        }, 0, 1000)
//    }
//
//    private fun stopTimer() {
//        timer?.cancel()
//        timer = null
//        _duration.postValue(0)
//    }
//}
