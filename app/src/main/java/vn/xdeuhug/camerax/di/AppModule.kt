package vn.xdeuhug.camerax.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.xdeuhug.camerax.data.repository.VideoRecordingRepositoryImpl
import vn.xdeuhug.camerax.data.source.CameraXManager
import vn.xdeuhug.camerax.domain.repository.VideoRecordingRepository
import vn.xdeuhug.camerax.domain.usecase.VideoRecordingUseCase
import javax.inject.Singleton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideCameraXManager(@ApplicationContext context: Context): CameraXManager {
        return CameraXManager(context)
    }

    @Singleton
    @Provides
    fun provideVideoRecordingRepository(cameraManager: CameraXManager): VideoRecordingRepository {
        return VideoRecordingRepositoryImpl(cameraManager)
    }

    @Singleton
    @Provides
    fun provideVideoRecordingUseCase(repository: VideoRecordingRepository): VideoRecordingUseCase {
        return VideoRecordingUseCase(repository)
    }

}