package vn.xdeuhug.test.data.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.xdeuhug.test.data.service.VideoRecordingService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */

@Singleton
class VideoServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var serviceBinder: VideoRecordingService.LocalBinder? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            serviceBinder = binder as? VideoRecordingService.LocalBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBinder = null
        }
    }

    fun bindService() {
        val intent = Intent(context, VideoRecordingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        context.unbindService(serviceConnection)
    }

    fun startRecording(outputPath: String) {
        serviceBinder?.getService()?.startRecording(outputPath)
    }

    fun stopRecording() {
        serviceBinder?.getService()?.stopRecording()
    }

    fun isRecording(): Boolean {
        return serviceBinder?.getService()?.isRecording() ?: false
    }
}