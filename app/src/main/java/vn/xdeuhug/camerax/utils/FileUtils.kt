package vn.xdeuhug.camerax.utils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 12 / 2024
 */
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.video.MediaStoreOutputOptions
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Singleton

@Singleton
object FileUtils {

    const val STORAGE_VIDEO = "DCIM/Recorded Videos"
    const val STORAGE_IMAGE = "Pictures/CameraX-Image"

    const val CAMERA_HEADER_NAME = "CameraX-takeAPicture-"
    const val VIDEO_HEADER_NAME = "CameraX-recording-"
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    fun createMediaStoreOutputOptions(
        fileName: String = VIDEO_HEADER_NAME, context: Context
    ): MediaStoreOutputOptions {
        val name = fileName + SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(
            System.currentTimeMillis()
        ) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH, STORAGE_VIDEO
                )
            }
        }
        return MediaStoreOutputOptions.Builder(
            context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
    }

    fun createPictureStoreOutput(
        fileName: String = VIDEO_HEADER_NAME, context: Context
    ): ImageCapture.OutputFileOptions {
        val name = fileName + "" + SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, STORAGE_IMAGE)
            }
        }
        return ImageCapture.OutputFileOptions.Builder(
            context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()
    }
}