package vn.xdeuhug.camerax.base

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.presentation.dialog.LoadingDialog

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 22 / 10 / 2024
 */
abstract class AppActivity : AppCompatActivity() {
    private var dialog: LoadingDialog.Builder? = null
    private var dialogCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog = LoadingDialog.Builder(this)
        updateTheme(this.resources.configuration)
        initActivity()
    }

    protected open fun initActivity() {
        initLayout()
        initView()
        initData()
        observerData()
    }

    protected abstract fun getLayoutView(): View

    protected abstract fun initView()

    protected abstract fun initData()

    protected abstract fun observerData()

    protected open fun initLayout() {
        setContentView(getLayoutView())
    }

    private fun setStatusBar(window: Window, backgroundColor: Int, isLightStatusBar: Boolean) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        @Suppress("DEPRECATION")
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, backgroundColor)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isLightStatusBar) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                0
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateTheme(newConfig)
    }

    private fun updateTheme(config: Configuration) {
        when (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                setStatusBar(window, R.color.black, false)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                setStatusBar(window, R.color.white, true)
            }

            else -> {
                setStatusBar(window, R.color.white, false)
            }
        }
    }


    open fun isShowDialog(): Boolean {
        return dialog != null && dialog!!.isShowing
    }

    open fun showDialog() {
        dialog?.showDialog()
    }

    open fun hideDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (dialogCount > 0) {
            dialogCount--
        }
        if ((dialogCount != 0) || (dialog == null) || !dialog!!.isShowing) {
            return
        }
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isShowDialog()) {
            hideDialog()
        }
        dialog = null

    }


}