package com.shmedo.lib.ble.scanner.model

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */
data class DevicesScanFilter(
    val filterUuidRequired: Boolean?,
    val filterNearbyOnly: Boolean,
    val filterWithNames: Boolean
)
