package vn.xdeuhug.test.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.xdeuhug.test.data.connection.VideoServiceConnection
import vn.xdeuhug.test.data.repository.VideoRepositoryImpl
import vn.xdeuhug.test.domain.repository.VideoRepository
import vn.xdeuhug.test.domain.usecase.VideoRecordingUseCase
import vn.xdeuhug.test.domain.usecase.VideoRecordingUseCaseImpl
import javax.inject.Singleton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2024
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindVideoRepository(
        impl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    abstract fun bindVideoRecordingUseCase(
        impl: VideoRecordingUseCaseImpl
    ): VideoRecordingUseCase
}