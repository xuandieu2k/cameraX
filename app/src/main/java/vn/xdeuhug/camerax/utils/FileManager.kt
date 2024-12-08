package vn.xdeuhug.camerax.utils

import android.content.Context
import android.os.Environment
import java.io.File
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 12 / 2024
 */
class FileManager @Inject constructor(
    private val context: Context
) {
    fun createVideoFile(): File {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return File(directory, "video_${System.currentTimeMillis()}.mp4")
    }

    fun createPhotoFile(): File {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(directory, "photo_${System.currentTimeMillis()}.jpg")
    }
}