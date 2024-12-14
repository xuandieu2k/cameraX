package vn.xdeuhug.camerax.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.view.ScaleGestureDetector
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.video.Quality
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.camerax.data.source.CameraXManager
import vn.xdeuhug.camerax.presentation.service.MediaRecordingService
import vn.xdeuhug.camerax.utils.Resource
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel(), CameraXManager.CameraXListener {
    private val _isFlashOn: MutableLiveData<Resource<Boolean>> =
        MutableLiveData(Resource.Success(false))
    val isFlashOn: LiveData<Resource<Boolean>> = _isFlashOn

    private val _ratioType: MutableLiveData<Resource<Int>> =
        MutableLiveData(Resource.Success(androidx.camera.core.AspectRatio.RATIO_DEFAULT))
    val ratioType: LiveData<Resource<Int>> = _ratioType

    private val _cameraMode: MutableLiveData<Resource<CameraSelector>> =
        MutableLiveData(Resource.Success(CameraSelector.DEFAULT_BACK_CAMERA))
    val cameraMode: LiveData<Resource<CameraSelector>> = _cameraMode

    private val _qualityMode: MutableLiveData<Resource<Quality>> =
        MutableLiveData(Resource.Success(Quality.SD))
    val qualityMode: LiveData<Resource<Quality>> = _qualityMode

    private val _pinchZoom: MutableLiveData<Resource<Float>> =
        MutableLiveData(Resource.Success(1f))
    val pinchZoom: LiveData<Resource<Float>> = _pinchZoom

    private val _isRecording: MutableLiveData<Resource<Boolean>> =
        MutableLiveData(Resource.Success(false))
    val isRecording: LiveData<Resource<Boolean>> = _isRecording

    private val _isSaveVideo: MutableLiveData<Resource<Pair<androidx.camera.video.OutputResults?, Boolean>>> =
        MutableLiveData(Resource.Success(Pair(null, false)))
    val isSaveVideo: LiveData<Resource<Pair<androidx.camera.video.OutputResults?, Boolean>>> =
        _isSaveVideo

    private val _timerRecord: MutableLiveData<Resource<String>> =
        MutableLiveData(Resource.Success(TIMER_DEFAULT))
    val timerRecord: LiveData<Resource<String>> = _timerRecord

    @SuppressLint("StaticFieldLeak")
    private var recordingService: MediaRecordingService? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null

    fun isRecording(): Boolean {
        return _isRecording.value?.data ?: false
    }

    fun resetSaveVideo() {
        _isSaveVideo.value = Resource.Success(Pair(null, false))
    }

    fun setRecordingService(
        service: MediaRecordingService, surfaceProvider: Preview.SurfaceProvider?
    ) {
        recordingService = service
        this.surfaceProvider = surfaceProvider
        onServiceBound(recordingService)
    }

    fun takeAPicture() {
        recordingService?.takeAPicture(object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                showToast("Saved Image")
            }

            override fun onError(exception: ImageCaptureException) {
                showToast("Error Take A Shot: $exception")
            }

        })
    }

    fun toggleFlash() {
        recordingService?.let {
            val isEnable = _isFlashOn.value?.data ?: false
            it.toggleFlash(!isEnable)//
            _isFlashOn.value = Resource.Success(!isEnable)
        }
    }

    fun pinchZoomCamera(detector: ScaleGestureDetector) {
        recordingService?.pinchZoom(detector)
    }


    fun switchCamera() {
        recordingService?.let {
            if (_cameraMode.value?.data == CameraSelector.DEFAULT_BACK_CAMERA) {
                it.switchCamera()
                _cameraMode.value = Resource.Success(CameraSelector.DEFAULT_FRONT_CAMERA)
                return
            }
            it.switchCamera()
            _cameraMode.value = Resource.Success(CameraSelector.DEFAULT_BACK_CAMERA)
        }
    }

    fun setAspectRatio(ratio: Int) {
        if (recordingService == null) return
        recordingService?.setAspectRatio(ratio)
    }

    fun changeQualityVideo(quality: Quality) {
        _qualityMode.value = Resource.Success(quality)
    }

    private fun onServiceBound(
        recordingService: MediaRecordingService?
    ) {
        when (recordingService?.getRecordingState()) {
            CameraXManager.RecordingState.RECORDING -> {
                _isRecording.value = Resource.Success(true)
            }

            CameraXManager.RecordingState.STOPPED -> {
                _isRecording.value = Resource.Success(false)
            }

            else -> {
                // no-op
            }
        }

        recordingService?.addListener(this)
        recordingService?.bindPreviewUseCase(surfaceProvider)
    }


    fun onPauseRecordClicked() {
        when (recordingService?.getRecordingState()) {
            CameraXManager.RecordingState.RECORDING -> {
                _isRecording.value = Resource.Success(false)
                recordingService?.stopRecording()
//                binding.txtDuration.text = "00:00:00"
            }

            CameraXManager.RecordingState.STOPPED -> {
                _isRecording.value = Resource.Success(true)
                recordingService?.startRecording()
            }

            else -> {
                // no-op
            }
        }
    }

    fun onStart() {
        //
    }

    fun onStop() {
        if (recordingService?.getRecordingState() == CameraXManager.RecordingState.STOPPED) {
            recordingService?.let {
                ServiceCompat.stopForeground(it, ServiceCompat.STOP_FOREGROUND_REMOVE)
                recordingService?.stopSelf()
            }
        } else {
            recordingService?.startRunningInForeground()
        }
        recordingService?.unbindPreview()
        recordingService?.removeListener(this)
    }

    private fun showToast(message: String) {
        Timber.tag("Message").d(message)
    }

    @SuppressLint("SetTextI18n")
    override fun onNewData(duration: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                var seconds = duration
                var minutes = seconds / MINUTE
                seconds %= MINUTE
                val hours = minutes / HOUR
                minutes %= HOUR

                val hoursString = if (hours >= 10) hours.toString() else "0$hours"
                val minutesString = if (minutes >= 10) minutes.toString() else "0$minutes"
                val secondsString = if (seconds >= 10) seconds.toString() else "0$seconds"
                _timerRecord.value = Resource.Success("$hoursString:$minutesString:$secondsString")
                Timber.tag("Log time").d("$hoursString:$minutesString:$secondsString")
            }
        }
    }

    override fun onCameraOpened() {
        //
    }

    override fun onRecordingEvent(it: VideoRecordEvent?) {
        when (it) {
            is VideoRecordEvent.Start -> {
                //
            }

            is VideoRecordEvent.Finalize -> {
                _isRecording.value = Resource.Success(false)
                onNewData(0)
                _timerRecord.value = Resource.Success(TIMER_DEFAULT)
                _isSaveVideo.value = Resource.Success(Pair(it.outputResults, true))
            }
        }
    }

    override fun onPinchZoomCamera(zoomSize: Float) {
        _pinchZoom.value = Resource.Success(zoomSize)
    }

    override fun onRecordingStateChanged(recordingState: CameraXManager.RecordingState) {
        //
    }

    override fun onCleared() {
        super.onCleared()
        recordingService?.removeListener(this)
        recordingService = null
        surfaceProvider = null
    }

    fun bindPreview(surfaceProvider: Preview.SurfaceProvider) {
        recordingService?.bindPreview(surfaceProvider)
        this.surfaceProvider = surfaceProvider
        recordingService?.removeListener(this)
        recordingService?.addListener(this)
    }

    companion object {
        private const val MINUTE: Int = 60
        private const val HOUR: Int = MINUTE * 60
        private const val TIMER_DEFAULT = "00:00:00"
    }
}