package vn.xdeuhug.test.domain.usecase

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
interface VideoRecordingUseCase {
    fun startRecording(outputPath: String)
    fun stopRecording()
    fun isRecording(): Boolean
}