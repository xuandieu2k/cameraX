//package vn.xdeuhug.camerax.di
//
//import android.content.Context
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import vn.xdeuhug.camerax.data.connection.VideoServiceConnection
//import javax.inject.Singleton
//
///**
// * @Author: NGUYEN XUAN DIEU
// * @Date: 05 / 12 / 2024
// */
//@Module
//@InstallIn(SingletonComponent::class)
//object ServiceModule {
//
//    @Provides
//    @Singleton
//    fun provideVideoServiceConnection(
//        @ApplicationContext context: Context
//    ): VideoServiceConnection {
//        return VideoServiceConnection(context)
//    }
//}