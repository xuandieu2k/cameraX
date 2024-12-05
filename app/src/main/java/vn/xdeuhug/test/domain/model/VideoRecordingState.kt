package vn.xdeuhug.test.domain.model

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
data class VideoRecordingState(
    val isRecording: Boolean,
    val outputPath: String? = null
)