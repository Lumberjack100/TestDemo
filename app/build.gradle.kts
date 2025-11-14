
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
}

/** ===== 读取 version.properties（持久化 VERSION_NAME versionCode 等）===== */
val versionProps = Properties().apply {
    val f = file("version.properties")
    require(f.exists()) {
        "version.properties 不存在，请在 app/ 目录下创建，并包含 VERSION_CODE=xxxx 等"
    }
    f.inputStream().use(::load)
}
// 通用 versionCode
val versionCodeFromFile = versionProps["VERSION_CODE"].toString().toInt()

// 提取正式版本号
val majorVersion = versionProps["MAJOR_VERSION"].toString().toInt()
val minorVersion = versionProps["MINOR_VERSION"].toString().toInt()
val patchVersion = versionProps["PATCH_VERSION"].toString().toInt()

// 提取测试版本号
val testMajorVersion = versionProps["TEST_MAJOR_VERSION"].toString().toInt()
val testMinorVersion = versionProps["TEST_MINOR_VERSION"].toString().toInt()
val testPatchVersion = versionProps["TEST_PATCH_VERSION"].toString().toInt()


/** ===== 读取 keystore.properties（签名配置）===== */
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    FileInputStream(keystorePropertiesFile).use(::load)
}


android {
    namespace = "com.shmedo.mcloudapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.shmedo.mcloudapp.iot"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            //设置支持的SO库架构（开发者可以根据需要，选择一个或多个平台的so）
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
        }
        manifestPlaceholders["buglyAppId"] = "09b15cd6a7"
        buildConfigField("String", "AMS_APP_KEY", "\"b80dd379-5256-48c8-947a-2208872c8a8f\"")
        buildConfigField("String", "AMS_APP_SECRET", "\"3dc8e0ec1f673325c6694b4da534dabe\"")
    }
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    buildTypes {
        getByName("release") {
            // 1. 启用代码混淆
            isMinifyEnabled = true // R8 会在构建 release 版本时自动执行压缩（移除无用代码）、优化（字节码级别）和混淆（重命名）。
            // 2. 启用资源压缩（可选，但建议与代码压缩一起使用）
            isShrinkResources = true
            // 3. 指定混淆规则文件
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{INDEX.LIST,io.netty.versions.properties}"
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("libs")
        }
        configureEach {
            kotlin.srcDir(layout.buildDirectory.files("generated/ksp/$name/kotlin/"))
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("production") {//正式发布版本
            dimension = "version"
            versionCode = versionCodeFromFile
            versionName = "$majorVersion.$minorVersion.$patchVersion"

            // 名称/常量/占位
            resValue("string", "app_name", "米易通")   // 设置默认的app_name
            buildConfigField("String", "APP_NAME", "\"米易通\"")
            buildConfigField("String", "PGY_API_KEY", "\"64454bf76fe2abd8dec45200c11fc93b\"")
            buildConfigField("String", "PGY_APP_KEY", "\"b8a852c106c6cc532332081e22f218a9\"")
            manifestPlaceholders["mapApikey"] = "QVihTHJNsLL5k9tBLwHFdLGR5TR0Lrlj"
        }
        create("demo") {//测试版本
            dimension = "version"
            applicationIdSuffix = ".v5"

            versionCode = versionCodeFromFile
            versionName = "$testMajorVersion.$testMinorVersion.$testPatchVersion"

            resValue("string", "app_name", "米易通V5")   // 设置默认的app_name
            buildConfigField("String", "APP_NAME", "\"米易通V5\"")
            buildConfigField("String", "PGY_API_KEY", "\"db9ce8a6bd3b8b95c20c66e4205194d9\"")
            buildConfigField("String", "PGY_APP_KEY", "\"a8508805883003fdd3e223b6f9e85a60\"")
            manifestPlaceholders["mapApikey"] = "kOEHOjIEoj8JHieMUqF3qMINFqkmxOoW"
        }

        applicationVariants.all {
            val flavor = flavorName ?: "noflavor"
            val buildTypeName = buildType.name
            outputs.all {
                val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                val appName = if (flavor.contains("demo", ignoreCase = true)) "米易通V5" else "米易通"

                // 设置输出文件名
                outputImpl.outputFileName =
                    "${appName}_${versionName}_${versionCode}_${flavor}_${buildTypeName}.apk"
            }
        }

    }
}

dependencies {
    implementation(project(":core_data"))
    implementation(project(":lib_cmd"))
    implementation(project(":lib_ble"))
    implementation(project(":lib_tcp"))
    implementation(project(":lib_rtsp"))
    implementation(project(":lib_wifi"))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.flexbox)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    //通过 exclude 把对官方 java 包的依赖排除了，引用默认指向 smooth-Navigation
    implementation(libs.androidx.navigation.fragment.ktx) {
        exclude(group = "androidx.navigation", module = "navigation-fragment")
    }

    implementation(libs.kunminx.unpeek.livedata)
    implementation(libs.kunminx.strict.databinding)
    implementation(libs.kunminx.smooth.navigation)

    //透明系统栏设置基础依赖包，必须要依赖
//    implementation(libs.immersionbar)
//    implementation(libs.immersionbar.ktx)

    //权限请求框架：https://github.com/getActivity/XXPermissions
    implementation(libs.getActivity.xxpermission)
    //Material Dialog
    implementation(libs.bundles.material.dialogs)
    //Powerful and Beautiful Popup for Android，can absolutely replace Dialog，PopupWindow，PopupMenu，BottomSheet，DrawerLayout，Spinner...
    implementation(libs.xpopup) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    implementation(libs.dialogx)
    //Toast 吐司
    implementation(libs.toastutils)
    implementation(libs.datetime.picker) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }

    //Android 快速构建 RecyclerView, 比 BRVAH 更简单强大 https://github.com/liangjingkanji/BRV
    implementation(libs.liangjingkanji.brv)
    //A ListView-like FastScroller for Android's RecyclerView.
    implementation(libs.fastscroll)
    implementation(libs.tableview)

    // 添加WebViewAssetLoader依赖
    implementation(libs.androidx.webkit)
    implementation(libs.agentweb.core)

    //开关 Button
    implementation(libs.switchbutton.library)
    //一款美观强大的支持单向、双向范围选择、分步、垂直、高度自定义的SeekBar
    implementation(libs.rangeSeekBar)
    //Android Library to handle software keyboard visibility change event.
    implementation(libs.keyboardvisibilityevent)

    //华为扫码服务
    implementation(libs.hms.scan)

    //利用了 Android 系统的原生 API 实现了分享功能
    implementation(libs.share2)

    //异常处理页面
    implementation(libs.customactivityoncrash)
    //异常信息上报组件
    implementation(libs.bugly.crashreport)

    implementation(libs.glide)
    ksp(libs.glide.ksp)
//    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation(libs.bundles.pictureselector)
    implementation(libs.subsampling.scale.image.view)


    //AndroidUtilCode 是一个强大易用的安卓工具类库
    implementation(libs.utilcodex) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    //A logger with a small, extensible API which provides utility on top of Android's normal Log class.
    implementation(libs.timber)
    implementation(libs.mmkv)

    //依赖注入框架
    implementation(libs.koin.android)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    // For debug builds only
//    debugImplementation(libs.leakcanary.android)
//    debugImplementation(libs.getActivity.logcat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
