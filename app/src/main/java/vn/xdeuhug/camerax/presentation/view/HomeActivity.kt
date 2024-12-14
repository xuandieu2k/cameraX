package vn.xdeuhug.camerax.presentation.view

import vn.xdeuhug.camerax.base.AppActivity

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 12 / 2024
 */
import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.databinding.ActivityMainBinding
import vn.xdeuhug.camerax.presentation.service.MediaRecordingService
import vn.xdeuhug.camerax.presentation.viewmodel.HomeViewModel
import vn.xdeuhug.camerax.utils.Resource


@AndroidEntryPoint
class HomeActivity : AppActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun getLayoutView(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setUpView()
    }

    override fun initData() {
        //
    }

    @SuppressLint("SetTextI18n")
    override fun observerData() {

        viewModel.isSaveVideo.observe(this) {
            when (it) {
                is Resource.Error -> {
                    //
                }

                is Resource.Loading -> {
                    //
                }

                is Resource.Success -> {
                    //
                }
            }
        }

        viewModel.isFlashOn.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    it.data?.let { isFlashOn ->
                        //
                    }
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

        viewModel.isRecording.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    it.data?.let { isRecording -> setUpViewRecording(isRecording) }
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

        viewModel.ratioType.observe(this) {
            when (it) {
                is Resource.Error -> {
                    //
                }

                is Resource.Loading -> {
                    //
                }

                is Resource.Success -> {
                    it.data?.let { ratio ->
                        //
                    }
                }
            }
        }

        viewModel.pinchZoom.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    it.data?.let { level ->
//                        binding.tvZoomSize.text = "${"%.1f".format(level)} x"
                    }
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

        viewModel.cameraMode.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    //
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

        viewModel.qualityMode.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    it.data?.let { quality ->
                        //
                    }
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

        viewModel.timerRecord.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    it.data?.let { str ->
                        binding.tvRecordingTimer.text = it.data
                        Timber.tag("${this.viewModel::class.java.name} Timer").e(str)
                    }
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }
    }

    // #### SETUP VIEW ##########
    private fun setUpView() {
        setUpClickView()
        setTouchPreviewCamera()
    }

    private fun setUpClickView() {
        binding.btnStartRecord.setOnClickListener {
            viewModel.onPauseRecordClicked()
        }

        binding.btnSwapCamera.setOnClickListener {
            viewModel.switchCamera()
        }

        binding.btnTakePicture.setOnClickListener {
            viewModel.takeAPicture()
        }
    }

    private fun setUpViewRecording(isRecording: Boolean) {
        binding.tvRecordingTimer.isVisible = isRecording
        binding.btnStartRecord.setImageResource(if (isRecording) R.drawable.ic_recording else R.drawable.ic_record_off)
    }

    private fun setTouchPreviewCamera() {
        val scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    viewModel.pinchZoomCamera(detector)
                    return true
                }
            })

        binding.previewView.setOnTouchListener { view, event ->
            view.performClick()
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    // #### PERMISSIONS FIRST ##########
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.plus(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.plus(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
            permissions.plus(Manifest.permission.POST_NOTIFICATIONS)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, neededPermissions.toTypedArray(), PERMISSION_REQUEST_CODE
            )
        } else {
            bindService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                bindService()
            } else {
                //
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Timber.tag("Message").d(message)
    }

    override fun onResume() {
        super.onResume()
        viewModel.bindPreview(binding.previewView.surfaceProvider)
    }


    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
    }

    private fun bindService() {
        val intent = Intent(this, MediaRecordingService::class.java)
        intent.action = MediaRecordingService.ACTION_START_WITH_PREVIEW
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            viewModel.setRecordingService(
                (service as MediaRecordingService.RecordingServiceBinder).getService(),
                binding.previewView.surfaceProvider
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}