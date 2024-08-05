plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shmedo.lib.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":lib_core"))
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.rxhttp)
    ksp(libs.rxhttp.compiler)
    implementation(libs.rxhttp.converter.moshi)

    //一个轻量级、高可用性的Android版本更新框架
    api(libs.xuexiangjys.xupdate)

    //A logger with a small, extensible API which provides utility on top of Android's normal Log class.
    implementation(libs.timber)
    implementation(libs.utilcodex)
    implementation(libs.mmkv)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}