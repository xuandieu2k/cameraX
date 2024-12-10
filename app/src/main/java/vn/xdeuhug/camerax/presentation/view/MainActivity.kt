package vn.xdeuhug.camerax.presentation.view



import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.base.AppActivity
import vn.xdeuhug.camerax.databinding.ActivityMainBinding
import vn.xdeuhug.camerax.presentation.viewmodel.MainViewModel
import vn.xdeuhug.camerax.utils.Resource

@AndroidEntryPoint
class MainActivity : AppActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var isReverseLandscape: Boolean = false

    override fun getLayoutView(): View {
        return binding.root
    }

    override fun initView() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        setUpViewWithOrientation(isLandscape)
        setUpView()
    }

    override fun initData() {
        //
    }

    override fun observerData() {
//        viewModel.isFlashOn.observe(this) {
//            when (it) {
//                is Resource.Loading -> {
//                    // Empty
//                }
//
//                is Resource.Success -> {
//                    it.data?.let { isFlashOn ->
//                        binding.layoutVideoMode.btnFlashMode.setImageResource(if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off)
//                    }
//                }
//
//                is Resource.Error -> {
//                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
//                }
//            }
//        }

        viewModel.isRecording.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    it.data?.let { isRecording -> setUpViewRecording(isRecording = isRecording) }
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

//        viewModel.zoomLevel.observe(this) {
//            when (it) {
//                is Resource.Loading -> {
//                    // Empty
//                }
//
//                is Resource.Success -> {
//                    it.data?.let { level ->
//                        // Update view
////                        when(level){
////                            AppConstants.ZoomMode.X1 -> setZoomLevel(1f)
////                            AppConstants.ZoomMode.X2 -> setZoomLevel(2f)
////                            AppConstants.ZoomMode.X4 -> setZoomLevel(2f)
////                            AppConstants.ZoomMode.X5 -> setZoomLevel(4f)
////                        }
//                    }
//                }
//
//                is Resource.Error -> {
//                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
//                }
//            }
//        }

        viewModel.rotation.observe(this) {
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

        viewModel.ratioType.observe(this) {
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
                    //
                }

                is Resource.Error -> {
                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
                }
            }
        }

//        viewModel.brightMode.observe(this) {
//            when (it) {
//                is Resource.Loading -> {
//                    // Empty
//                }
//
//                is Resource.Success -> {
//                    //
//                }
//
//                is Resource.Error -> {
//                    Timber.tag("${this.viewModel::class.java.name} Error").e(it.message)
//                }
//            }
//        }
    }

    // #### SETUP VIEW ##########
    private fun setUpView() {
        binding.btnRecord.setOnClickListener {
            viewModel.onPauseRecordClicked()
        }

        binding.btnRotate.setOnClickListener {
            viewModel.switchCamera()
        }

//        binding.layoutInformationRecord.btnRotationOrient.setOnClickListener {
//            requestedOrientation = if (isReverseLandscape) {
//                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//            } else {
//                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
//            }
//            isReverseLandscape = !isReverseLandscape
//        }
//
//        binding.layoutVideoMode.btnFlashMode.setOnClickListener {
//            viewModel.toggleFlash()
//        }
//
//        binding.layoutVideoMode.btnZoomMode.setOnClickListener {
//            viewModel.zoomCamera(AppConstants.ZoomMode.X2)
//        }
//
//        binding.layoutInformationRecord.btnSwapCamera.setOnClickListener {
//            viewModel.switchCamera()
//        }
//
//        binding.layoutVideoMode.btnRatioMode.setOnClickListener {
//            viewModel.setAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
//        }
    }

    private fun setUpViewRecording(isRecording: Boolean) {
        binding.btnRecord.setBackgroundResource(if(isRecording) R.drawable.ic_recording else R.drawable.ic_record_off)
    }

    // #### PERMISSIONS FIRST ##########
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.plus(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.plus(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
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
                showToast("Permissions are required to record video.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Timber.tag("Message").d(message)
    }

    override fun onResume() {
        super.onResume()
        viewModel.bindPreview(binding.previewContainer.surfaceProvider)
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
                binding.previewContainer.surfaceProvider
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //
        }
    }

    override fun onStop() {
        super.onStop()
        showToast("Stop!!")
        viewModel.onStop()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setUpViewWithOrientation(isLandscape = true)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setUpViewWithOrientation(isLandscape = false)
        }
    }


    private fun setUpViewWithOrientation(isLandscape: Boolean) {
//        if (isLandscape) {
//            //Landscape
//            binding.layoutBottomVideoRecord.root.apply {
//                visibility = View.VISIBLE //
//                layoutParams = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
//                    ConstraintLayout.LayoutParams.MATCH_PARENT
//                ).apply {
//                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
//                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
//                    rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
//                    setMargins(0, resources.getDimensionPixelSize(R.dimen.dp_24), 0, 0)
//                }
//            }
//
//            binding.layoutInformationRecord.root.apply {
//                visibility = View.VISIBLE
//                layoutParams = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
//                    ConstraintLayout.LayoutParams.MATCH_PARENT
//                ).apply {
//                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
//                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
//                    leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
//                    setMargins(0, resources.getDimensionPixelSize(R.dimen.dp_24), 0, 0)
//                }
//            }
//
//            binding.rlVideoMode.apply {
//                visibility = View.VISIBLE
//                layoutParams = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
//                    setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.dp_16))
//                }
//            }
//        } else {
//            // Portrait
//            binding.layoutBottomVideoRecord.root.apply {
//                visibility = View.VISIBLE
//                layoutParams = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
//                    marginStart = resources.getDimensionPixelSize(R.dimen.dp_16)
//                    marginEnd = resources.getDimensionPixelSize(R.dimen.dp_16)
//                }
//            }
//
//            binding.layoutInformationRecord.root.apply {
//                visibility = View.VISIBLE
//                layoutParams = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
//                    setMargins(0, resources.getDimensionPixelSize(R.dimen.dp_32), 0, 0)
//                }
//            }
//
//            binding.rlVideoMode.apply {
//                visibility = View.VISIBLE
//                layoutParams = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    bottomToTop = R.id.layoutBottomVideoRecord
//                }
//            }
//        }
    }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}