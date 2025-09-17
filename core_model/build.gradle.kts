plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shmedo.core.model"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //把库自己的 consumer 规则暴露出去
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
    buildTypes {
        getByName("release") {
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    // json parsing
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    //AndroidUtilCode 是一个强大易用的安卓工具类库
    implementation(libs.utilcodex)

}