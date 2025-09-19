# --------------- Moshi Proguard Rules ---------------
# 保留 Moshi 生成的 JsonAdapter（例如 FooJsonAdapter）
-keep class **JsonAdapter { *; }
# Moshi/Kotlin 反射与注解元数据（保守做法，避免误删必要元信息）
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, Signature, Exceptions, InnerClasses, EnclosingMethod

# --------------- XUpdate Proguard Rules ---------------
#XUpdate 避免混淆配置
-keep class com.xuexiang.xupdate.entity.** { *; }
#XUpdate 注意，如果你使用的是自定义Api解析器解析，还需要给你自定义Api实体配上混淆，如下是自定义Api实体混淆规则：
-keep class com.shmedo.lib.network.response.** { *; }