# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#=========================================基础不变的混淆配置=========================================##
#保留行号和源文件属性
-keepattributes SourceFile, LineNumberTable

# 保留实现了 Serializable 接口的类
-keep class * implements java.io.Serializable {
    *;
}

# --------------- xpopup Proguard Rules ---------------
#忽略警告规则：如果混淆时发现 com.lxj.xpopup.widget 有引用了缺失的类、方法或其它问题，不要报 warning。
-dontwarn com.lxj.xpopup.widget.**
#禁止混淆 com.lxj.xpopup.widget 库里的任何类或成员
-keep class com.lxj.xpopup.widget.**{*;}

# --------------- dialogx Proguard Rules ---------------
-keep class com.kongzue.dialogx.** { *; }
-dontwarn com.kongzue.dialogx.**

# 额外的，建议将 android.view 也列入 keep 范围：
-keep class android.view.** { *; }

# 若启用模糊效果，请增加如下配置：
-dontwarn androidx.renderscript.**
-keep public class androidx.renderscript.** { *; }

# --------------- Toaster Proguard Rules ---------------
-keep class com.hjq.toast.** {*;}
-dontwarn com.hjq.toast.**

# --------------- AgentWeb Proguard Rules ---------------
#AgentWeb 避免混淆配置
-keep class com.just.agentweb.** {
        *;
}
-dontwarn com.just.agentweb.**

# --------------- HMS Core SDK(华为扫码服务) Proguard Rules ---------------
#HMS Core SDK(华为扫码服务) 避免混淆配置
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

# --------------- Bugly Proguard Rules ---------------
#Bugly 避免混淆配置
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# --------------- Glide Proguard Rules ---------------
#Glide 避免混淆配置
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# --------------- PictureSelector Proguard Rules ---------------
#PictureSelector 避免混淆配置
-keep class com.luck.picture.lib.** { *; }

#PictureSelector 如果引入了Camerax库请添加混淆配置
-keep class com.luck.lib.camerax.** { *; }

#PictureSelector 如果引入了Ucrop库请添加混淆配置
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# --------------- AndroidUtilCode Proguard Rules ---------------
#AndroidUtilCode  避免混淆配置
#-keep class com.blankj.utilcode.** { *; }

# --------------- Koin Proguard Rules ---------------
# Keep annotation definitions
-keep class org.koin.core.annotation.** { *; }

# 保留使用了 Koin 相关注解的类，这在使用 Koin 注解（koin-annotations）时是必需的。
-keep @org.koin.core.annotation.* class * { *; }

# 保留你定义的所有 Koin 模块（例如，val appModule = module { ... } 实际上会生成继承自 Module 的类）。
-keep class * extends org.koin.core.module.Module { *; }
-keep class org.koin.dsl.* { *; }

# 保留泛型签名，这对于 Koin 解析泛型依赖（如 List<MyType>）非常重要。
-keepattributes Signature

# --------------- Moshi Proguard Rules ---------------
# 保留 Moshi 生成的 JsonAdapter（例如 FooJsonAdapter）
-keep class **JsonAdapter { *; }

# Moshi/Kotlin 反射与注解元数据（保守做法，避免误删必要元信息）
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, Signature, Exceptions, InnerClasses, EnclosingMethod

# 保留所有继承自 MoshiUtil.MoshiTypeReference 的匿名类/内部类，确保泛型信息不被擦除
-keep class * extends com.shmedo.core.commonlib.jsonhelper.MoshiUtil$MoshiTypeReference { *; }
