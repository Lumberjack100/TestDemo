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
class IOTCommandResponse : IOTCommandDataCallback() {
    var response: String = ""
    var responseList: List<String> = listOf()

    override fun onResponseReceived(
        device: BluetoothDevice,
        cmdResult: String,
        cmdResultList: List<String>
    ) {
        response = cmdResult
        responseList = cmdResultList
    }
}