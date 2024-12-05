package vn.xdeuhug.test.domain.repository

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
interface VideoRepository {
    fun startRecording(outputPath: String)
    fun stopRecording()
    fun isRecording(): Boolean
}