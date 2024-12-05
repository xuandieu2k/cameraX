package vn.xdeuhug.test.domain.usecase

import vn.xdeuhug.test.domain.repository.VideoRepository
import javax.inject.Inject

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
class VideoRecordingUseCaseImpl @Inject constructor(
    private val repository: VideoRepository
) : VideoRecordingUseCase {
    override fun startRecording(outputPath: String) {
        repository.startRecording(outputPath)
    }

    override fun stopRecording() {
        repository.stopRecording()
    }

    override fun isRecording(): Boolean {
        return repository.isRecording()
    }
}