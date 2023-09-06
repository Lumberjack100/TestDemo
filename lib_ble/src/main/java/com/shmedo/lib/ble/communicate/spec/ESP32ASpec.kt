package com.shmedo.lib.ble.communicate.spec

import java.util.UUID

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
class ESP32ASpec {
    companion object {
        val ESP32_SERVICE_UUID: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        val ESP32_NOTIFY_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
        val ESP32_WRITABLE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb")
    }
}