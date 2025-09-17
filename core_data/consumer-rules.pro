

# --------------- 百度开放平台 Proguard Rules ---------------
#百度开放平台 3D地图SDK、定位SDK 避免混淆配置
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-keep class com.baidu.vi.** {*;}
-dontwarn com.baidu.**