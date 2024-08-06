package com.shmedo.core.commonlib.jsonhelper

import com.squareup.moshi.FromJson
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
object NullBooleanAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Boolean {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextBoolean()
        }
        reader.nextNull<Unit>()
        return false
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Boolean?) {
        writer.value(value)
    }
}
