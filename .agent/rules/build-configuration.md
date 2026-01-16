---
trigger: glob
globs: *.gradle.kts,gradle.properties,version.properties,libs.versions.toml
---

# 构建配置规范

## 项目构建结构

### 核心配置文件
- [`build.gradle.kts`](mdc:build.gradle.kts) - 根项目构建配置
- [`settings.gradle.kts`](mdc:settings.gradle.kts) - 项目设置和模块包含
- [`gradle/libs.versions.toml`](mdc:gradle/libs.versions.toml) - 版本目录统一管理
- [`app/version.properties`](mdc:app/version.properties) - 版本号配置
- [`keystore.properties`](mdc:keystore.properties) - 签名配置

### 模块构建文件
```
├── app/build.gradle.kts              # 主应用模块
├── core_commonlib/build.gradle.kts   # 通用库模块
├── core_data/build.gradle.kts        # 数据层模块
├── core_model/build.gradle.kts       # 数据模型模块
├── lib_ble/build.gradle.kts          # BLE 通信模块
├── lib_cmd/build.gradle.kts          # 设备指令模块
├── lib_network/build.gradle.kts      # 网络请求模块
└── lib_tcp/build.gradle.kts          # TCP 通信模块
```

## 版本管理规范

### 版本号策略
```properties
# version.properties
VERSION_CODE=100
MAJOR_VERSION=1
MINOR_VERSION=0
PATCH_VERSION=0

# 测试版本
TEST_MAJOR_VERSION=1
TEST_MINOR_VERSION=0
TEST_PATCH_VERSION=0
```

### 版本号规则
- **MAJOR**: 重大功能更新或架构变更
- **MINOR**: 新功能添加或重要改进
- **PATCH**: Bug 修复或小幅优化
- **VERSION_CODE**: 每次构建递增，用于应用商店版本控制

### 产品变体配置
```kotlin
android {
    flavorDimensions += "version"
    productFlavors {
        create("production") {
            dimension = "version"
            applicationId = "com.shmedo.mcloudapp.iot"
            versionName = "$majorVersion.$minorVersion.$patchVersion"
            resValue("string", "app_name", "米易通")
        }
        
        create("demo") {
            dimension = "version"
            applicationIdSuffix = ".v5"
            versionName = "$testMajorVersion.$testMinorVersion.$testPatchVersion"
            resValue("string", "app_name", "米易通V5")
        }
    }
}
```

## 依赖管理

### 版本目录使用
```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.2.20"
agp = "8.13.0"
compileSdk = "36"
targetSdk = "35"
minSdk = "26"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib-jdk8", version.ref = "kotlin" }

[bundles]
androidx-lifecycle = ["androidx-lifecycle-viewmodel", "androidx-lifecycle-livedata"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

### 模块依赖配置
```kotlin
// app/build.gradle.kts
dependencies {
    // 项目模块依赖
    implementation(project(":core_data"))
    implementation(project(":lib_cmd"))
    implementation(project(":lib_ble"))
    implementation(project(":lib_tcp"))
    
    // 使用版本目录
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.androidx.lifecycle)
    
    // 本地 AAR 文件
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}
```

## 构建类型配置

### Release 构建优化
```kotlin
android {
    buildTypes {
        getByName("release") {
            // 代码混淆和压缩
            isMinifyEnabled = true
            isShrinkResources = true
            
            // 混淆规则
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // 签名配置
            signingConfig = signingConfigs.getByName("release")
        }
        
        getByName("debug") {
            // 调试配置
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            // 测试覆盖率
            testCoverageEnabled = true
        }
    }
}
```

### 签名配置
```kotlin
// keystore.properties
storeFile=../mcloud.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password

// build.gradle.kts
android {
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
}
```

## 构建优化

### 编译优化
```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    // 构建缓存
    buildCache {
        local {
            isEnabled = true
        }
    }
}

// gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
kotlin.incremental=true
kotlin.incremental.android=true
```

### 资源优化
```kotlin
android {
    // 资源压缩
    buildTypes {
        release {
            isShrinkResources = true
        }
    }
    
    // 排除不需要的资源
    packagingOptions {
        resources {
            excludes += "/META-INF/{INDEX.LIST,io.netty.versions.properties}"
        }
    }
    
    // ABI 过滤
    defaultConfig {
        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
        }
    }
}
```

## 构建脚本规范

### 任务定义
```kotlin
// 自定义构建任务
tasks.register("generateVersionInfo") {
    doLast {
        val versionFile = file("src/main/assets/version.json")
        versionFile.writeText("""
            {
                "versionName": "${android.defaultConfig.versionName}",
                "versionCode": ${android.defaultConfig.versionCode},
                "buildTime": "${System.currentTimeMillis()}"
            }
        """.trimIndent())
    }
}

// 构建前执行
tasks.named("preBuild") {
    dependsOn("generateVersionInfo")
}
```

### 输出文件命名
```kotlin
android {
    applicationVariants.all {
        val flavor = flavorName ?: "noflavor"
        val buildTypeName = buildType.name
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val appName = if (flavor.contains("demo", ignoreCase = true)) "米易通V5" else "米易通"
            
            outputImpl.outputFileName = 
                "${appName}_${versionName}_${versionCode}_${flavor}_${buildTypeName}.apk"
        }
    }
}
```

## 构建命令

### 常用构建命令
```bash
# 清理项目
./gradlew clean

# 构建所有变体
./gradlew build

# 构建特定变体
./gradlew assembleDemoDebug
./gradlew assembleProductionRelease

# 运行测试
./gradlew test
./gradlew connectedAndroidTest

# 生成测试覆盖率报告
./gradlew createDemoDebugCoverageReport

# 依赖检查
./gradlew dependencyUpdates

# 构建分析
./gradlew --profile --build-cache assembleRelease
```

### CI/CD 构建脚本
```bash
#!/bin/bash
# build.sh

set -e

echo "开始构建 mCloudapp_V5..."

# 清理项目
./gradlew clean

# 运行测试
./gradlew test

# 构建 Release 版本
./gradlew assembleProductionRelease

# 构建 Demo 版本
./gradlew assembleDemoRelease

echo "构建完成！"
```

## 模块化构建

### 库模块配置模板
```kotlin
// lib_*/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shmedo.lib.modulename"
    compileSdk = libs.versions.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(project(":core_commonlib"))
    implementation(libs.kotlin.stdlib.jdk8)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
```

### 依赖传递控制
```kotlin
// 避免依赖冲突
dependencies {
    implementation(libs.some.library) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    
    // API 依赖 - 传递给使用方
    api(project(":core_model"))
    
    // Implementation 依赖 - 不传递
    implementation(project(":core_commonlib"))
}
```