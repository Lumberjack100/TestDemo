package com.shmedo.lib.ble.communicate.parser

import android.bluetooth.BluetoothDevice

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/6
 *
 * 描述： TODO
 *
 *
 */
interface CommandCallback {
    fun onResponseReceived(
        device: BluetoothDevice,
        cmdResult: String = "",
    )
}