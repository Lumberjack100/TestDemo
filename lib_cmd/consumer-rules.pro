#-keep public class com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.** { *; }
#-keep public class com.shmedo.lib.cmd.base.md_cmd.assemble.entity.** { *; }

# --------------- Moshi Proguard Rules ---------------
# 保留 Moshi 生成的 JsonAdapter（例如 FooJsonAdapter）
-keep class **JsonAdapter { *; }
# Moshi/Kotlin 反射与注解元数据（保守做法，避免误删必要元信息）
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, Signature, Exceptions, InnerClasses, EnclosingMethod

# --------------- Koin Proguard Rules ---------------
# 保留你定义的所有 Koin 模块（例如，val appModule = module { ... } 实际上会生成继承自 Module 的类）。
-keep class * extends org.koin.core.module.Module { *; }
-keep class org.koin.dsl.* { *; }

# 保留泛型签名，这对于 Koin 解析泛型依赖（如 List<MyType>）非常重要。
-keepattributes Signature