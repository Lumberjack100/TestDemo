package com.shmedo.lib.ble.communicate.spec

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
class ESP32BSpec {
    companion object {
        val ESP32B_SERVICE_UUID: String = "0000a002-0000-1000-8000-00805f9b34fb"
        val ESP32B_NOTIFY_CHARACTERISTIC_UUID: String = "0000c305-0000-1000-8000-00805f9b34fb"
        val ESP32B_WRITABLE_CHARACTERISTIC_UUID: String = "0000c303-0000-1000-8000-00805f9b34fb"
    }
}