pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
        maven(url = "https://developer.huawei.com/repo/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()//https://dl.google.com/dl/android/maven2/
        mavenCentral()//https://repo.maven.apache.org/maven2/
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://developer.huawei.com/repo/")
//        jcenter()//https://jcenter.bintray.com/
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "mCloudapp"
include(":app")
include(":lib_tcp")
include(":lib_ble")
include(":lib_network")
include(":lib_cmd")
include(":core_data")
include(":core_commonlib")
include(":core_model")
