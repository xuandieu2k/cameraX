plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
}

android {
    namespace = "vn.xdeuhug.test"
    compileSdk = 35

    defaultConfig {
        applicationId = "vn.xdeuhug.test"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }

    kapt {
        correctErrorTypes = true
        javacOptions {
            // These options are normally set automatically via the Hilt Gradle plugin, but we
            // set them manually to workaround a bug in the Kotlin 1.5.20
            option("-Adagger.fastInit=ENABLED")
            option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
        }
        arguments {
            arg("mockk.codegen", "true")
        }
    }

}

dependencies {

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Hilt for Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Retrofit
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    // OkHttp - Add log
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
    // Viewmodel
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.activity.ktx)

    // RoomDb
    implementation(libs.room)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    // Paging3
    implementation(libs.paging)
    implementation(libs.room.paging)

    // Flexbox
    implementation(libs.flexbox)

    // Glide
    implementation(libs.glide)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.glide.compiler)

    // Lottie
    implementation(libs.lottie)

    // Immersionbar
    implementation(libs.immersionbar)
    implementation(libs.immersionbar.ktx)
    implementation(libs.immersionbar.components)

    // Timber
    implementation(libs.timber)
    //
    // Permission request framework
    implementation(libs.xxpermissions)

    // Title bar framework
    implementation(libs.titlebar)

    // Toast framework
    implementation(libs.toastutils)

    // Json parsing framework
    implementation(libs.gson)

    // Gson parsing fault tolerance
    implementation(libs.gsonfactory)

    // Shape framework
    implementation(libs.shapeview)

    // AOP plug-in library
    implementation(libs.aspectjrt)

    // Gesture ImageView
    implementation(libs.photoview)

    // Bugly exception catching
    implementation(libs.bugly)
    implementation(libs.nativecrashreport)

    // Pull up to refresh and pull down to load the framework
//    implementation(libs.refreshlayout)
//    implementation(libs.refreshheader)

    // Indicator framework
    implementation(libs.circleindicator)

    // Tencent MMKV
    implementation(libs.mmkv)

    // Memory leak monitoring framework
    debugImplementation(libs.leakcanary)
//    previewImplementation(libs.leakcanary)

    // CameraX
    implementation( libs.androidx.camera.core)
    implementation( libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.video)
    implementation( libs.androidx.camera.view)
    implementation( libs.androidx.camera.extensions)



}