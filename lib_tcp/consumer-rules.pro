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

# --------------- Netty Proguard Rules ---------------
# Netty - General rules to prevent issues with reflection and native code access
-keep class io.netty.** { *; }

