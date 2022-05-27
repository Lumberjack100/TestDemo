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

 #高德开放平台 3D地图SDK、定位SDK 混淆配置
    #3D 地图 V5.0.0之后：
    -keep   class com.amap.api.maps.**{*;}
    -keep   class com.autonavi.**{*;}
    -keep   class com.amap.api.trace.**{*;}

    #定位
    -keep class com.amap.api.location.**{*;}
    -keep class com.amap.api.fence.**{*;}
    -keep class com.loc.**{*;}

      #钉钉分享混淆处理
    -keep class  com.android.dingtalk.share.ddsharemodule.** {
       *;
    }

 #NordicSemiconductor相关库混淆处理
   -keep class no.nordicsemi.android.log.** { *; }

 #避免混淆Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#加入排除HMS Core SDK的混淆配置脚本
   -ignorewarnings
   -keepattributes *Annotation*
   -keepattributes Exceptions
   -keepattributes InnerClasses
   -keepattributes Signature
   -keepattributes SourceFile,LineNumberTable
   -keep class com.huawei.hianalytics.**{*;}
   -keep class com.huawei.updatesdk.**{*;}
   -keep class com.huawei.hms.**{*;}


# 友盟统计SDK混淆配置(重要：启用代码混淆必须设置)
    -keep class com.umeng.** {*;}
    -keep class com.uc.** {*;}
    -keepclassmembers class * {
       public <init> (org.json.JSONObject);
    }
    -keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
    }
    -keep class com.zui.** {*;}
    -keep class com.miui.** {*;}
    -keep class com.heytap.** {*;}
    -keep class a.** {*;}
    -keep class com.vivo.** {*;}