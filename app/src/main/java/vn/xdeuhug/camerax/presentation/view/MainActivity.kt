package vn.xdeuhug.camerax.presentation.view


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import vn.xdeuhug.camerax.base.AppActivity
import vn.xdeuhug.camerax.databinding.ActivityMainBinding
import vn.xdeuhug.camerax.presentation.viewmodel.MainViewModel
import vn.xdeuhug.camerax.utils.Resource

@AndroidEntryPoint
class MainActivity : AppActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun getLayoutView(): View {
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

    override fun observerData() {
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

        viewModel.timeRecord.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Empty
                }

                is Resource.Success -> {
                    binding.tvRecordingTimer.text = it.data
                }

                is Resource.Error -> {
                    //
                }
            }
        }
    }

    // #### SETUP VIEW ##########
    private fun setUpView() {
        binding.btnStartRecord.setOnClickListener {
            viewModel.onPauseRecordClicked()
        }

        binding.btnSwapCamera.setOnClickListener {
            viewModel.switchCamera(this)
        }

        binding.btnTakePicture.setOnClickListener {
            viewModel.takeAPhoto()
        }
    }

    private fun setUpViewRecording(isRecording: Boolean) {
        binding.tvRecordingTimer.isVisible = isRecording
        binding.btnStartRecord.setImageResource(if (isRecording) R.drawable.ic_recording else R.drawable.ic_record_off)
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
            viewModel.initializeCamera(binding.previewView, this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                viewModel.initializeCamera(binding.previewView, this)
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
        viewModel.bindPreview(binding.previewView, this)
    }


    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
    }

//    private fun bindService() {
//        val intent = Intent(this, MediaRecordingService::class.java)
//        intent.action = MediaRecordingService.ACTION_START_WITH_PREVIEW
//        startService(intent)
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//    }
//
//    private val serviceConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            viewModel.setRecordingService(
//                (service as MediaRecordingService.RecordingServiceBinder).getService(),
//                binding.previewView.surfaceProvider
//            )
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            //
//        }
//    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}