package vn.xdeuhug.test.presentation.view

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import vn.xdeuhug.test.databinding.ActivityVideoRecordingBinding
import vn.xdeuhug.test.presentation.viewmodel.VideoRecordingViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@AndroidEntryPoint
class VideoRecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoRecordingBinding
    private val viewModel: VideoRecordingViewModel by viewModels()

    private lateinit var cameraExecutor: ExecutorService
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null // Instance để quản lý video đang quay
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private var recordingTime = 0 // Thời gian quay (giây)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupCamera()
        setupListeners()
        setupObservers()
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder()
                .build()

            // Set the preview stream to the PreviewView
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.FHD)) // Chọn chất lượng Full HD
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()

                // Bind the use cases to the camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera initialization failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupListeners() {
        binding.startButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
            }
        }

        binding.stopButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }

        binding.flashToggle.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Flash On" else "Flash Off", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecording() {
        if (!hasPermissions()) {
            requestPermissions()
            return
        }

        val file = File(getExternalFilesDir(null), "video_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(file).build()

        // Bắt đầu ghi video
        activeRecording = videoCapture?.output?.prepareRecording(this, outputOptions)
            ?.apply {
                if(ContextCompat.checkSelfPermission(
                        this@VideoRecordingActivity, android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED){
                    withAudioEnabled()
                }
            }
            ?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        updateRecordingUI()
                        startTimer()
                    }
                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        updateRecordingUI()
                        stopTimer()
                        if (recordEvent.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                            Toast.makeText(this, "Video saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Recording failed: ${recordEvent.error}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        activeRecording?.stop() // Kết thúc ghi video
        activeRecording = null
    }

    private fun updateRecordingUI() {
        binding.startButton.isEnabled = !isRecording
        binding.stopButton.isEnabled = isRecording
    }

    private fun startTimer() {
        recordingTime = 0
        handler.post(object : Runnable {
            override fun run() {
                recordingTime++
                val minutes = recordingTime / 60
                val seconds = recordingTime % 60
                binding.recordingTimer.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun stopTimer() {
        handler.removeCallbacksAndMessages(null)
        binding.recordingTimer.text = "00:00"
    }

    private fun setupObservers() {
        viewModel.isRecording.observe(this) { isRecording ->
            updateRecordingUI()
        }
    }

    private fun hasPermissions(): Boolean {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Quyền đã được cấp
                startRecording()
            } else {
                Toast.makeText(this, "Permissions are required to record video.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}