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
# 保留所有带有 native 方法的类名和方法名
-keepclasseswithmembernames class * {
    native <methods>;
}

# --------------- AgentWeb Proguard Rules ---------------
#AgentWeb 避免混淆配置
-keep class com.just.agentweb.** {
        *;
}
-dontwarn com.just.agentweb.**


#Bugly 避免混淆配置
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#Glide 避免混淆配置
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

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

#PictureSelector 避免混淆配置
-keep class com.luck.picture.lib.** { *; }

#PictureSelector 如果引入了Camerax库请添加混淆
-keep class com.luck.lib.camerax.** { *; }

#PictureSelector 如果引入了Ucrop库请添加混淆
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }



#AndroidUtilCode  避免混淆配置
#-keep class com.blankj.utilcode.** { *; }

#xpopup 避免混淆配置
-dontwarn com.lxj.xpopup.widget.**
-keep class com.lxj.xpopup.widget.**{*;}







