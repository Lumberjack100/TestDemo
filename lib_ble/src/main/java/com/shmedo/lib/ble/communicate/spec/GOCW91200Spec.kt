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
class GOCW91200Spec {
    companion object {
        val GOCW91200_SERVICE_UUID: UUID = UUID.fromString("00001910-0000-1000-8000-00805f9b34fb")
        val GOCW91200_NOTIFY_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb")
        val GOCW91200_WRITABLE_CHARACTERISTIC_UUID: UUID =UUID.fromString( "0000fff4-0000-1000-8000-00805f9b34fb")
    }
}