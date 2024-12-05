package vn.xdeuhug.test.data.repository

import vn.xdeuhug.test.data.connection.VideoServiceConnection
import vn.xdeuhug.test.domain.repository.VideoRepository
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRepositoryImpl @Inject constructor(
    private val videoServiceConnection: VideoServiceConnection
) : VideoRepository {
    override fun startRecording(outputPath: String) {
        videoServiceConnection.startRecording(outputPath)
    }

    override fun stopRecording() {
        videoServiceConnection.stopRecording()
    }

    override fun isRecording(): Boolean {
        return videoServiceConnection.isRecording()
    }
}