plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shmedo.lib.core"
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
        dataBinding = true
        buildConfig = true
    }

    //ksp传参方式
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    api(project(":core_model"))
    api(project(":core_commonlib"))
    api(project(":lib_network"))

    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //依赖注入框架
    implementation(libs.koin.android)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    //AndroidUtilCode 是一个强大易用的安卓工具类库
    implementation(libs.utilcodex)
    //A logger with a small, extensible API which provides utility on top of Android's normal Log class.
    implementation(libs.timber)

    //高德地图
    api(libs.amap.a3dmap)
    api(libs.amap.search)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}