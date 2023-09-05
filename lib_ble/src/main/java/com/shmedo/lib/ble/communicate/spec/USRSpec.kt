package com.shmedo.lib.ble.communicate.spec

import java.util.UUID

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： 济南有人物联网公司低功耗蓝牙模块服务和特征值
 *
 *
 */
class USRSpec {
    companion object {
        val USR_SERVICE_UUID: UUID = UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131")
        // A UUID of a characteristic with notify property.
        val USR_NOTIFY_CHARACTERISTIC_UUID: UUID = UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131")
        // A UUID of a characteristic with write property.
        val USR_WRITABLE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131")
    }
}