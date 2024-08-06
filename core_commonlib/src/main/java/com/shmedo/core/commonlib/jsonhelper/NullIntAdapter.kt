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
object NullIntAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Int {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextInt()
        }
        reader.nextNull<Unit>()
        return 0
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Int?) {
        writer.value(value)
    }
}
