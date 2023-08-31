package com.shmedo.lib.core.util


import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 * 创建者： gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述：基于moshi的json转换封装
 *
 *
 * <p>
 *  <pre>
 *      <code>
 *     /*对象反序列化泛型测试*/
 *     val user = User("喻志强", 19)
 *     val userStr = MoshiUtil.toJson(user)
 *     val userObj = MoshiUtil.fromJson<User>(userStr)
 *     println("userObj = ${userObj!!.name}")
 *
 *     /*复杂对象反序列化时的泛型测试*/
 *     val baseRespJsonStr = """
 *         {"code":200,"msg":"ok","data":[{"name":"name1","age":1,"hobby":[{"type":"类型","name":"爱好1"}]},{"name":"name2","age":2,"hobby":[{"type":"类型","name":"爱好2"}]},{"name":"name3","age":3,"hobby":[{"type":"类型","name":"爱好3"}]},{"name":"name4","age":4,"hobby":[{"type":"类型","name":"爱好4"}]},{"name":"name5","age":5,"hobby":[{"type":"类型","name":"爱好5"}]},{"name":"name6","age":6,"hobby":[{"type":"类型","name":"爱好6"}]},{"name":"name7","age":7,"hobby":[{"type":"类型","name":"爱好7"}]},{"name":"name8","age":8,"hobby":[{"type":"类型","name":"爱好8"}]},{"name":"name9","age":9,"hobby":[{"type":"类型","name":"爱好9"}]},{"name":"name10","age":10,"hobby":[{"type":"类型","name":"爱好10"}]}]}
 *     """.trimIndent()
 *     val baseResp = MoshiUtil.fromJson<BaseResp<List<User>>>(baseRespJsonStr)
 *     println("baseResp = ${baseResp}")
 *     if (baseResp == null) {
 *         println("解析异常")
 *     }
 *
 *     println(baseResp!!.data.get(0).name)
 *
 *     val baseRespToJson = MoshiUtil.toJson(baseResp)
 *     println("baseRespToJson = ${baseRespToJson}")
 *
 *     /*直接是一个list测试*/
 *     val userListJsonStr = MoshiUtil.toJson(baseResp.data)
 *     println("userListJsonStr = ${userListJsonStr}")
 *     val userList = MoshiUtil.fromJson<List<User>>(userListJsonStr)
 *     println("userList = ${userList}")
 *
 *     println(userList!!.get(0).hobby.get(0).name)
 *     </code>
 * </pre>
 */
object MoshiUtil {
    abstract class MoshiTypeReference<T> // 自定义的类，用来包装泛型

    val moshi: Moshi = Moshi.Builder()
//        .addLast(KotlinJsonAdapterFactory()) //TODO 使用kotlin反射时，需要添加这个适配器以及 implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
        .add(NullIntAdapter)
        .add(NullDoubleAdapter)
        .add(NullBooleanAdapter)
        .add(NullStringAdapter)
        .build()

    inline fun <reified T> toJson(src: T, indent: String = ""): String {
        try {
            val jsonAdapter = moshi.adapter<T>(getGenericType<T>())
            return jsonAdapter.indent(indent).toJson(src)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""

    }

    inline fun <reified T> fromJson(jsonStr: String): T? {
        try {
            val jsonAdapter = moshi.adapter<T>(getGenericType<T>())
            return jsonAdapter.fromJson(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    inline fun <reified T> getGenericType(): Type {
        return object :
            MoshiTypeReference<T>() {}::class.java
            .genericSuperclass
            .let { it as ParameterizedType }
            .actualTypeArguments
            .first()

    }
}