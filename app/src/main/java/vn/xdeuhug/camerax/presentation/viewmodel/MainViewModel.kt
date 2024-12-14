package vn.xdeuhug.camerax.presentation.viewmodel

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 12 / 2024
 */
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.Quality
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
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
import vn.xdeuhug.camerax.domain.usecase.VideoRecordingUseCase
import vn.xdeuhug.camerax.utils.Resource
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoRecordingUseCase: VideoRecordingUseCase
) : ViewModel(), CameraXManager.CameraXListener {
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

    private val _rotation: MutableLiveData<Resource<Int>> = MutableLiveData(Resource.Success(0))
    val rotation: LiveData<Resource<Int>> = _rotation

    private val _timeRecord: MutableLiveData<Resource<String>> =
        MutableLiveData(Resource.Success("00:00:00"))
    val timeRecord: LiveData<Resource<String>> = _timeRecord

    private val _initCamera: MutableLiveData<Resource<Boolean>> =
        MutableLiveData(Resource.Success(false))
    val initCamera: LiveData<Resource<Boolean>> = _initCamera

    private val _statusRecord: MutableLiveData<Resource<CameraXManager.RecordingState>> =
        MutableLiveData(Resource.Success(CameraXManager.RecordingState.STOPPED))
    val statusRecord: LiveData<Resource<CameraXManager.RecordingState>> = _statusRecord


//    private val _zoomLevel: MutableLiveData<Resource<AppConstants.ZoomMode>> =
//        MutableLiveData(Resource.Success(AppConstants.ZoomMode.X1))
//    val zoomLevel: LiveData<Resource<AppConstants.ZoomMode>> = _zoomLevel
//
//    private val _brightMode: MutableLiveData<Resource<AppConstants.BrightMode>> =
//        MutableLiveData(Resource.Success(AppConstants.BrightMode.BRIGHT))
//    val brightMode: LiveData<Resource<AppConstants.BrightMode>> = _brightMode

    private val _isRecording: MutableLiveData<Resource<Boolean>> =
        MutableLiveData(Resource.Success(false))
    val isRecording: LiveData<Resource<Boolean>> = _isRecording

    fun initializeCamera(preview: PreviewView, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            _initCamera.value = videoRecordingUseCase.initializeCamera(lifecycleOwner, preview)
            videoRecordingUseCase.addListenerCameraX(this@MainViewModel)
        }
    }

    fun switchCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            if (_cameraMode.value?.data == CameraSelector.DEFAULT_BACK_CAMERA) {
                videoRecordingUseCase.switchCamera(lifecycleOwner)
                _cameraMode.value = Resource.Success(CameraSelector.DEFAULT_FRONT_CAMERA)
                return@launch
            }
            videoRecordingUseCase.switchCamera(lifecycleOwner)
            _cameraMode.value = Resource.Success(CameraSelector.DEFAULT_BACK_CAMERA)
        }
    }


    fun onPauseRecordClicked() {
        viewModelScope.launch {
            when (_statusRecord.value?.data) {
                CameraXManager.RecordingState.RECORDING -> {
                    videoRecordingUseCase.stopRecording()
                    _isRecording.value = Resource.Success(false)
                    _timeRecord.value = Resource.Success("00:00:00")
                }

                CameraXManager.RecordingState.STOPPED -> {
                    videoRecordingUseCase.startRecording()
                    _isRecording.value = Resource.Success(true)
                }

                else -> {
                    // no-op
                }
            }
        }
    }

    fun takeAPhoto() {
        viewModelScope.launch {
            videoRecordingUseCase.takeAPicture(object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    //
                }

                override fun onError(exception: ImageCaptureException) {
                    //
                }

            })
        }

    }

    fun onStop() {
        viewModelScope.launch {
            videoRecordingUseCase.destroyCamera()
            videoRecordingUseCase.removeListenerCameraX(this@MainViewModel)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                _timeRecord.value = Resource.Success("$hoursString:$minutesString:$secondsString")
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
            }
        }
    }

    override fun onPinchZoomCamera(scale: Float) {
        //
    }

    override fun onRecordingStateChanged(recordingState: CameraXManager.RecordingState) {
        _statusRecord.value = Resource.Success(recordingState)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            videoRecordingUseCase.removeListenerCameraX(this@MainViewModel)
        }
    }

    fun bindPreview(preview: PreviewView, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            videoRecordingUseCase.bindPreview(preview, lifecycleOwner)
        }
    }

    companion object {
        private const val MINUTE: Int = 60
        private const val HOUR: Int = MINUTE * 60
    }
}