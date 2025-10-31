import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.shmedo.lib.rtsp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 把库自己的 consumer 规则暴露出去
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("libs")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    // FFmpeg 解码器 - 本地 JAR 提供渲染器类，SO 库已放置在 jniLibs
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // ExoPlayer (Media3) - RTSP 视频流播放
    implementation(libs.androidx.media3.exoplayer)
    api(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.rtsp)
    
    // Kotlin Coroutines - 异步处理
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // 依赖注入框架
    implementation(libs.koin.android)
    
    // AndroidUtilCode 是一个强大易用的安卓工具类库
    implementation(libs.utilcodex) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    
    // A logger with a small, extensible API which provides utility on top of Android's normal Log class.
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
