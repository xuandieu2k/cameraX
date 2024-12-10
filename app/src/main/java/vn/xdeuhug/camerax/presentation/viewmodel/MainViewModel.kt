package vn.xdeuhug.camerax.presentation.viewmodel

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 12 / 2024
 */
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.camera.core.CameraSelector
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
import vn.xdeuhug.camerax.presentation.view.MediaRecordingService
import vn.xdeuhug.camerax.utils.Resource
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel(), MediaRecordingService.DataListener {
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


    private var recordingService: MediaRecordingService? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var isReverseLandscape: Boolean = false

    fun setRecordingService(
        service: MediaRecordingService, surfaceProvider: Preview.SurfaceProvider?
    ) {
        recordingService = service
        this.surfaceProvider = surfaceProvider
        onServiceBound(recordingService)
    }

    fun toggleFlash() {
        if (recordingService == null) return
        val isEnable = _isFlashOn.value?.data ?: false
        recordingService?.toggleFlash(!isEnable)//
        _isFlashOn.value = Resource.Success(!isEnable)
    }

//    fun zoomCamera(zoomLevel: AppConstants.ZoomMode) {
//        if (recordingService == null) return
//        recordingService?.setZoomLevel(getValueZoomLevel(zoomLevel))
//        _zoomLevel.value = Resource.Success(zoomLevel)
//    }


    fun switchCamera() {
        if (recordingService == null) return
        if (_cameraMode.value?.data == CameraSelector.DEFAULT_BACK_CAMERA) {
            recordingService?.switchCamera()
            _cameraMode.value = Resource.Success(CameraSelector.DEFAULT_FRONT_CAMERA)
            return
        }
        recordingService?.switchCamera()
        _cameraMode.value = Resource.Success(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    fun setAspectRatio(ratio: Int) {
        if (recordingService == null) return
        recordingService?.setAspectRatio(ratio)
    }

//    private fun getValueZoomLevel(zoomLevel: AppConstants.ZoomMode): Float {
//        return when (zoomLevel) {
//            AppConstants.ZoomMode.X1 -> 1f
//            AppConstants.ZoomMode.X2 -> 2f
//            AppConstants.ZoomMode.X4 -> 3f
//            AppConstants.ZoomMode.X5 -> 4f
//        }
//    }

    private fun onServiceBound(
        recordingService: MediaRecordingService?
    ) {
        when (recordingService?.getRecordingState()) {
            MediaRecordingService.RecordingState.RECORDING -> {
                _isRecording.value = Resource.Success(true)
            }

            MediaRecordingService.RecordingState.STOPPED -> {
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
            MediaRecordingService.RecordingState.RECORDING -> {
                _isRecording.value = Resource.Success(false)
                recordingService?.stopRecording()
//                binding.txtDuration.text = "00:00:00"
            }

            MediaRecordingService.RecordingState.STOPPED -> {
                _isRecording.value = Resource.Success(true)
                recordingService?.startRecording()
            }

            else -> {
                // no-op
            }
        }
    }

    fun onStart() {

    }

    fun onStop() {
        showToast("Stop!!")
        if (recordingService?.getRecordingState() == MediaRecordingService.RecordingState.STOPPED) {
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
//                binding.txtDuration.text = "$hoursString:$minutesString:$secondsString"
            }
        }
    }

    override fun onCameraOpened() {

    }

    override fun onRecordingEvent(it: VideoRecordEvent?) {
        when (it) {
            is VideoRecordEvent.Start -> {
//                binding.btnMute.visibility = View.INVISIBLE
//                binding.viewRecordPause.setBackgroundResource(R.drawable.ic_crown)
            }

            is VideoRecordEvent.Finalize -> {
//                recordingService?.isSoundEnabled()?.let { it1 -> setSoundState(it1) }
//                binding.btnMute.visibility = View.VISIBLE
//                binding.viewRecordPause.setBackgroundResource(R.drawable.ic_recording)
//                setUpViewRecording(isRecording = false)
                _isRecording.value = Resource.Success(false)
                onNewData(0)
                val intent = Intent(Intent.ACTION_VIEW, it.outputResults.outputUri)
                intent.setDataAndType(it.outputResults.outputUri, "video/mp4")
                context.startActivity(Intent.createChooser(intent, "Open recorded video"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingService = null
        surfaceProvider = null
    }

    fun bindPreview(surfaceProvider: Preview.SurfaceProvider) {
        recordingService?.bindPreview(surfaceProvider)
    }

    companion object {
        private const val MINUTE: Int = 60
        private const val HOUR: Int = MINUTE * 60
    }
}