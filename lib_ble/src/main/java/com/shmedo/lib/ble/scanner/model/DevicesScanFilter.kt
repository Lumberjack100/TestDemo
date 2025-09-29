package com.shmedo.lib.ble.scanner.model

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */
enum class SortMode { ByRssiDesc, None }

data class DevicesScanFilter(
    val filterUuidRequired: Boolean? = null,
    val filterNearbyOnly: Boolean = false,
    val filterWithNames: Boolean = false,
    val sortMode: SortMode = SortMode.ByRssiDesc
)
