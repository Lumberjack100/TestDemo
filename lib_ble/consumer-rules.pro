# --------------- NordicSemiconductor Proguard Rules ---------------
#NordicSemiconductor 相关库避免混淆配置
-keep class no.nordicsemi.android.log.** { *; }

# --------------- Koin Proguard Rules ---------------
# 保留你定义的所有 Koin 模块（例如，val appModule = module { ... } 实际上会生成继承自 Module 的类）。
-keep class * extends org.koin.core.module.Module { *; }
-keep class org.koin.dsl.* { *; }

# 保留泛型签名，这对于 Koin 解析泛型依赖（如 List<MyType>）非常重要。
-keepattributes Signature