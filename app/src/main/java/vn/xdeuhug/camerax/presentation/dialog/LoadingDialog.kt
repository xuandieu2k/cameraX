package vn.xdeuhug.camerax.presentation.dialog

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialog
import vn.xdeuhug.camerax.R
import vn.xdeuhug.camerax.databinding.DialogLoadingBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 10 / 2024
 */
class LoadingDialog {
    class Builder(context: Context) : AppCompatDialog(context, R.style.TransparentDialogTheme){
        private var binding: DialogLoadingBinding =
            DialogLoadingBinding.inflate(LayoutInflater.from(context))

        init {
            setContentView(binding.root)
            setCanceledOnTouchOutside(false)
            setCancelable(false)

            window?.setLayout(
                Resources.getSystem().displayMetrics.widthPixels * 9 / 10,
                Resources.getSystem().displayMetrics.widthPixels * 9 / 10
            )

        }
        override fun create() {
            show()
        }

        fun showDialog() {
            show()
        }


    }
}