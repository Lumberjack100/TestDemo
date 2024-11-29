import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
}

val majorVersion = 5
val minorVersion = 2
val patchVersion = 10

/**
 * 获取Git库HEAD的SHA1码前5位
 */
fun gitShortCommitId(): String {
    val cmd = "git rev-parse --short HEAD"
    return Runtime.getRuntime().exec(cmd).inputStream.reader().use { it.readText().trim() }
}

fun getReversion(): Int {
    var buildnum = 1
    try {
        // 使用 ProcessBuilder 更可靠地执行命令
        val processBuilder = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        processBuilder.redirectErrorStream(true) // 将错误输出和标准输出合并
        val process = processBuilder.start()
        val output = process.inputStream.reader().use { it.readText().trim() } // 读取命令输出

        // 确保正确地关闭了进程的输入输出流
        process.inputStream.close()
        process.outputStream.close()
        process.errorStream.close()
        process.waitFor() // 等待进程结束

        if (output == "") {
            buildnum = majorVersion * 10000 + minorVersion * 1000 + patchVersion * 100
        } else {
            buildnum = output.toInt()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return buildnum
}

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")
// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()
// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

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
            isMinifyEnabled = false
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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
            versionCode = getReversion()
            versionName = "$majorVersion.$minorVersion.$patchVersion"
            resValue("string", "app_name", "米易通物联网")   // 设置默认的app_name
            buildConfigField("String", "APP_NAME", "\"米易通物联网\"")
            buildConfigField("String", "PGY_API_KEY", "\"64454bf76fe2abd8dec45200c11fc93b\"")
            buildConfigField("String", "PGY_APP_KEY", "\"b8a852c106c6cc532332081e22f218a9\"")
            manifestPlaceholders["mapApikey"] = "QVihTHJNsLL5k9tBLwHFdLGR5TR0Lrlj"
        }
        create("demo") {//测试版本
            dimension = "version"
            applicationIdSuffix = ".v5"
            versionCode = getReversion()
            versionName = "5.2.9"
            resValue("string", "app_name", "米易通V5")   // 设置默认的app_name
            buildConfigField("String", "APP_NAME", "\"米易通V5\"")
            buildConfigField("String", "PGY_API_KEY", "\"db9ce8a6bd3b8b95c20c66e4205194d9\"")
            buildConfigField("String", "PGY_APP_KEY", "\"a8508805883003fdd3e223b6f9e85a60\"")
            manifestPlaceholders["mapApikey"] = "kOEHOjIEoj8JHieMUqF3qMINFqkmxOoW"
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .onEach { output ->
                val appName =
                    productFlavors.first().buildConfigFields["APP_NAME"]?.value?.toString()
                        ?.replace("\"", "")

                // 设置输出文件名
                val outputFileName =
                    "${appName}_${variant.versionName}_${gitShortCommitId()}_${getReversion()}_${variant.flavorName}_${variant.buildType.name}.apk"
                output.outputFileName = outputFileName
            }
    }
}

dependencies {
    implementation(project(":core_data"))
    implementation(project(":lib_cmd"))
    implementation(project(":lib_ble"))
    implementation(project(":lib_tcp"))

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
    //通过 exclude 把对官方 java 包的依赖排除了，引用默认指向 smooth-Navigation
    implementation(libs.androidx.navigation.fragment.ktx) {
        exclude(group = "androidx.navigation", module = "navigation-fragment")
    }

    implementation(libs.kunminx.unpeek.livedata)
    implementation(libs.kunminx.strict.databinding)
    implementation(libs.kunminx.smooth.navigation)

    //透明系统栏设置基础依赖包，必须要依赖
    implementation(libs.immersionbar)
    implementation(libs.immersionbar.ktx)

    //权限请求框架：https://github.com/getActivity/XXPermissions
    implementation(libs.getActivity.xxpermission)
    //Material Dialog
    implementation(libs.bundles.material.dialogs)
    //Powerful and Beautiful Popup for Android，can absolutely replace Dialog，PopupWindow，PopupMenu，BottomSheet，DrawerLayout，Spinner...
    implementation(libs.xpopup)
    implementation(libs.dialogx)
    //Toast 吐司
    implementation(libs.toastutils)
    implementation(libs.datetime.picker)

    //Android 快速构建 RecyclerView, 比 BRVAH 更简单强大 https://github.com/liangjingkanji/BRV
    implementation(libs.liangjingkanji.brv)
    //A ListView-like FastScroller for Android’s RecyclerView.
    implementation(libs.fastscroll)
    implementation(libs.tableview)

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
    implementation(libs.utilcodex)
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
