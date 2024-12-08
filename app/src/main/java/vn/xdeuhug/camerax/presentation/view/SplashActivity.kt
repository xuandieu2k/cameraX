package vn.xdeuhug.camerax.presentation.view

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import vn.xdeuhug.camerax.base.AppActivity
import vn.xdeuhug.camerax.databinding.ActivitySplashBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 10 / 2024
 */
class SplashActivity : AppActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun getLayoutView(): View {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, VideoRecordingActivity::class.java))
            finish()
        }, 2000)
    }

    override fun initData() {
        //
    }

    override fun observerData() {
        //
    }
}