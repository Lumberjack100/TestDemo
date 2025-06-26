package com.shmedo.core.commonlib.mmkv

import android.os.Parcelable
import com.shmedo.core.commonlib.mmkv.property.MMKVLiveDataProperty
import com.shmedo.core.commonlib.mmkv.property.MMKVMapProperty
import com.shmedo.core.commonlib.mmkv.property.MMKVProperty
import com.shmedo.core.commonlib.mmkv.property.MMKVStateFlowProperty
import com.tencent.mmkv.MMKV


/**
 * 创建者：gonghe
 * 创建时间：2024/3/26
 * 描述： TODO
 */

open class MMKVOwner(override val mmapID: String) : IMMKVOwner {
    override val kv: MMKV by lazy { MMKV.mmkvWithID(mmapID) }
}

interface IMMKVOwner {
    val mmapID: String

    val kv: MMKV

    fun mmkvInt(default: Int = 0) =
        MMKVProperty({ kv.decodeInt(it, default) }, { kv.encode(first, second) })

    fun mmkvLong(default: Long = 0L) =
        MMKVProperty({ kv.decodeLong(it, default) }, { kv.encode(first, second) })

    fun mmkvBool(default: Boolean = false) =
        MMKVProperty({ kv.decodeBool(it, default) }, { kv.encode(first, second) })

    fun mmkvFloat(default: Float = 0f) =
        MMKVProperty({ kv.decodeFloat(it, default) }, { kv.encode(first, second) })

    fun mmkvDouble(default: Double = 0.0) =
        MMKVProperty({ kv.decodeDouble(it, default) }, { kv.encode(first, second) })

    fun mmkvString() =
        MMKVProperty({ kv.decodeString(it) }, { kv.encode(first, second) })

    fun mmkvString(default: String) =
        MMKVProperty({ kv.decodeString(it) ?: default }, { kv.encode(first, second) })

    fun mmkvStringSet() =
        MMKVProperty({ kv.decodeStringSet(it) }, { kv.encode(first, second) })

    fun mmkvStringSet(default: Set<String>) =
        MMKVProperty({ kv.decodeStringSet(it) ?: default }, { kv.encode(first, second) })

    fun mmkvBytes() =
        MMKVProperty({ kv.decodeBytes(it) }, { kv.encode(first, second) })

    fun mmkvBytes(default: ByteArray) =
        MMKVProperty({ kv.decodeBytes(it) ?: default }, { kv.encode(first, second) })

    fun <V> MMKVProperty<V>.asLiveData() = MMKVLiveDataProperty(this)

    fun <V> MMKVProperty<V>.asStateFlow() = MMKVStateFlowProperty(this)

    fun <V> MMKVProperty<V>.asMap() = MMKVMapProperty(this)

    fun clearAllKV() = kv.clearAll()
}

inline fun <reified T : Parcelable> IMMKVOwner.mmkvParcelable() =
    MMKVProperty(
        { kv.decodeParcelable(it, T::class.java) },
        { kv.encode(first, second) })

inline fun <reified T : Parcelable> IMMKVOwner.mmkvParcelable(default: T) =
    MMKVProperty(
        { kv.decodeParcelable(it, T::class.java) ?: default },
        { kv.encode(first, second) })


//TODO #GH#注释掉，因为使用了 kotlin 反射，项目中没有依赖反射库
//fun IMMKVOwner.getAllKV(): Map<String, Any?> = buildMap {
//    val types = arrayOf(
//        MMKVProperty::class,
//        MMKVLiveDataProperty::class,
//        MMKVStateFlowProperty::class,
//        MMKVMapProperty::class
//    )
//    this@getAllKV::class.memberProperties
//        .filterIsInstance<KProperty1<IMMKVOwner, *>>()
//        .forEach { property ->
//            property.isAccessible = true
//            val delegate = property.getDelegate(this@getAllKV)
//            if (types.any { it.isInstance(delegate) }) {
//                this[property.name] = when (val value = property.get(this@getAllKV)) {
//                    is LiveData<*> -> value.value
//                    is StateFlow<*> -> value.value
//                    else -> value
//                }
//            }
//            property.isAccessible = false
//        }
//}