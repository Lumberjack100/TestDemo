# RTSP 模块 ProGuard 规则

# ExoPlayer (Media3) 混淆规则
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# FFmpeg 解码器混淆规则（JNI 依赖 native 方法）
-keep class androidx.media3.decoder.ffmpeg.** { *; }
-dontwarn androidx.media3.decoder.ffmpeg.**
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留 RTSP 相关类
-keep class com.shmedo.lib.rtsp.** { *; }
