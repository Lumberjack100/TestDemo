plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shmedo.core.commonlib"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //把库自己的 consumer 规则暴露出去
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(project(":core_model"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.startup.runtime)
    implementation (libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kunminx.unpeek.livedata)

    // json parsing
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.mmkv)
    //AndroidUtilCode 是一个强大易用的安卓工具类库
    implementation(libs.utilcodex)
    //A logger with a small, extensible API which provides utility on top of Android's normal Log class.
    implementation(libs.timber)

}