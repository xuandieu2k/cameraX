package vn.xdeuhug.camerax.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.base.AppActivity
import vn.xdeuhug.camerax.databinding.ActivityVideoRecordingBinding
import vn.xdeuhug.camerax.presentation.viewmodel.VideoRecordingViewModel

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@AndroidEntryPoint
class VideoRecordingActivity : AppActivity() {

    private val binding: ActivityVideoRecordingBinding by lazy {
        ActivityVideoRecordingBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel: VideoRecordingViewModel by viewModels()


    override fun getLayoutView(): View {
        return binding.root
    }

    override fun initView() {
        setupUI()
    }

    override fun initData() {
        checkAndRequestPermissions()
    }

    override fun observerData() {
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnStartRecord.setOnClickListener {
            if (viewModel.isRecording.value == true) {
                Timber.tag("Stop recording").d("Create")
                viewModel.stopRecording()
            } else {
                val outputFile =
                    "${getExternalFilesDir(Environment.DIRECTORY_MOVIES)}/video_${System.currentTimeMillis()}.mp4"
                Timber.tag("Start recording").d("Create")
                viewModel.startRecording(this, outputFile)
            }
        }

//        binding.btnSwitchCamera.setOnClickListener {
//            viewModel.switchCamera()
//        }

//        binding.btnQuality.setOnClickListener {
//            viewModel.loadAvailableQualities()
//            viewModel.qualities.observe(this) { qualities ->
//                // Show quality selector dialog
//            }
//        }
    }

    private fun observeViewModel() {
        viewModel.isRecording.observe(this) { isRecording ->
            showToast(isRecording.toString())
            binding.btnStartRecord.setImageResource(
                if (!isRecording) R.drawable.ic_record_off else R.drawable.ic_recording
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.plus(Manifest.permission.READ_MEDIA_VIDEO)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                neededPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            viewModel.initializeCamera(this, binding.previewView)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                viewModel.initializeCamera(this, binding.previewView)
            } else {
                showToast("Permissions are required to record video.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Timber.tag("Message").d(message)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

}



