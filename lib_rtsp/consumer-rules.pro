# RTSP 模块 ProGuard 规则

# ExoPlayer (Media3) 混淆规则
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# 保留 RTSP 相关类
-keep class com.shmedo.lib.rtsp.** { *; }

