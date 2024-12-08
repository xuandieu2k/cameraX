package vn.xdeuhug.camerax.domain.model

import androidx.camera.video.Quality

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 12 / 2024
 */
data class VideoConfig(
    val quality: Quality,  // Chất lượng video (HD, FHD, 4K,...)
    val withAudio: Boolean // Có thu âm hay không
)