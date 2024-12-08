package vn.xdeuhug.camerax.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.xdeuhug.camerax.data.repository.VideoRecordingRepositoryImpl
import vn.xdeuhug.camerax.data.source.CameraManager
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import vn.xdeuhug.camerax.domain.usecase.VideoRecordingUseCase

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideCameraManager(@ApplicationContext context: Context): CameraManager {
        return CameraManager(context)
    }

    @Provides
    fun provideVideoRecordingRepository(cameraManager: CameraManager): VideoRecordingRepository {
        return VideoRecordingRepositoryImpl(cameraManager)
    }

    @Provides
    fun provideVideoRecordingUseCase(repository: VideoRecordingRepository): VideoRecordingUseCase {
        return VideoRecordingUseCase(repository)
    }

}