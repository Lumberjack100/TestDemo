package com.shmedo.core.commonlib.jsonhelper

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/30
 *
 * 描述： TODO
 *
 *
 */
object NullStringAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): String {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> reader.nextString() // 将数字转换为字符串
            JsonReader.Token.STRING -> reader.nextString() // 正常读取字符串
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                "" // 如果是 null，返回默认值
            }

            else -> throw JsonDataException("Expected NUMBER or STRING but was ${reader.peek()}")
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: String?) {
        writer.value(value)
    }
}

